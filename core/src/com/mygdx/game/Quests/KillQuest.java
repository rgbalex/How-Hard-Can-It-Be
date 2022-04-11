package com.mygdx.game.Quests;

import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Entitys.Entity;
import com.mygdx.game.Entitys.Player;

/**
 * A Quest to kill a college is only complete once that college is dead
 */
public class KillQuest extends Quest {

    public KillQuest() {
        super();
        name = "Kill the college";
        description = "KILL KILL KILL";
        reward = 100;
    }

    public KillQuest(Pirate target) {
        this();
        this.target = target;
        description = target.getFaction().getName();
    }

    public KillQuest(Entity target) {
        this(target.getComponent(Pirate.class));
    }

    @Override
    public boolean checkCompleted(Player p) {
        isCompleted = !target.isAlive();
        return isCompleted;
    }

    public Pirate getTarget() {
        return target;
    }
}
