package quest.greater_stigma;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi
 */
public class _30217GroupStigmasScars extends AbstractQuestHandler {

	public _30217GroupStigmasScars() {
		super(30217);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798909).addOnQuestStart(questId);
		qe.registerQuestNpc(798909).addOnTalkEvent(questId);
		qe.registerQuestNpc(799506).addOnTalkEvent(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798909) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 798941:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							spawnForFiveMinutesInFrontOf(799506, player, 1.5f);
							qs.setQuestVarById(0, 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
					}
					return false;
				case 798909:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							long itemCount1 = player.getInventory().getItemCountByItemId(182209618);
							long itemCount2 = player.getInventory().getItemCountByItemId(182209619);
							if (var == 2) {
								if (itemCount1 > 0 && itemCount2 > 0) {
									removeQuestItem(env, 182209618, 1);
									removeQuestItem(env, 182209619, 1);
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
									return sendQuestDialog(env, 1693);
								} else
									return sendQuestDialog(env, 10001);
							}
					}
					return false;
				case 799506:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO2:
							env.getVisibleObject().getController().delete();
							qs.setQuestVarById(0, 2);
							updateQuestStatus(env);
							return true;
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798909) {
				if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}