package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Represents a faction contains data largely sourced from GameSettings
 */
public class Faction {
    private String name;
    private String shipColour;
    private Vector2 position, spawnPos;
    public int id = -1;

    public Faction() {
        name = "Faction not named";
        shipColour = "";
    }

    /**
     * Creates a faction with the specified name, colour, and in-game location.
     *
     * @param name   name of faction
     * @param colour colour name (used as prefix to retrieve ship sprites)
     * @param pos    2D vector location
     */
    public Faction(String name, String colour, Vector2 pos, Vector2 spawn, int id) {
        this();
        this.name = name;
        this.shipColour = colour;
        this.position = pos;
        spawnPos = spawn;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getColour() {
        return shipColour;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSpawnPos() {
        return spawnPos;
    }
}
