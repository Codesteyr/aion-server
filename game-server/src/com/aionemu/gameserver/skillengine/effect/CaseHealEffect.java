package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author kecimis
 */
public class CaseHealEffect extends AbstractHealEffect {

	@XmlAttribute(name = "cond_value")
	protected int condValue;
	@XmlAttribute
	protected HealType type;

	@Override
	protected int getCurrentStatValue(Effect effect) {
		switch (type) {
			case HP:
				return effect.getEffected().getLifeStats().getCurrentHp();
			case MP:
				return effect.getEffected().getLifeStats().getCurrentMp();
			default:
				return 0;
		}
	}

	@Override
	protected int getMaxStatValue(Effect effect) {
		switch (type) {
			case HP:
				return effect.getEffected().getGameStats().getMaxHp().getCurrent();
			case MP:
				return effect.getEffected().getGameStats().getMaxMp().getCurrent();
			default:
				return 0;
		}
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		ActionObserver observer = effect.getActionObserver(position);
		if (observer != null)
			effect.getEffected().getObserveController().removeObserver(observer);
	}

	@Override
	public void startEffect(final Effect effect) {
		if (tryHeal(effect))
			return;
		ActionObserver observer = new ActionObserver(ObserverType.HP_CHANGED) {

			@Override
			public void hpChanged(int value) {
				tryHeal(effect);
			}

		};
		effect.getEffected().getObserveController().addObserver(observer);
		effect.setActionObserver(observer, position);
		
	}

	private boolean tryHeal(final Effect effect) {
		final int currentValue = getCurrentStatValue(effect);
		final int maxCurValue = getMaxStatValue(effect);
		if (currentValue <= (maxCurValue * condValue / 100f)) {
			// calculate heal to the effective moment
			int healValueWithDelta = value + delta * effect.getSkillLevel();
			int finalHeal = calculateHeal(effect, type, healValueWithDelta, currentValue, maxCurValue);

			// apply heal
			if (type == HealType.HP)
				effect.getEffected().getLifeStats().increaseHp(TYPE.HP, finalHeal, effect, LOG.CASEHEAL);
			else if (type == HealType.MP)
				effect.getEffected().getLifeStats().increaseMp(TYPE.MP, finalHeal, effect.getSkillId(), LOG.CASEHEAL);
			effect.endEffect();
			return true;
		}
		return false;
	}
}
