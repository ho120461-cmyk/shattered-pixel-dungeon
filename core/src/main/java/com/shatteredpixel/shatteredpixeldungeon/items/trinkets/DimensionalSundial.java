/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import com.watabou.utils.GameSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DimensionalSundial extends Trinket {

	{
		image = ItemSpriteSheet.SUNDIAL;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this,
					"stats_desc",
					(int)(100*(1f - enemySpawnMultiplierDaytime(buffedLvl()))),
					(int)(100*(enemySpawnMultiplierNighttime(buffedLvl())-1f)));
		} else {
			return Messages.get(this, "typical_stats_desc",
					(int)(100*(1f - enemySpawnMultiplierDaytime(0))),
					(int)(100*(enemySpawnMultiplierNighttime(0)-1f)));
		}
	}

	//MOD: force night toggle, saved in game settings (persists across runs)
	public static final String AC_NIGHT = "NIGHT";
	private static final String KEY_FORCE_NIGHT = "mod_sundial_force_night";

	public static boolean forceNight(){
		return GameSettings.getBoolean(KEY_FORCE_NIGHT, true);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_NIGHT);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_NIGHT)){
			return forceNight() ? "밤 고정 끄기" : "밤 고정 켜기";
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_NIGHT)){
			boolean on = !forceNight();
			GameSettings.put(KEY_FORCE_NIGHT, on);
			GLog.i(on ? "해시계: 밤 고정 켜짐" : "해시계: 실제 시간 사용");
		}
	}

	public static boolean sundialWarned = false;

	public static float spawnMultiplierAtCurrentTime(){
		if (trinketLevel(DimensionalSundial.class) != -1) {
			Calendar cal = GregorianCalendar.getInstance();
			if (forceNight() || cal.get(Calendar.HOUR_OF_DAY) >= 20 || cal.get(Calendar.HOUR_OF_DAY) <= 7) {
				if (!sundialWarned){
					GLog.w(Messages.get(DimensionalSundial.class, "warning"));
					sundialWarned = true;
				}
				return enemySpawnMultiplierNighttime();
			} else {
				return enemySpawnMultiplierDaytime();
			}
		} else {
			return 1f;
		}
	}

	public static float enemySpawnMultiplierDaytime(){
		return enemySpawnMultiplierDaytime(trinketLevel(DimensionalSundial.class));
	}

	public static float enemySpawnMultiplierDaytime( int level ){
		if (level == -1){
			return 1f;
		} else {
			return 0.95f - 0.05f*level;
		}
	}

	public static float enemySpawnMultiplierNighttime(){
		return enemySpawnMultiplierNighttime(trinketLevel(DimensionalSundial.class));
	}

	public static float enemySpawnMultiplierNighttime( int level ){
		if (level == -1){
			return 1f;
		} else {
			return 1.25f + 0.25f*level;
		}
	}
}
