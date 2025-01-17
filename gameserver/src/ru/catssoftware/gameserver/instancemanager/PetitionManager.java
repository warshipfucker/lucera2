/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.catssoftware.gameserver.instancemanager;

import java.text.SimpleDateFormat;
import java.util.Date;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;


import org.apache.log4j.Logger;


import ru.catssoftware.Config;
import ru.catssoftware.Message;
import ru.catssoftware.gameserver.datatables.GmListTable;
import ru.catssoftware.gameserver.model.L2World;
import ru.catssoftware.gameserver.model.actor.instance.L2PcInstance;
import ru.catssoftware.gameserver.network.SystemChatChannelId;
import ru.catssoftware.gameserver.network.SystemMessageId;
import ru.catssoftware.gameserver.network.serverpackets.CreatureSay;
import ru.catssoftware.gameserver.network.serverpackets.L2GameServerPacket;
import ru.catssoftware.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.catssoftware.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Tempy
 */
public final class PetitionManager
{
	protected static Logger				_log	= Logger.getLogger(PetitionManager.class.getName());
	private static PetitionManager		_instance;

	private static int					_lastUsedId;

	private FastMap<Integer, Petition>	_pendingPetitions;
	private FastMap<Integer, Petition>	_completedPetitions;

	private static enum PetitionState
	{
		Pending, Responder_Cancel, Responder_Missing, Responder_Reject, Responder_Complete, Petitioner_Cancel, Petitioner_Missing, In_Process, Completed
	}

	private static enum PetitionType
	{
		Immobility, Recovery_Related, Bug_Report, Quest_Related, Bad_User, Suggestions, Game_Tip, Operation_Related, Other
	}

	public static PetitionManager getInstance()
	{
		if (_instance == null)
			_instance = new PetitionManager();

		return _instance;
	}

	private class Petition
	{
		private long					_submitTime	= System.currentTimeMillis();

		private int						_id;
		private PetitionType			_type;
		private PetitionState			_state		= PetitionState.Pending;
		private String					_content;

		private FastList<CreatureSay>	_messageLog	= new FastList<CreatureSay>();

		private L2PcInstance			_petitioner;
		private L2PcInstance			_responder;

		public Petition(L2PcInstance petitioner, String petitionText, int petitionType)
		{
			petitionType--;
			_lastUsedId++;
			_id = _lastUsedId;
			if (petitionType >= PetitionType.values().length)
				_log.warn("PetitionManager:Petition : invalid petition type (received type was +1) : " + petitionType);
			_type = PetitionType.values()[petitionType];
			_content = petitionText;

			_petitioner = petitioner;
		}

		protected boolean addLogMessage(CreatureSay cs)
		{
			return _messageLog.add(cs);
		}

		protected FastList<CreatureSay> getLogMessages()
		{
			return _messageLog;
		}

		public boolean endPetitionConsultation(PetitionState endState)
		{
			setState(endState);

			if (getResponder() != null && getResponder().isOnline() == 1)
			{
				if (endState == PetitionState.Responder_Reject)
					getPetitioner().sendMessage(Message.getMessage(getPetitioner(), Message.MessageId.MSG_PETITION_REJECTED));
				else
				{
					// Ending petition consultation with <Player>.
					SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_ENDED_WITH_S1);
					sm.addString(getPetitioner().getName());
					getResponder().sendPacket(sm);

					if (endState == PetitionState.Petitioner_Cancel)
					{
						// Receipt No. <ID> petition cancelled.
						sm = new SystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED);
						sm.addNumber(getId());
						getResponder().sendPacket(sm);
					}
				}
			}

