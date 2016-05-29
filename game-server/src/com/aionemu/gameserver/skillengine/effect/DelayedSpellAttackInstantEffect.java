package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelayedSpellAttackInstantEffect")
public class DelayedSpellAttackInstantEffect extends DamageEffect {

	@XmlAttribute
	protected int delay;

	@Override
	public void applyEffect(final Effect effect) {
		int skillLvl = effect.getSkillLevel();
		int valueWithDelta = value + delta * skillLvl;

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, true);// ignores shields on retail
		final int finalPosition = this.position;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				effect
					.getEffected()
					.getController()
					.onAttack(effect.getEffector(), effect.getSkillId(), TYPE.DELAYDAMAGE, effect.getReserveds(finalPosition).getValue(), true,
						LOG.DELAYEDSPELLATKINSTANT, effect.getAttackStatus());
				effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());
				effect.getEffector().getObserveController().notifyGodstoneObserver(effect.getEffected());
			}
		}, delay);
	}

	@Override
	public void calculateDamage(Effect effect) {

	}
}
