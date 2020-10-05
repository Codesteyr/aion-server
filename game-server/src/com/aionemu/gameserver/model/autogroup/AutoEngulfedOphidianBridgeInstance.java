package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.EngulfedOphidianBridgeReward;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.services.instance.periodic.EngulfedOphidianBridgeService;

/**
 * @author xTz
 */
public class AutoEngulfedOphidianBridgeInstance extends AutoInstance {

	@Override
	public AGQuestion addPlayer(Player player, SearchInstance searchInstance) {
		super.writeLock();
		try {
			if (!satisfyTime(searchInstance) || (players.size() >= getMaxPlayers())) {
				return AGQuestion.FAILED;
			}
			EntryRequestType ert = searchInstance.getEntryRequestType();
			List<AGPlayer> playersByRace = getAGPlayersByRace(player.getRace());
			if (ert.isGroupEntry()) {
				if (searchInstance.getMembers().size() + playersByRace.size() > 6) {
					return AGQuestion.FAILED;
				}
				for (Player member : player.getPlayerGroup().getOnlineMembers()) {
					if (searchInstance.getMembers().contains(member.getObjectId())) {
						players.put(member.getObjectId(), new AGPlayer(player));
					}
				}
			} else {
				if (playersByRace.size() >= 6) {
					return AGQuestion.FAILED;
				}
				players.put(player.getObjectId(), new AGPlayer(player));
			}
			return instance != null ? AGQuestion.ADDED : (players.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED);
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		List<Player> playersByRace = getPlayersByRace(player.getRace());
		playersByRace.remove(player);
		if (playersByRace.size() == 1 && !playersByRace.get(0).isInGroup()) {
			PlayerGroup newGroup = PlayerGroupService.createGroup(playersByRace.get(0), player, TeamType.AUTO_GROUP, 0);
			int groupId = newGroup.getObjectId();
			if (!instance.isRegistered(groupId)) {
				instance.register(groupId);
			}
		} else if (!playersByRace.isEmpty() && playersByRace.get(0).isInGroup()) {
			PlayerGroupService.addPlayer(playersByRace.get(0).getPlayerGroup(), player);
		}
		int objectId = player.getObjectId();
		if (!instance.isRegistered(objectId)) {
			instance.register(objectId);
		}
	}

	@Override
	public void onPressEnter(Player player) {
		super.onPressEnter(player);
		EngulfedOphidianBridgeService.getInstance().addCooldown(player);
		((EngulfedOphidianBridgeReward) instance.getInstanceHandler().getInstanceReward()).portToPosition(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
		PlayerGroupService.removePlayer(player);
	}
}