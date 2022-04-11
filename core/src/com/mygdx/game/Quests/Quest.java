package com.mygdx.game.Quests;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Entitys.Player;

/**
 * Base class for all quests facilitates the checking of completion
 */
public abstract class Quest {
    protected String name;
    protected String description;
    protected int reward;
    protected boolean isCompleted;
    protected Pirate target;
    protected Vector2 loc;
    protected float radius;

    public Quest() {
        name = "";
        description = "";
        reward = 0;
        isCompleted = false;
        target = null;
        loc = new Vector2();
        radius = -1f;
    }

    /**
     * Checks if the given player has met the complete condition
     *
     * @param p the player
     * @return has completed
     */
    public abstract boolean checkCompleted(Player p);

    public int getReward() {
        return reward;
    }

    public boolean isCompleted() {
        return isCompleted;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Pirate getTarget() {
        return target;
    };

    public Vector2 getLoc() {
        return loc;
    }

    public float getRadius() {
        return radius;
    }
}
