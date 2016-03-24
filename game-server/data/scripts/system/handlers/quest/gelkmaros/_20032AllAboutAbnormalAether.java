package quest.gelkmaros;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @Author Majka
 */
public class _20032AllAboutAbnormalAether extends QuestHandler {

	private final static int questId = 20032;
	
	// Angrad						ID: 799247
	// Eddas						ID: 799250
	// Taloc's Guardian	ID: 799325
	// Taloc's Mirage		ID: 799503
	private final static int[] npcs = { 799247, 799250, 799325 };

	public _20032AllAboutAbnormalAether() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(215488).addOnKillEvent(questId); // Celestius
		qe.registerQuestItem(182215592, questId); // quest_20032a
		qe.registerQuestItem(182215593, questId); // quest_20032b
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799247) { // Angrad
				if (var == 0) {
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, var, var+1); // 1
					}
				}
			} else if (targetId == 799250) { // Eddas
				if (var == 1) {
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							return defaultCloseDialog(env, var, var+1); // 2
					}
				}
			} else if (targetId == 799325) { // Taloc's Guardian
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
						break;
					}
					case SETPRO3:
						if (giveQuestItem(env, 182215592, 1) && giveQuestItem(env, 182215593, 1)) {
							changeQuestStep(env, 2, 3, false); // 3
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300190000);
							InstanceService.registerPlayerWithInstance(newInstance, player);
							TeleportService2.teleportTo(player, 300190000, newInstance.getInstanceId(), 202.26694f, 226.0532f, 1098.236f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							return closeDialogWindow(env);
						} else {
							PacketSendUtility.sendPacket(player, STR_MSG_FULL_INVENTORY());
							return sendQuestSelectionDialog(env);
						}
				}
			} 
			/* To check on retail if there is a dedicate portal to exit.
			else if (targetId == 799503) { // Taloc's Mirage
				switch (dialog) {
					case USE_OBJECT:
						if (var == 7) {
							return sendQuestDialog(env, 3057);
						}
						break;
					case SETPRO7: {
						qs.setQuestVar(8); // Reward
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						TeleportService2.teleportTo(env.getPlayer(), 220070000, 1025, 2782, 388, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						return closeDialogWindow(env);
					}
				}
			}*/
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799247) { // Angrad
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 215488: { // Celestius
					if(var == 6) {
						playQuestMovie(env, 437);
						return defaultOnKillEvent(env, 215488, var, var+1); // 7
					}
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 300190000) {
				int itemId = item.getItemId();
				int var = qs.getQuestVarById(0);
				int var1 = qs.getQuestVarById(1);
				if (itemId == 182215593) { // quest_20032b
					changeQuestStep(env, 4, 5, false); // 7
					return HandlerResult.SUCCESS; // //TODO: Should return FAILED (not removed, but skill still should be used)
				}
				else if (itemId == 182215592) { // quest_20032a
					if (var == 5) {
						if (var1 >= 0 && var1 < 19) {
							changeQuestStep(env, var1, var1 + 1, false, 1); // 3: 19
							return HandlerResult.SUCCESS;
						}
						else if (var1 == 19) {
							qs.setQuestVar(6);
							updateQuestStatus(env);
							return HandlerResult.SUCCESS;
						}
					}
				}
			}
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 300190000) {
				qs.setQuestVar(4);
				updateQuestStatus(env);
				return true;
			} else {
				removeQuestItem(env, 182215592, 1);
				removeQuestItem(env, 182215593, 1);
				
				int var = qs.getQuestVarById(0);
				if (var > 2 && var < 7) {
					qs.setQuestVar(2);
					updateQuestStatus(env);
					return true;
				}
				else if (var == 7) { // Final boss killed
					qs.setQuestVar(8); // Reward
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onDieEvent(QuestEnv env) {
		return restoreStep(env);
	}
	
	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return restoreStep(env);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}
	
	private boolean restoreStep(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 4 && var < 6) {
				removeQuestItem(env, 182215592, 1);
				removeQuestItem(env, 182215593, 1);
				qs.setQuestVar(2);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
