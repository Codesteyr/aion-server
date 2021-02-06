package com.aionemu.gameserver.model.stats.calc.functions;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;

/**
 * @author ATracer
 */
public class PlayerStatFunctions {

	private static final List<IStatFunction> FUNCTIONS = new ArrayList<>();

	static {
		FUNCTIONS.add(new PhysicalAttackFunction());
		FUNCTIONS.add(new MagicalAttackFunction());
		FUNCTIONS.add(new AttackSpeedFunction());
		FUNCTIONS.add(new BoostCastingTimeFunction());
		FUNCTIONS.add(new PvPAttackRatioFunction());
		FUNCTIONS.add(new PDefFunction());
		FUNCTIONS.add(new MaxHpFunction());
		FUNCTIONS.add(new MaxMpFunction());
		FUNCTIONS.add(new PvEAttackRatioFunction());
		FUNCTIONS.add(new PvEDefendRatioFunction());
	}

	public static final List<IStatFunction> getFunctions() {
		return FUNCTIONS;
	}

	public static final void addPredefinedStatFunctions(Player player) {
		player.getGameStats().addEffectOnly(null, FUNCTIONS);
	}
}

class PhysicalAttackFunction extends StatFunction {

	PhysicalAttackFunction() {
		stat = StatEnum.PHYSICAL_ATTACK;
	}

	@Override
	public void apply(Stat2 stat) {
		if (stat.getOwner() instanceof Player) {
			PlayerStatsTemplate pst = ((Player) stat.getOwner()).getGameStats().getStatsTemplate();
			if (stat.getBase() == pst.getAttack())
				return;
		}
		float power = stat.getOwner().getGameStats().getPower().getCurrent();
		stat.setBaseRate(power * 0.01f);
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

class MaxHpFunction extends StatFunction {

	MaxHpFunction() {
		stat = StatEnum.MAXHP;
	}

	@Override
	public void apply(Stat2 stat) {
		float health = stat.getOwner().getGameStats().getHealth().getCurrent();
		stat.setBaseRate(health * 0.01f);
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

class MaxMpFunction extends StatFunction {

	MaxMpFunction() {
		stat = StatEnum.MAXMP;
	}

	@Override
	public void apply(Stat2 stat) {
		float will = stat.getOwner().getGameStats().getWill().getCurrent();
		stat.setBaseRate(will * 0.01f);
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

class MagicalAttackFunction extends StatFunction {

	MagicalAttackFunction() {
		stat = StatEnum.MAGICAL_ATTACK;
	}

	@Override
	public void apply(Stat2 stat) {
		float knowledge = stat.getOwner().getGameStats().getKnowledge().getCurrent();
		stat.setBaseRate(knowledge * 0.01f);
	}

	@Override
	public int getPriority() {
		return 30;
	}
}

class PDefFunction extends StatFunction {

	PDefFunction() {
		stat = StatEnum.PHYSICAL_DEFENSE;
	}

	@Override
	public void apply(Stat2 stat) {
		if (stat.getOwner().isInFlyingState())
			stat.setBonus(stat.getBonus() - (stat.getBase() / 2));
	}

	@Override
	public int getPriority() {
		return 60;
	}
}

class AttackSpeedFunction extends DuplicateStatFunction {

	AttackSpeedFunction() {
		stat = StatEnum.ATTACK_SPEED;
	}

}

class BoostCastingTimeFunction extends DuplicateStatFunction {

	BoostCastingTimeFunction() {
		stat = StatEnum.BOOST_CASTING_TIME;
	}
}

class PvEAttackRatioFunction extends StatFunction {

	PvEAttackRatioFunction() {
		stat = StatEnum.PVE_ATTACK_RATIO;
	}

	@Override
	public void apply(Stat2 stat) {
		WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(stat.getOwner().getWorldId());
		stat.addToBonus(template.getPvEAttackRatio());
	}
}

class PvEDefendRatioFunction extends StatFunction {

	PvEDefendRatioFunction() {
		stat = StatEnum.PVE_DEFEND_RATIO;
	}

	@Override
	public void apply(Stat2 stat) {
		WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(stat.getOwner().getWorldId());
		stat.addToBonus(template.getPvEAttackRatio());
	}
}

class PvPAttackRatioFunction extends DuplicateStatFunction {

	PvPAttackRatioFunction() {
		stat = StatEnum.PVP_ATTACK_RATIO;
	}
}

class DuplicateStatFunction extends StatFunction {

	@Override
	public void apply(Stat2 stat) {
		Item mainWeapon = ((Player) stat.getOwner()).getEquipment().getMainHandWeapon();
		Item offWeapon = ((Player) stat.getOwner()).getEquipment().getOffHandWeapon();
		if (mainWeapon == offWeapon)
			offWeapon = null;

		if (mainWeapon != null) {
			StatFunction func1 = null;
			StatFunction func2 = null;
			List<StatFunction> functions = new ArrayList<>();
			List<StatFunction> functions1 = mainWeapon.getItemTemplate().getModifiers();

			if (functions1 != null) {
				List<StatFunction> f1 = getFunctions(functions1, stat, mainWeapon);
				if (!f1.isEmpty()) {
					func1 = f1.get(0);
					functions.addAll(f1);
				}
			}

			if (mainWeapon.hasFusionedItem()) {
				ItemTemplate template = mainWeapon.getFusionedItemTemplate();
				List<StatFunction> functions2 = template.getModifiers();
				if (functions2 != null) {
					List<StatFunction> f2 = getFunctions(functions2, stat, mainWeapon);
					if (!f2.isEmpty()) {
						func2 = f2.get(0);
						functions.addAll(f2);
					}
				}
			} else if (offWeapon != null) {
				List<StatFunction> functions2 = offWeapon.getItemTemplate().getModifiers();
				if (functions2 != null) {
					functions.addAll(getFunctions(functions2, stat, offWeapon));
				}
			}

			if (func1 != null && func2 != null) { // for fusioned weapons
				if (Math.abs(func1.getValue()) >= Math.abs(func2.getValue()))
					functions.remove(func2);
				else
					functions.remove(func1);
			}
			if (!functions.isEmpty()) {
				if (getName() == StatEnum.PVP_ATTACK_RATIO) {
					functions.forEach(f -> f.apply(stat));
				} else {
					functions.stream().max((f1, f2) -> Integer.compare(f1.getValue(), f2.getValue())).get().apply(stat);
				}
				functions.clear();
			}
		}
	}

	private List<StatFunction> getFunctions(List<StatFunction> list, Stat2 stat, Item item) {
		List<StatFunction> functions = new ArrayList<>();
		for (StatFunction func : list) {
			if (func.getName() == getName()) {
				StatFunctionProxy func2 = new StatFunctionProxy(item, func);
				if (func2.validate(stat))
					functions.add(func);
			}
		}
		return functions;
	}

	@Override
	public int getPriority() {
		return 60;
	}

}