			// End petition consultation and inform them, if they are still online.
			if (getPetitioner() != null && getPetitioner().isOnline() == 1)
				getPetitioner().sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK);

			getCompletedPetitions().put(getId(), this);
			return (getPendingPetitions().remove(getId()) != null);
		}

		public String getContent()
		{
			return _content;
		}

		public int getId()
		{
			return _id;
		}

		public L2PcInstance getPetitioner()
		{
			return _petitioner;
		}

		public L2PcInstance getResponder()
		{
			return _responder;
		}


		public long getSubmitTime()
		{
			return _submitTime;
		}

		public PetitionState getState()
		{
			return _state;
		}

		public String getTypeAsString()
		{
			return _type.toString().replace("_", " ");
		}

		public void sendPetitionerPacket(L2GameServerPacket responsePacket)
		{
			if (getPetitioner() == null || getPetitioner().isOnline() == 0)
				return;

			getPetitioner().sendPacket(responsePacket);
		}

		public void sendResponderPacket(L2GameServerPacket responsePacket)
		{
			if (getResponder() == null || getResponder().isOnline() == 0)
			{
				endPetitionConsultation(PetitionState.Responder_Missing);
				return;
			}

			getResponder().sendPacket(responsePacket);
		}

		public void setState(PetitionState state)
		{
			_state = state;
		}

		public void setResponder(L2PcInstance respondingAdmin)
		{
			if (getResponder() != null)
				return;

			_responder = respondingAdmin;
		}
	}

	private PetitionManager()
	{
		_log.info("PetitionManager: initalized.");
		_pendingPetitions = new FastMap<Integer, Petition>();
		_completedPetitions = new FastMap<Integer, Petition>();
	}

	public void clearPendingPetitions()
	{
		int numPetitions = getPendingPetitionCount();

		getPendingPetitions().clear();
		_log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
	}

	public boolean acceptPetition(L2PcInstance respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;

		Petition currPetition = getPendingPetitions().get(petitionId);

		if (currPetition.getResponder() != null)
			return false;

		currPetition.setResponder(respondingAdmin);
		currPetition.setState(PetitionState.In_Process);

		// Petition application accepted. (Send to Petitioner)
		currPetition.sendPetitionerPacket(new SystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));

		// Petition application accepted. Reciept No. is <ID>
		SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
		sm.addNumber(currPetition.getId());
		currPetition.sendResponderPacket(sm);

		// Petition consultation with <Player> underway.
		sm = new SystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY);
		sm.addString(currPetition.getPetitioner().getName());
		currPetition.sendResponderPacket(sm);
		return true;
	}

	public boolean cancelActivePetition(L2PcInstance player)
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel));

			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Responder_Cancel));
		}

		return false;
	}

	public void checkPetitionMessages(L2PcInstance petitioner)
	{
		if (petitioner != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;

				if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
				{
					for (CreatureSay logMessage : currPetition.getLogMessages())
						petitioner.sendPacket(logMessage);

					return;
				}
			}
	}

	public boolean endActivePetition(L2PcInstance player)
	{
		if (!player.isGM())
			return false;

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;

			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Completed));
		}

		return false;
	}

	protected FastMap<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}

	protected FastMap<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}

	public int getPendingPetitionCount()
	{
		return getPendingPetitions().size();
	}

	public int getPlayerTotalPetitionCount(L2PcInstance player)
	{
		if (player == null)
			return 0;

		int petitionCount = 0;

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;

			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}

		for (Petition currPetition : getCompletedPetitions().values())
		{
			if (currPetition == null)
				continue;

			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}

		return petitionCount;
	}

	public boolean isPetitionInProcess()
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;

			if (currPetition.getState() == PetitionState.In_Process)
				return true;
		}

		return false;
	}

	public boolean isPetitionInProcess(int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;

		Petition currPetition = getPendingPetitions().get(petitionId);
		return (currPetition.getState() == PetitionState.In_Process);
	}

	public boolean isPlayerInConsultation(L2PcInstance player)
	{
		if (player != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;

				if (currPetition.getState() != PetitionState.In_Process)
					continue;

				if ((currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
						|| (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()))
					return true;
			}

		return false;
	}

	public boolean isPetitioningAllowed()
	{
		return Config.PETITIONING_ALLOWED;
	}

	public boolean isPlayerPetitionPending(L2PcInstance petitioner)
	{
		if (petitioner != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;

				if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
					return true;
			}

		return false;
	}

	private boolean isValidPetition(int petitionId)
	{
		return getPendingPetitions().containsKey(petitionId);
	}

	public boolean rejectPetition(L2PcInstance respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;

		Petition currPetition = getPendingPetitions().get(petitionId);

		if (currPetition.getResponder() != null)
			return false;

		currPetition.setResponder(respondingAdmin);
		return (currPetition.endPetitionConsultation(PetitionState.Responder_Reject));
	}

	public boolean sendActivePetitionMessage(L2PcInstance player, String messageText)
	{
		//if (!isPlayerInConsultation(player))
		//return false;

		CreatureSay cs;

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;

			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), SystemChatChannelId.Chat_User_Pet, player.getName(), messageText);
				currPetition.addLogMessage(cs);

				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}

			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), SystemChatChannelId.Chat_GM_Pet, player.getName(), messageText);
				currPetition.addLogMessage(cs);

				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
		}

		return false;
	}

	public void sendPendingPetitionList(L2PcInstance activeChar)
	{
		TextBuilder htmlContent = new TextBuilder("<html><body>" + "<center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm z");

		if (getPendingPetitionCount() == 0)
			htmlContent.append("<tr><td colspan=\"4\"><center>There are no currently pending petitions.</center></td></tr>");
		else
			htmlContent.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td>" + "<td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>");

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;

			htmlContent.append("<tr><td>");

			if (currPetition.getState() != PetitionState.In_Process)
				htmlContent.append("<button value=\"View\" action=\"bypass -h admin_view_petition " + currPetition.getId() + "\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui94\">");
			else
				htmlContent.append("<font color=\"999999\">In Process</font>");

			htmlContent.append("</td><td>" + currPetition.getPetitioner().getName() + "</td><td>" + currPetition.getTypeAsString() + "</td><td>" + dateFormat.format(new Date(currPetition.getSubmitTime())) + "</td></tr>");
		}

		htmlContent.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" " + "height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui94\"><br><button value=\"Back\" action=\"bypass -h admin_control\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui94\"></center></body></html>");
		NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
		htmlMsg.setHtml(htmlContent.toString());
		activeChar.sendPacket(htmlMsg);
	}

	public int submitPetition(L2PcInstance petitioner, String petitionText, int petitionType)
	{
		Petition newPetition = new Petition(petitioner, petitionText, petitionType);
		int newPetitionId = newPetition.getId();
		getPendingPetitions().put(newPetitionId, newPetition);

		GmListTable.broadcastToGMs(new CreatureSay(petitioner.getObjectId(), SystemChatChannelId.Chat_Hero, "Petition System", (petitioner.getName() + " создал петицию")));

		if (Config.SEND_PAGE_ON_PETTITON)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player != null && player.isGM())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/petition/notification.htm");
					player.sendPacket(html);
				}
			}
		}
		return newPetitionId;
	}

	public void viewPetition(L2PcInstance activeChar, int petitionId)
	{
		if (!activeChar.isGM())
			return;

		if (!isValidPetition(petitionId))
			return;

		Petition currPetition = getPendingPetitions().get(petitionId);
		TextBuilder htmlContent = new TextBuilder("<html><body>");
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM HH:mm z");

		htmlContent.append("<center><br><font color=\"LEVEL\">Petition #" + currPetition.getId() + "</font><br1>");
		htmlContent.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>");
		htmlContent.append("Submit Time: " + dateFormat.format(new Date(currPetition.getSubmitTime())) + "<br1>");
		htmlContent.append("Petitioner: " + currPetition.getPetitioner().getName() + "<br1>");
		htmlContent.append("Petition Type: " + currPetition.getTypeAsString() + "<br>" + currPetition.getContent() + "<br>");
		htmlContent.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition " + currPetition.getId() + "\"" + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui94\"><br1>");
		htmlContent.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition " + currPetition.getId() + "\" " + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui94\"><br>");
		htmlContent.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" " + "fore=\"sek.cbui94\"></center>");
		htmlContent.append("</body></html>");

		NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
		htmlMsg.setHtml(htmlContent.toString());
		activeChar.sendPacket(htmlMsg);
	}
}