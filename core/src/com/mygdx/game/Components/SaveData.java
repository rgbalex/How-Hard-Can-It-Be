package com.mygdx.game.Components;

import com.mygdx.game.Entitys.College;
import com.mygdx.game.Faction;
import com.mygdx.game.Quests.Quest;

import java.util.ArrayList;
/**
 * This class has been added for assesment 2.
 * It is used to store data about a game state for future reloading.
 * */
public class SaveData {
    public ArrayList<Faction> factions;
    public ArrayList<DataShip> ships;
    public ArrayList<DataCollege> colleges;
    public ArrayList<DataQuest> quests;
    public int health_level;
    public int ammo_level;
    public int speed_level;
    public int damage_level;
    public int health;
    public int plunder;
    public int cannon_balls;
    public int points;
    public float x;
    public float y;
}
