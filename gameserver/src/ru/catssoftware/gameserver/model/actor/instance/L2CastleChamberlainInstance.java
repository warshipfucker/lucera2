package ru.catssoftware.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ru.catssoftware.Config;
import ru.catssoftware.Message;
import ru.catssoftware.gameserver.SevenSigns;
import ru.catssoftware.gameserver.ai.CtrlIntention;
import ru.catssoftware.gameserver.datatables.ClanTable;
import ru.catssoftware.gameserver.datatables.SkillTable;
import ru.catssoftware.gameserver.datatables.TeleportLocationTable;
import ru.catssoftware.gameserver.instancemanager.CastleManager;
import ru.catssoftware.gameserver.instancemanager.CastleManorManager;
import ru.catssoftware.gameserver.instancemanager.ClanHallManager;
import ru.catssoftware.gameserver.model.L2Clan;
import ru.catssoftware.gameserver.model.L2Skill;
import ru.catssoftware.gameserver.model.L2TeleportLocation;
import ru.catssoftware.gameserver.model.entity.Castle;
import ru.catssoftware.gameserver.model.entity.ClanHall;
import ru.catssoftware.gameserver.network.SystemMessageId;
import ru.catssoftware.gameserver.network.serverpackets.ActionFailed;
import ru.catssoftware.gameserver.network.serverpackets.ExShowCropInfo;
import ru.catssoftware.gameserver.network.serverpackets.ExShowCropSetting;
import ru.catssoftware.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import ru.catssoftware.gameserver.network.serverpackets.ExShowSeedInfo;
import ru.catssoftware.gameserver.network.serverpackets.ExShowSeedSetting;
import ru.catssoftware.gameserver.network.serverpackets.MyTargetSelected;
import ru.catssoftware.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.catssoftware.gameserver.network.serverpackets.ValidateLocation;
import ru.catssoftware.gameserver.templates.chars.L2NpcTemplate;
import ru.catssoftware.gameserver.templates.skills.L2SkillType;
import ru.catssoftware.gameserver.util.Util;

import javolution.text.TextBuilder;

public class L2CastleChamberlainInstance extends L2MerchantInstance
{
	protected static final int	COND_ALL_FALSE				= 0;
	protected static final int	COND_BUSY_BECAUSE_OF_SIEGE	= 1;
	protected static final int	COND_OWNER					= 2;
	private int _preDay;
	private int _preHour;

	private final NpcHtmlMessage IN_SIEGE;

