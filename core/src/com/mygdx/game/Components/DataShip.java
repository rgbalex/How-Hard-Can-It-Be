package com.mygdx.game.Components;

import com.badlogic.gdx.math.Vector2;

public class DataShip {
    public Vector2 position;
    public String faction;
    public String colour;
    public String type;

    public DataShip(Vector2 position, String faction, String colour, String type) {
        this.position = position;
        this.faction = faction;
        this.colour = colour;
        this.type = type;
    }
}
