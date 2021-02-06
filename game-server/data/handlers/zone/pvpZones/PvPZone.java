package zone.pvpZones;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_PvPZONE_MY_DEATH_TO_B;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_PvPZONE_OUT_MESSAGE;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.SiegeZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.handler.AdvancedZoneHandler;

/**
 * @author MrPoke
 */
public abstract class PvPZone implements AdvancedZoneHandler {

	@Override
	public void onEnterZone(Creature player, ZoneInstance zone) {
	}

	@Override
	public void onLeaveZone(Creature player, ZoneInstance zone) {
	}

	@Override
	public boolean onDie(Creature lastAttacker, Creature target, ZoneInstance zone) {
		if (!(target instanceof Player))
			return false;

		if (zone instanceof SiegeZoneInstance) {
			Player player = (Player) target;
			PacketSendUtility.broadcastToZone((SiegeZoneInstance) zone, STR_PvPZONE_OUT_MESSAGE(player.getName()));

			ThreadPoolManager.getInstance().schedule(() -> {
				PlayerReviveService.duelRevive(player);
				doTeleport(player, zone.getZoneTemplate().getName());
				PacketSendUtility.sendPacket(player, STR_MSG_PvPZONE_MY_DEATH_TO_B(lastAttacker.getName()));
			}, 5000);
		}
		return true;
	}

	protected abstract void doTeleport(Player player, ZoneName zoneName);
}