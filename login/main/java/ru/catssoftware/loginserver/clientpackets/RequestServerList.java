/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package ru.catssoftware.loginserver.clientpackets;

import ru.catssoftware.loginserver.network.serverpackets.LoginFailReason;
import ru.catssoftware.loginserver.network.serverpackets.ServerList;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int	_skey1;
	private int	_skey2;
	private int	_data3;

	/**
	 * @return
	 */
	public int getSessionKey1()
	{
		return _skey1;
	}

	/**
	 * @return
	 */
	public int getSessionKey2()
	{
		return _skey2;
	}

	/**
	 * @return
	 */
	public int getData3()
	{
		return _data3;
	}

	@Override
	public boolean readImpl()
	{
		if (this.getAvaliableBytes() >= 8)
		{
			_skey1 = readD(); // loginOk 1
			_skey2 = readD(); // loginOk 2
			return true;
		}
		else
			return false;
	}

	/**
	 * @see com.l2jserver.mmocore.network.ReceivablePacket#run()
	 */
	@Override
	public void run()
	{
		if (this.getClient().getSessionKey().checkPreLoginPair(_skey1)) {
			this.getClient().sendPacket(new ServerList(this.getClient(),true));
			try { Thread.sleep(20); } catch(InterruptedException e) { return; }
			this.getClient().sendPacket(new ServerList(this.getClient(),false));
		}
		else
			this.getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
	}
}