package com.mygdx.game.Components;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Quests.Quest;

public class DataQuest {
    public String type;
    public Quest quest;
//    LocQuest
    public Vector2 loc;
    public float radius;
//    KillQuest
    public Pirate target;

    public DataQuest(String type, Quest quest, Vector2 loc, float radius) {
        this.type = type;
        this.quest = quest;
        this.loc = loc;
        this.radius = radius;
        this.target = null;
    }
    public DataQuest(String type, Quest quest, Pirate target) {
        this.type = type;
        this.quest = quest;
        this.loc = null;
        this.radius = 0;
        this.target = target;
    }
}
