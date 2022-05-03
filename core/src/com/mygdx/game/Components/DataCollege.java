package com.mygdx.game.Components;
/**
 * This class has been added for assesment 2.
 * It is used to store data about a college for future reloading.
 * */
public class DataCollege {
    public String factionID;
    public boolean destroyed;

    public DataCollege(String factionID, boolean destroyed){
        this.factionID = factionID;
        this.destroyed = destroyed;
    }
}
