package com.mygdx.game.Managers;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Components.*;
import com.mygdx.game.Entitys.College;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Faction;
import com.mygdx.game.Quests.Quest;

import java.io.*;
import java.util.ArrayList;

public class SaveManager{
    public SaveManager(int health_level, int ammo_level, int speed_level, int damage_level, int timer_points) {
//        Data Collection
        SaveData data = new SaveData();
        Player p = GameManager.getPlayer();
        data.factions = GameManager.getFactions();

//        Saving necessary data from ship entities as they themselves contain circular
//        references that when JSON-ified do not play nicely.
        ArrayList<Ship> ships = GameManager.getShips();
        ArrayList<DataShip> shipsData = new ArrayList<>();
        for (Ship i : ships) {
            String jsonFactionID = "Error";
            int counter = 0;
            for (Faction f : data.factions) {
                if (i.getComponent(Pirate.class).getFaction().getName() == f.getName()) {
                    jsonFactionID = String.valueOf(counter);
                } else {
                    counter ++;
                }
            }
            String type = i.isPlayer ? "Player" : "NPC";
            DataShip d = new DataShip(i.getPosition(), jsonFactionID, i.getComponent(Pirate.class).getFaction().getColour(), type);
            shipsData.add(d);
        }
        data.ships = shipsData;

//        Saving necessary data from college entities as they themselves contain circular
//        references that when JSON-ified do not play nicely.
        ArrayList<College> colleges = GameManager.getColleges();
        ArrayList<DataCollege> collegesData = new ArrayList<>();
        for (College i : colleges) {
            String jsonFactionID = "Error";
            int counter = 0;
            for (Faction f : data.factions) {
                if (i.getComponent(Pirate.class).getFaction().getName() == f.getName()) {
                    jsonFactionID = String.valueOf(counter);
                } else {
                    counter ++;
                }
            }
            DataCollege d = new DataCollege(jsonFactionID);
            collegesData.add(d);
        }
        data.colleges = collegesData;

//        Saving necessary data from quest entries as they themselves contain circular
//        references that when JSON-ified do not play nicely.
        ArrayList<Quest> quests = QuestManager.getAllQuests();
        ArrayList<DataQuest> questData = new ArrayList<>();
        String type;
        for (Quest i : quests) {
            if (i.getClass().getSimpleName().equals("KillQuest")) {
                type = "KillQuest";
                DataQuest d = new DataQuest(type, i.getName(), i.getDescription(), i.getReward(), i.isCompleted(), i.getTarget());
                questData.add(d);
            } else {
                type = "LocateQuest";
                DataQuest d = new DataQuest(type, i.getName(), i.getDescription(), i.getReward(), i.isCompleted(), i.getLoc(), i.getRadius());
                questData.add(d);
            }
        }
        data.quests = questData;


//        Please note these will only save if non-zero
        data.health_level = health_level;
        data.ammo_level = ammo_level;
        data.speed_level = speed_level;
        data.damage_level = damage_level;
        data.health = p.getHealth();
        data.plunder = p.getPlunder();
        data.cannon_balls = p.getAmmo();
        data.points = (p.getPlunder() * 10 + timer_points);
        data.x = p.getPosition().x;
        data.y = p.getPosition().y;

//        Data Handling
        Json jsonObject = new Json();
        String output = jsonObject.prettyPrint(data);
        String userprofile = System.getenv("USERPROFILE");
        String fileLoc = userprofile + "\\saved_data.json";
        try (PrintWriter out = new PrintWriter(fileLoc)) {
            out.println(output);
        } catch (FileNotFoundException e) {
            try {
                File myObj = new File(fileLoc);
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    System.out.println("File already exists.");
                }
            }
            catch (IOException ee) {
                System.out.println("An error occurred.");
                e.printStackTrace();
                System.out.println("==== Second Error Below ====");
                ee.printStackTrace();
            }
        }
    }
}
