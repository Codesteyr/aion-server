package ai.siege;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISummon;
import com.aionemu.gameserver.ai.handler.FollowEventHandler;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.SiegeWeaponController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;
import com.aionemu.gameserver.services.summons.SummonsService;

/**
 * @author xTz
 */
@AIName("siege_weapon")
public class SiegeWeaponAI extends AISummon {

	private long lastAttackTime;
	private int skill;
	private int skillLvl;
	private int duration;

	@Override
	protected void handleSpawned() {
		this.setStateIfNot(AIState.IDLE);
		SummonsService.doMode(SummonMode.GUARD, getOwner());
		NpcSkillTemplate skillTemplate = getNpcSkillTemplates().getNpcSkills().get(0);
		skill = skillTemplate.getSkillId();
		skillLvl = skillTemplate.getSkillLevel();
		duration = DataManager.SKILL_DATA.getSkillTemplate(this.skill).getDuration();
	}

	@Override
	protected void handleFollowMe(Creature creature) {
		this.setStateIfNot(AIState.FOLLOWING);
	}

	@Override
	protected void handleStopFollowMe(Creature creature) {
		this.setStateIfNot(AIState.IDLE);
		this.getOwner().getMoveController().abortMove();
	}

	@Override
	protected void handleTargetTooFar() {
		getOwner().getMoveController().moveToDestination();
	}

	@Override
	protected void handleMoveArrived() {
		this.getOwner().getController().onMove();
		this.getOwner().getMoveController().abortMove();
	}

	@Override
	protected void handleMoveValidate() {
		this.getOwner().getController().onMove();
		getMoveController().moveToTargetObject();
	}

	@Override
	protected SiegeWeaponController getController() {
		return (SiegeWeaponController) super.getController();
	}

	private NpcSkillTemplates getNpcSkillTemplates() {
		return getController().getNpcSkillTemplates();
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (creature == null) {
			return;
		}
		Race race = creature.getRace();
		Player master = getOwner().getMaster();
		if (master == null) {
			return;
		}
		Race masterRace = master.getRace();
		if (masterRace.equals(Race.ASMODIANS) && !race.equals(Race.PC_LIGHT_CASTLE_DOOR) && !race.equals(Race.DRAGON_CASTLE_DOOR)) {
			return;
		} else if (masterRace.equals(Race.ELYOS) && !race.equals(Race.PC_DARK_CASTLE_DOOR) && !race.equals(Race.DRAGON_CASTLE_DOOR)) {
			return;
		}
		if (!getOwner().getMode().equals(SummonMode.ATTACK)) {
			return;
		}

		if (System.currentTimeMillis() - lastAttackTime > duration + 2000) {
			lastAttackTime = System.currentTimeMillis();
			getOwner().getController().useSkill(skill, skillLvl);
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case DESTINATION_REACHED:
				return destinationReached();
			default:
				return super.ask(question);
		}
	}

	private boolean destinationReached() {
		AIState state = getState();
		switch (state) {
			case FOLLOWING:
				return FollowEventHandler.isInRange(this, getOwner().getTarget());
			default:
				AILogger.info(this, "[siege_weapon] calling destinationReached with unhandled state: " + state.toString());
				break;
		}
		return true;
	}
}