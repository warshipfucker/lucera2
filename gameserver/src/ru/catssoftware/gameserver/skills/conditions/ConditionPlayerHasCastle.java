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
package ru.catssoftware.gameserver.skills.conditions;

import ru.catssoftware.gameserver.model.L2Clan;
import ru.catssoftware.gameserver.model.actor.instance.L2PcInstance;
import ru.catssoftware.gameserver.skills.Env;

/**
 * @author MrPoke
 */
final class ConditionPlayerHasCastle extends Condition
{
	private final int _castle;

	public ConditionPlayerHasCastle(int castle)
	{
		_castle = castle;
	}

	/**
	 * @see ru.catssoftware.gameserver.skills.conditions.Condition#testImpl(ru.catssoftware.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player.isPlayer()))
			return false;

		L2Clan clan = ((L2PcInstance)env.player).getClan();
		if (clan == null)
			return _castle == 0;

		// Any castle
		if (_castle == -1)
			return clan.getHasCastle() > 0;

		return clan.getHasCastle() == _castle;
	}
}