	public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		IN_SIEGE = new NpcHtmlMessage(getObjectId());
		IN_SIEGE.setFile("data/html/chamberlain/chamberlain-busy.htm");
		IN_SIEGE.replace("%objectId%", String.valueOf(getObjectId()));
		IN_SIEGE.replace("%npcname%", String.valueOf(getName()));
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		player.setLastFolkNPC(this);

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the zL2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to
		// avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != this.getObjectId())
			return;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			//Take note: it is better to check privileges in each command
			//because that way it is easier for developers to understand the purpose
			//of various CP_CS values
			if (actualCommand.equals("banish_foreigner"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS)) return;
				if (siegeBlocksFunction(player)) return;
				getCastle().banishForeigners(); // Move non-clan members off castle area
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-banishafter.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("banish_foreigner_show"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_DISMISS)) return;
				if (siegeBlocksFunction(player)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-banishfore.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("list_siege_clans"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_MANAGE_SIEGE))
					return;
				getCastle().getSiege().listRegisterClan(player);
				return;
			}
			else if (actualCommand.equals("receive_report"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-report.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%castlename%", getCastle().getName());
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				if (clan != null)
				{
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
				}
				else //avoid NPE in GM view when castle belongs to NPCs!
				{
					html.replace("%clanname%", "NPC");
					html.replace("%clanleadername%", "");
				}
				html.replace("%ss_event%", SevenSigns.getInstance().getCurrentPeriodNameText());
				html.replace("%ss_avarice%", SevenSigns.getCabalNameText(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE)));
				html.replace("%ss_gnosis%", SevenSigns.getCabalNameText(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS)));
				html.replace("%ss_strife%", SevenSigns.getCabalNameText(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE)));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("items"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				if (val.isEmpty()) return;
				player.tempInvetoryDisable();
				showBuyWindow(player, Integer.parseInt(val + "1"));
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (actualCommand.equals("manage_siege_defender"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_MANAGE_SIEGE)) return;
				if (siegeBlocksFunction(player)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-defender.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("manage_vault"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_TAXES)) return;
				if (siegeBlocksFunction(player)) return;
				String filename = "data/html/chamberlain/chamberlain-vault.htm";
				int amount = 0;
				if (val.equals("deposit"))
				{
					try
					{
						amount = Integer.parseInt(st.nextToken());
					}
					catch (NoSuchElementException e)
					{
					}
					if (amount > 0 && (long) getCastle().getTreasury() + amount < Integer.MAX_VALUE)
					{
						if (player.reduceAdena("Castle", amount, this, true))
							getCastle().addToTreasuryNoTax(amount);
						else
							sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
					}
				}
				else if (val.equals("withdraw"))
				{
					try
					{
						amount = Integer.parseInt(st.nextToken());
					}
					catch (NoSuchElementException e)
					{
					}
					if (amount > 0)
					{
						if (getCastle().getTreasury() < amount)
							filename = "data/html/chamberlain/chamberlain-vault-no.htm";
						else
						{
							if (getCastle().addToTreasuryNoTax((-1) * amount))
								player.addAdena("Castle", amount, this, true);
						}
					}
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				html.replace("%tax_income%", Util.formatNumber(getCastle().getTreasury()));
				html.replace("%withdraw_amount%", Util.formatNumber(amount));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("manor"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_MANOR_ADMIN)) return;
				if (siegeBlocksFunction(player)) return;
				String filename = "";
				if (CastleManorManager.getInstance().isDisabled())
					filename = "data/html/npcdefault.htm";
				else
				{
					int cmd = Integer.parseInt(val);
					switch (cmd)
					{
					case 0:
						filename = "data/html/chamberlain/manor/manor.htm";
						break;
						// TODO: correct in html's to 1
					case 4:
						filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
						break;
					default:
						filename = "data/html/chamberlain/chamberlain-no.htm";
					break;
					}
				}
				if (filename.length() != 0)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				return;
			}
			else if (command.startsWith("manor_menu_select"))
			{// input string format:
				// manor_menu_select?ask=X&state=Y&time=X
				if (CastleManorManager.getInstance().isUnderMaintenance())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
					return;
				}

				String params = command.substring(command.indexOf("?") + 1);
				StringTokenizer str = new StringTokenizer(params, "&");
				int ask = Integer.parseInt(str.nextToken().split("=")[1]);
				int state = Integer.parseInt(str.nextToken().split("=")[1]);
				int time = Integer.parseInt(str.nextToken().split("=")[1]);

				int castleId;
				if (state == -1) // info for current manor
					castleId = getCastle().getCastleId();
				else
					// info for requested manor
					castleId = state;

				switch (ask)
				{ // Main action
					case 3: // Current seeds (Manor info)
						if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							player.sendPacket(new ExShowSeedInfo(castleId, null));
						else
							player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
						break;
					case 4: // Current crops (Manor info)
						if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							player.sendPacket(new ExShowCropInfo(castleId, null));
						else
							player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
						break;
					case 5: // Basic info (Manor info)
						player.sendPacket(new ExShowManorDefaultInfo());
						break;
					case 7: // Edit seed setup
						if (getCastle().isNextPeriodApproved())
							player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						else
							player.sendPacket(new ExShowSeedSetting(getCastle().getCastleId()));
						break;
					case 8: // Edit crop setup
						if (getCastle().isNextPeriodApproved())
							player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						else
							player.sendPacket(new ExShowCropSetting(getCastle().getCastleId()));
						break;
				}
			}
			else if (actualCommand.equals("operate_door")) // door
				// control
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_OPEN_DOOR)) return;
				if (!Config.SIEGE_GATE_CONTROL && getCastle().getSiege().getIsInProgress()) {
					player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
					return;
				}
				if (!val.isEmpty())
				{
					boolean open = (Integer.parseInt(val) == 1);
					while (st.hasMoreTokens())
						getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
					if (open)
					{
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/chamberlain/chamberlain-door-opened.htm");
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
						return;
					}
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-door-closed.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/" + getTemplate().getNpcId() + "-d.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("tax_setup"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_TAXES)) return;
				if (siegeBlocksFunction(player)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-settaxrate.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%currTax%", String.valueOf(getCastle().getTaxPercent()));
				html.replace("%nextTax%", String.valueOf(getCastle().getNewCastleTax()));
				html.replace("%maxTax%", String.valueOf(getMaxTaxRate()));
				player.sendPacket(html);
			}
			else if (actualCommand.equals("tax_set")) // tax rates
				// Control
			{
				int tax=0;
				if (!validatePrivileges(player, L2Clan.CP_CS_TAXES)) return;
				if (siegeBlocksFunction(player)) return;
				if (!val.isEmpty()) {
					tax = Integer.parseInt(val);
					if (tax > getMaxTaxRate()) {
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/chamberlain/chamberlain-toohightaxrate.htm");
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%maxTax%", String.valueOf(getMaxTaxRate()));
						player.sendPacket(html);
						return;
					}
					else
					{
						if (getCastle().checkTaxPercent(player, tax))
								getCastle().SetUpNewTax(tax);
						else
							tax=getCastle().getTaxPercent();
					}
						
				}
				else
					//player deliberately didn't press "cancel"
					getCastle().SetUpNewTax(0);
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-aftersettaxrate.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%nextTax%", tax);
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("manage_functions"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_SET_FUNCTIONS)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-manage.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("products"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-products.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcId%", String.valueOf(getNpcId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("functions"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				if (val.equals("tele"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_TELEPORT) == null)
						html.setFile("data/html/chamberlain/chamberlain-nac.htm");
					else
						html.setFile("data/html/clanHallManager/tele" + getCastle().getName()
								+ getCastle().getFunction(ClanHall.FUNC_TELEPORT).getLvl() + ".htm");
					sendHtmlMessage(player, html);
				}
				else if (val.equals("support"))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
						html.setFile("data/html/chamberlain/chamberlain-nac.htm");
					else
					{
						html.setFile("data/html/chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
						html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equals("back"))
					showMessageWindow(player);
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/chamberlain-functions.htm");
					if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
						html.replace("%xp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl()));
					else
						html.replace("%xp_regen%", "0");
					if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
						html.replace("%hp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl()));
					else
						html.replace("%hp_regen%", "0");
					if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
						html.replace("%mp_regen%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl()));
					else
						html.replace("%mp_regen%", "0");
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equals("manage"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_SET_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				if (val.equals("recovery"))
				{
					if (st.countTokens() >= 1)
					{
						if (getCastle().getOwnerId() == 0)
						{
							player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_CASTLE));
							return;
						}
						val = st.nextToken();
						if (val.equals("hp_cancel"))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-cancel.htm");
							html.replace("%apply%", "recovery hp 0");
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("mp_cancel"))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-cancel.htm");
							html.replace("%apply%", "recovery mp 0");
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("exp_cancel"))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-cancel.htm");
							html.replace("%apply%", "recovery exp 0");
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("edit_hp"))
						{
							val = st.nextToken();
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-apply.htm");
							html.replace("%name%", "Камин (Устройство Восстановления HP)");
							int percent = Integer.valueOf(val);
							int cost;
							switch (percent)
							{
								case 80:
									cost = Config.CS_HPREG1_FEE;
									break;
								case 120:
									cost = Config.CS_HPREG2_FEE;
									break;
								case 180:
									cost = Config.CS_HPREG3_FEE;
									break;
								case 240:
									cost = Config.CS_HPREG4_FEE;
									break;
								default: // 300
									cost = Config.CS_HPREG5_FEE;
								break;
							}
							html.replace("%cost%", String.valueOf(cost) + "</font>Аден /"
									+ String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней</font>)");
							html.replace("%use%", "Предоставляет дополнительное восстановление HP участникам клана в замке.<font color=\"00FFFF\">"
									+ String.valueOf(percent) + "%</font>");
							html.replace("%apply%", "recovery hp " + String.valueOf(percent));
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("edit_mp"))
						{
							val = st.nextToken();
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-apply.htm");
							html.replace("%name%", "Ковер (Восстановление MP)");
							int percent = Integer.valueOf(val);
							int cost;
							switch (percent)
							{
								case 5:
									cost = Config.CS_MPREG1_FEE;
									break;
								case 15:
									cost = Config.CS_MPREG2_FEE;
									break;
								case 30:
									cost = Config.CS_MPREG3_FEE;
									break;
								default: // 40
									cost = Config.CS_MPREG4_FEE;
								break;
							}
							html.replace("%cost%", String.valueOf(cost) + "</font>Аден /"
									+ String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней</font>)");
							html.replace("%use%", "Предоставляет дополнительное восстановление MP участникам клана в замке.<font color=\"00FFFF\">"
									+ String.valueOf(percent) + "%</font>");
							html.replace("%apply%", "recovery mp " + String.valueOf(percent));
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("edit_exp"))
						{
							val = st.nextToken();
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-apply.htm");
							html.replace("%name%", "Люстра (Устройство Восстановления EXP)");
							int percent = Integer.valueOf(val);
							int cost;
							switch (percent)
							{
								case 15:
									cost = Config.CS_EXPREG1_FEE;
									break;
								case 25:
									cost = Config.CS_EXPREG2_FEE;
									break;
								case 35:
									cost = Config.CS_EXPREG3_FEE;
									break;
								default: // 50
									cost = Config.CS_EXPREG4_FEE;
								break;
							}
							html.replace("%cost%", String.valueOf(cost) + "</font>Аден /"
									+ String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней</font>)");
							html.replace("%use%", "Восстанавливает Exp любого участника клана, который возрожден в замке.<font color=\"00FFFF\">"
									+ String.valueOf(percent) + "%</font>");
							html.replace("%apply%", "recovery exp " + String.valueOf(percent));
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("hp"))
						{
							if (st.countTokens() >= 1)
							{
								int fee;
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/chamberlain/functions-apply_confirmed.htm");
								if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
								{
									if (getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl() == Integer.valueOf(val))
									{
										html.setFile("data/html/chamberlain/functions-used.htm");
										html.replace("%val%", String.valueOf(val) + "%");
										sendHtmlMessage(player, html);
										return;
									}
								}
								int percent = Integer.valueOf(val);
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/chamberlain/functions-cancel_confirmed.htm");
										break;
									case 80:
										fee = Config.CS_HPREG1_FEE;
										break;
									case 120:
										fee = Config.CS_HPREG2_FEE;
										break;
									case 180:
										fee = Config.CS_HPREG3_FEE;
										break;
									case 240:
										fee = Config.CS_HPREG4_FEE;
										break;
									default: // 300
										fee = Config.CS_HPREG5_FEE;
									break;
								}
								if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_HP, percent, fee, Config.CS_HPREG_FEE_RATIO,
										(getCastle().getFunction(Castle.FUNC_RESTORE_HP) == null)))
								{
									html.setFile("data/html/chamberlain/low_adena.htm");
									sendHtmlMessage(player, html);
								}
								sendHtmlMessage(player, html);
							}
							return;
						}
						else if (val.equals("mp"))
						{
							if (st.countTokens() >= 1)
							{
								int fee;
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/chamberlain/functions-apply_confirmed.htm");
								if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
								{
									if (getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl() == Integer.valueOf(val))
									{
										html.setFile("data/html/chamberlain/functions-used.htm");
										html.replace("%val%", String.valueOf(val) + "%");
										sendHtmlMessage(player, html);
										return;
									}
								}
								int percent = Integer.valueOf(val);
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/chamberlain/functions-cancel_confirmed.htm");
										break;
									case 5:
										fee = Config.CS_MPREG1_FEE;
										break;
									case 15:
										fee = Config.CS_MPREG2_FEE;
										break;
									case 30:
										fee = Config.CS_MPREG3_FEE;
										break;
									default: // 40
										fee = Config.CS_MPREG4_FEE;
									break;
								}
								if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_MP, percent, fee, Config.CS_MPREG_FEE_RATIO,
										(getCastle().getFunction(Castle.FUNC_RESTORE_MP) == null)))
								{
									html.setFile("data/html/chamberlain/low_adena.htm");
									sendHtmlMessage(player, html);
								}
								sendHtmlMessage(player, html);
							}
							return;
						}
						else if (val.equals("exp"))
						{
							if (st.countTokens() >= 1)
							{
								int fee;
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/chamberlain/functions-apply_confirmed.htm");
								if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
								{
									if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl() == Integer.valueOf(val))
									{
										html.setFile("data/html/chamberlain/functions-used.htm");
										html.replace("%val%", String.valueOf(val) + "%");
										sendHtmlMessage(player, html);
										return;
									}
								}
								int percent = Integer.valueOf(val);
								switch (percent)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/chamberlain/functions-cancel_confirmed.htm");
										break;
									case 15:
										fee = Config.CS_EXPREG1_FEE;
										break;
									case 25:
										fee = Config.CS_EXPREG2_FEE;
										break;
									case 35:
										fee = Config.CS_EXPREG3_FEE;
										break;
									default: // 50
										fee = Config.CS_EXPREG4_FEE;
									break;
								}
								if (!getCastle().updateFunctions(player, Castle.FUNC_RESTORE_EXP, percent, fee, Config.CS_EXPREG_FEE_RATIO,
										(getCastle().getFunction(Castle.FUNC_RESTORE_EXP) == null)))
								{
									html.setFile("data/html/chamberlain/low_adena.htm");
									sendHtmlMessage(player, html);
								}
								sendHtmlMessage(player, html);
							}
							return;
						}
					}
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/edit_recovery.htm");
					String hp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
					String exp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
					String mp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
					if (getCastle().getFunction(Castle.FUNC_RESTORE_HP) != null)
					{
						html.replace("%hp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLvl())
								+ "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getLease())
								+ "</font>Аден /" + String.valueOf(Config.CS_HPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней)");
						html.replace("%hp_period%", "Дата взымания арендной платы за функцию "
								+ format.format(getCastle().getFunction(Castle.FUNC_RESTORE_HP).getEndTime()));
						html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Отключить</a>]" + hp);
					}
					else
					{
						html.replace("%hp_recovery%", "нет");
						html.replace("%hp_period%", "нет");
						html.replace("%change_hp%", hp);
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_EXP) != null)
					{
						html.replace("%exp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLvl())
								+ "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getLease())
								+ "</font>Аден /" + String.valueOf(Config.CS_EXPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней)");
						html.replace("%exp_period%", "Дата взымания арендной платы за функцию "
								+ format.format(getCastle().getFunction(Castle.FUNC_RESTORE_EXP).getEndTime()));
						html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Отключить</a>]" + exp);
					}
					else
					{
						html.replace("%exp_recovery%", "нет");
						html.replace("%exp_period%", "нет");
						html.replace("%change_exp%", exp);
					}
					if (getCastle().getFunction(Castle.FUNC_RESTORE_MP) != null)
					{
						html.replace("%mp_recovery%", String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLvl())
								+ "%</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getLease())
								+ "</font>Аден /" + String.valueOf(Config.CS_MPREG_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней)");
						html.replace("%mp_period%", "Дата взымания арендной платы за функцию "
								+ format.format(getCastle().getFunction(Castle.FUNC_RESTORE_MP).getEndTime()));
						html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Отключить</a>]" + mp);
					}
					else
					{
						html.replace("%mp_recovery%", "нет");
						html.replace("%mp_period%", "нет");
						html.replace("%change_mp%", mp);
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equals("other"))
				{
					if (st.countTokens() >= 1)
					{
						if (getCastle().getOwnerId() == 0)
						{
							player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_TO_CONTROLL_CASTLE));
							return;
						}
						val = st.nextToken();
						if (val.equals("tele_cancel"))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-cancel.htm");
							html.replace("%apply%", "other tele 0");
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("support_cancel"))
						{
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-cancel.htm");
							html.replace("%apply%", "other support 0");
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("edit_support"))
						{
							val = st.nextToken();
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-apply.htm");
							html.replace("%name%", "Знаки отличия (Дополнительная Магия)");
							int stage = Integer.valueOf(val);
							int cost;
							switch (stage)
							{
								case 1:
									cost = Config.CS_SUPPORT1_FEE;
									break;
								case 2:
									cost = Config.CS_SUPPORT2_FEE;
									break;
								case 3:
									cost = Config.CS_SUPPORT3_FEE;
									break;
								default:
									cost = Config.CS_SUPPORT4_FEE;
								break;
							}
							html.replace("%cost%", String.valueOf(cost) + "</font>Аден /"
									+ String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней</font>)");
							html.replace("%use%", "Позволяет воспользоваться дополнительной магией.");
							html.replace("%apply%", "other support " + String.valueOf(stage));
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("edit_tele"))
						{
							val = st.nextToken();
							NpcHtmlMessage html = new NpcHtmlMessage(1);
							html.setFile("data/html/chamberlain/functions-apply.htm");
							html.replace("%name%", "Зеркало (Устройство Телепортации)");
							int stage = Integer.valueOf(val);
							int cost;
							switch (stage)
							{
								case 1:
									cost = Config.CS_TELE1_FEE;
									break;
								default:
									cost = Config.CS_TELE2_FEE;
								break;
							}
							html.replace("%cost%", String.valueOf(cost) + "</font>Аден /" + String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24)
									+ " Lytq</font>)");
							html.replace("%use%", "Телепортирует участников клана в замке к цели <font color=\"00FFFF\">Стадия " + String.valueOf(stage)
									+ "</font> - го уровня");
							html.replace("%apply%", "other tele " + String.valueOf(stage));
							sendHtmlMessage(player, html);
							return;
						}
						else if (val.equals("tele"))
						{
							if (st.countTokens() >= 1)
							{
								int fee;
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/chamberlain/functions-apply_confirmed.htm");
								if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
								{
									if (getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl() == Integer.valueOf(val))
									{
										html.setFile("data/html/chamberlain/functions-used.htm");
										html.replace("%val%", "Stage " + String.valueOf(val));
										sendHtmlMessage(player, html);
										return;
									}
								}
								int lvl = Integer.valueOf(val);
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/chamberlain/functions-cancel_confirmed.htm");
										break;
									case 1:
										fee = Config.CS_TELE1_FEE;
										break;
									default:
										fee = Config.CS_TELE2_FEE;
									break;
								}
								if (!getCastle().updateFunctions(player, Castle.FUNC_TELEPORT, lvl, fee, Config.CS_TELE_FEE_RATIO,
										(getCastle().getFunction(Castle.FUNC_TELEPORT) == null)))
								{
									html.setFile("data/html/chamberlain/low_adena.htm");
									sendHtmlMessage(player, html);
								}
								sendHtmlMessage(player, html);
							}
							return;
						}
						else if (val.equals("support"))
						{
							if (st.countTokens() >= 1)
							{
								int fee;
								val = st.nextToken();
								NpcHtmlMessage html = new NpcHtmlMessage(1);
								html.setFile("data/html/chamberlain/functions-apply_confirmed.htm");
								if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
								{
									if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == Integer.valueOf(val))
									{
										html.setFile("data/html/chamberlain/functions-used.htm");
										html.replace("%val%", "Стадия " + String.valueOf(val));
										sendHtmlMessage(player, html);
										return;
									}
								}
								int lvl = Integer.valueOf(val);
								switch (lvl)
								{
									case 0:
										fee = 0;
										html.setFile("data/html/chamberlain/functions-cancel_confirmed.htm");
										break;
									case 1:
										fee = Config.CS_SUPPORT1_FEE;
										break;
									case 2:
										fee = Config.CS_SUPPORT2_FEE;
										break;
									case 3:
										fee = Config.CS_SUPPORT3_FEE;
										break;
									default:
										fee = Config.CS_SUPPORT4_FEE;
									break;
								}
								if (!getCastle().updateFunctions(player, Castle.FUNC_SUPPORT, lvl, fee, Config.CS_SUPPORT_FEE_RATIO,
										(getCastle().getFunction(Castle.FUNC_SUPPORT) == null)))
								{
									html.setFile("data/html/chamberlain/low_adena.htm");
									sendHtmlMessage(player, html);
								}
								else
									sendHtmlMessage(player, html);
							}
							return;
						}
					}
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/edit_other.htm");
					String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Ур. 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Ур. 2</a>]";
					String support = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Ур. 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Ур. 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Ур. 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Ур. 4</a>]";
					if (getCastle().getFunction(Castle.FUNC_TELEPORT) != null)
					{
						html.replace("%tele%", "Стадия " + String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLvl())
								+ "</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_TELEPORT).getLease())
								+ "</font>Аден /" + String.valueOf(Config.CS_TELE_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней)");
						html.replace("%tele_period%", "Дата взымания арендной платы за функцию за функцию"
								+ format.format(getCastle().getFunction(Castle.FUNC_TELEPORT).getEndTime()));
						html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Отключить</a>]" + tele);
					}
					else
					{
						html.replace("%tele%", "нет");
						html.replace("%tele_period%", "нет");
						html.replace("%change_tele%", tele);
					}
					if (getCastle().getFunction(Castle.FUNC_SUPPORT) != null)
					{
						html.replace("%support%", "Стадия " + String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl())
								+ "</font> (<font color=\"FFAABB\">" + String.valueOf(getCastle().getFunction(Castle.FUNC_SUPPORT).getLease())
								+ "</font>Аден /" + String.valueOf(Config.CS_SUPPORT_FEE_RATIO / 1000 / 60 / 60 / 24) + " Дней)");
						html.replace("%support_period%", "Дата взымания арендной платы за функцию "
								+ format.format(getCastle().getFunction(Castle.FUNC_SUPPORT).getEndTime()));
						html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Отключить</a>]" + support);
					}
					else
					{
						html.replace("%support%", "нет");
						html.replace("%support_period%", "нет");
						html.replace("%change_support%", support);
					}
					sendHtmlMessage(player, html);
				}
				else if (val.equals("back"))
					showMessageWindow(player);
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/manage.htm");
					sendHtmlMessage(player, html);
				}
				return;
			}
			else if (actualCommand.equals("support"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				setTarget(player);
				L2Skill skill;
				if (val.isEmpty())
					return;

				try
				{
					int skill_id = Integer.parseInt(val);
					try
					{
						if (getCastle().getFunction(Castle.FUNC_SUPPORT) == null)
							return;
						if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
							return;
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						int skill_lvl = 0;
						if (st.countTokens() >= 1)
							skill_lvl = Integer.parseInt(st.nextToken());
						skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);
						if (skill.getSkillType() == L2SkillType.SUMMON)
							player.doSimultaneousCast(skill);
						else
						{
							if (!((skill.getMpConsume() + skill.getMpInitialConsume()) > getStatus().getCurrentMp()))
								this.doCast(skill);
							else
							{
								html.setFile("data/html/chamberlain/support-no_mana.htm");
								html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
								sendHtmlMessage(player, html);
								return;
							}
						}
						if (Config.CLASSIC_BUFF_MODE)
						{
							html.setFile("data/html/chamberlain/support-done.htm");
							html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
							sendHtmlMessage(player, html);
						}
						else
						{
							if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
								return;
							html.setFile("data/html/chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
							html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
							sendHtmlMessage(player, html);
						}
					}
					catch (Exception e)
					{
						player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_WRONG_SKILL_LEVEL_CONTACT_ADMINISTRATOR));
					}
				}
				catch (Exception e)
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_WRONG_SKILL_LEVEL_CONTACT_ADMINISTRATOR));
				}
				return;
			}
			else if (actualCommand.equals("support_back"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				if (getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() == 0)
					return;
				html.setFile("data/html/chamberlain/support" + getCastle().getFunction(Castle.FUNC_SUPPORT).getLvl() + ".htm");
				html.replace("%mp%", String.valueOf((int) getStatus().getCurrentMp()));
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equals("goto"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS)) return;
				if (siegeBlocksFunction(player)) return;
				int whereTo = Integer.parseInt(val);
				doTeleport(player, whereTo);
				return;
			}
			else if (actualCommand.equals("siege_change")) // siege day set
			{
				if (Config.CHANGE_SIEGE_TIME_IS_DISABLES)
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_CHANGE_SIEGE_TIME));
					return;
				}
				if (!validatePrivileges(player, L2Clan.CP_CS_MANAGE_SIEGE))
					return;
				if (siegeBlocksFunction(player))
					return;
				if (Config.CL_SET_SIEGE_TIME_LIST.isEmpty())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/chamberlain-noadmin.htm");
					sendHtmlMessage(player, html);
				}
				else if (getCastle().getSiege().getTimeRegistrationOverDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/siegetime1.htm");
					sendHtmlMessage(player, html);
				}
				else if (getCastle().getSiege().getIsTimeRegistrationOver())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/siegetime2.htm");
					sendHtmlMessage(player, html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/siegetime3.htm");
					html.replace("%time%", String.valueOf(getCastle().getSiegeDate().getTime()));
					sendHtmlMessage(player, html);
				}
			}
			else if (actualCommand.equals("siege_time_set")) // set preDay
			{
				if (Config.CHANGE_SIEGE_TIME_IS_DISABLES)
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_CHANGE_SIEGE_TIME));
					return;
				}
				if (!validatePrivileges(player, L2Clan.CP_CS_MANAGE_SIEGE))
					return;
				if (siegeBlocksFunction(player))
					return;
				boolean isAfternoon = Config.SIEGE_HOUR_LIST_MORNING.isEmpty();
				switch (Integer.parseInt(val))
				{
					case 0:
					case 4:
						break;
					case 1:
						_preDay = Integer.parseInt(st.nextToken());
						break;
					case 2:
						isAfternoon = Boolean.parseBoolean(st.nextToken());
						break;
					case 3:
						_preHour = Integer.parseInt(st.nextToken());
						break;
					default:
						break;
				}
				NpcHtmlMessage html = getNextSiegeTimePage(Integer.parseInt(val),isAfternoon);

				if (html == null)
				{
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("day"))
						getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, _preDay);
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("hour"))
						getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, _preHour);
					if (Config.CL_SET_SIEGE_TIME_LIST.contains("minute"))
						getCastle().getSiegeDate().set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));
					// Now store the changed time and finished next Siege Time registration
					getCastle().getSiege().endTimeRegistration(false);

					html = new NpcHtmlMessage(1);
					html.setFile("data/html/chamberlain/siegetime8.htm");
					html.replace("%time%", String.valueOf(getCastle().getSiegeDate().getTime()));
				}
				sendHtmlMessage(player, html);
			}
			else if (actualCommand.equals("give_crown"))
			{
				if (siegeBlocksFunction(player)) return;
				if (player.isClanLeader() && player.getInventory().getItemByItemId(6841) == null)
				{
					player.addItem("Chamberlain - Crown", 6841, 1, this, true, true);
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-gavecrown.htm");
					html.replace("%CharName%", String.valueOf(player.getName()));
					html.replace("%FeudName%", String.valueOf(getCastle().getName()));
					player.sendPacket(html);
					return;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-hascrown.htm");
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equals("give_LotMCoA"))
			{
				if (!validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS))
					return;
				if (siegeBlocksFunction(player))
					return;
				int valbuy = 63880 + getCastle().getCastleId();
				showBuyWindow(player, valbuy);
				return;
			}
			super.onBypassFeedback(player, command);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/chamberlain/chamberlain-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
				filename = "data/html/chamberlain/chamberlain-busy.htm"; // Busy because of siege
			else if (condition == COND_OWNER) // Clan owns castle
				filename = "data/html/chamberlain/chamberlain.htm"; // Owner message window
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private NpcHtmlMessage getNextSiegeTimePage(int now, boolean isAfternoon)
	{
		NpcHtmlMessage ret = new NpcHtmlMessage(1);
		if (now == 0 && Config.CL_SET_SIEGE_TIME_LIST.contains("day"))
		{
			ret.setFile("data/html/chamberlain/siegetime4.htm");
			return ret;
		}
		if (now < 3 && Config.CL_SET_SIEGE_TIME_LIST.contains("hour"))
		{
			switch (now)
			{
				case 0:
				case 1:
					if (!Config.SIEGE_HOUR_LIST_MORNING.isEmpty() && !Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
					{
						ret.setFile("data/html/chamberlain/siegetime5.htm");
						return ret;
					}
					break;
				case 2:
					ret.setFile("data/html/chamberlain/siegetime6.htm");
					List<Integer> list;
					int inc = 0;
					String ampm = "";

					if (!isAfternoon)
					{
						if (Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
							ampm = "AM";
						list = Config.SIEGE_HOUR_LIST_MORNING;
					}
					else
					{
						if (Config.SIEGE_HOUR_LIST_MORNING.isEmpty())
							ampm = "PM";
						inc = 12;
						list = Config.SIEGE_HOUR_LIST_AFTERNOON;
					}
					TextBuilder tList = new TextBuilder();
					for (Integer hour : list)
					{
						if (hour == 0)
							tList.append("<a action=\"bypass -h npc_%objectId%_siege_time_set 3 " + String.valueOf(hour + inc) + "\">" + String.valueOf(hour + 12) + ":00 " + ampm + "</a><br>");
						else
							tList.append("<a action=\"bypass -h npc_%objectId%_siege_time_set 3 " + String.valueOf(hour + inc) + "\">" + String.valueOf(hour) + ":00 " + ampm + "</a><br>");
					}
					ret.replace("%links%", tList.toString());
			}
			return ret;
		}
		if (now < 4 && Config.CL_SET_SIEGE_TIME_LIST.contains("minute"))
		{
			ret.setFile("data/html/chamberlain/siegetime7.htm");
			return ret;
		}

		return null;
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.reduceAdena("Телепортировать", list.getPrice(), this, true))
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
		}
		else
			_log.warn("No teleport destination with id:" + val);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private boolean siegeBlocksFunction(L2PcInstance player)
	{
		if (getCastle().getSiege().getIsInProgress()) {
			player.sendPacket(IN_SIEGE);
			return true;
		}
		return false;
	}

	private boolean validatePrivileges(L2PcInstance player, int privilege) {
		if ((player.getClanPrivileges() & privilege) != privilege) {
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		return true;
	}

	private int getMaxTaxRate()
	{
		int maxTax = 15;
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
		}
		return maxTax;
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (getNpcId()==35419)
		{
			if(player.getClan() != null && ClanHallManager.getInstance().getClanHallById(34).getOwnerId()==player.getClanId() && validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS))
				return COND_OWNER; // Owner	
		}
		else if (getNpcId()==35638)
		{
			if(player.getClan() != null && ClanHallManager.getInstance().getClanHallById(64).getOwnerId()==player.getClanId() && validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS))
				return COND_OWNER; // Owner	
		}
		else if (getCastle() != null && getCastle().getCastleId() > 0 && player.getClan() != null && getCastle().getOwnerId() == player.getClanId() && validatePrivileges(player, L2Clan.CP_CS_USE_FUNCTIONS))
			return COND_OWNER; // Owner

		return COND_ALL_FALSE;
	}
}
