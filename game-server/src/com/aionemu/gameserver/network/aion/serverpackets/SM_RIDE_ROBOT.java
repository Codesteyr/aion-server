package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.event.AprilFoolsAppearance;

/**
 * @author Cheatkiller
 */
public class SM_RIDE_ROBOT extends AionServerPacket {

	private int robotId;
	private int objectId;

	public SM_RIDE_ROBOT(Player player) {
		this(player, AprilFoolsAppearance.getRobotId(player));
	}

	public SM_RIDE_ROBOT(Player player, int robotId) {
		this.objectId = player.getObjectId();
		this.robotId = robotId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeD(robotId);
	}
}
