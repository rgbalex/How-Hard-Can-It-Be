package com.mygdx.game.Components;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Quests.Quest;
/**
 * This class has been added for assesment 2.
 * It is used to store data about a quest for future reloading.
 * */
public class DataQuest {
    public String type;
    public String name;
    public String description;
    public int reward;
    public boolean completed;
//    LocQuest
    public Vector2 loc = null;
    public float radius = 0f;
//    KillQuest
    public Pirate target = null;

    public DataQuest(String type, String name, String description, int reward, boolean completed, Pirate target) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.reward = reward;
        this.completed = completed;
//        this.target = target;
    }

    public DataQuest(String type, String name, String description, int reward, boolean completed, Vector2 loc, float radius) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.reward = reward;
        this.completed = completed;
        this.loc = loc;
        this.radius = radius;
    }
}
