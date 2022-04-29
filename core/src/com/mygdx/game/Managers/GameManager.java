package com.mygdx.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.AI.TileMapGraph;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.RigidBody;
import com.mygdx.game.Components.Transform;
import com.mygdx.game.Entitys.*;
import com.mygdx.game.Faction;
import com.mygdx.game.Quests.KillDuckQuest;
import com.mygdx.game.Quests.KillQuest;
import com.mygdx.game.Quests.LocateQuest;
import com.mygdx.utils.QueueFIFO;
import com.mygdx.utils.Utilities;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * Responsible for creating most entity's associated with the game. Also the cached chest and cannonballs
 */
public final class GameManager {
    private static boolean initialized = false;
    private static ArrayList<Faction> factions;
    private static ArrayList<Ship> ships;
    private static ArrayList<College> colleges;

    private static final int cacheSize = 40;
    private static ArrayList<CannonBall> ballCache;
    private static int currentElement;
    private static Powerup [] powerups;
    private static JsonValue settings;
    private static DuckMonster longboi;
    private static TileMapGraph mapGraph;

    /**
     * facilitates creation of the game
     */
    public static void Initialize() {
        initialized = true;
        currentElement = 0;
        settings = new JsonReader().parse(Gdx.files.internal("GameSettings.json"));
        factions = new ArrayList<>();
        ships = new ArrayList<>();
        ballCache = new ArrayList<>(cacheSize);
        colleges = new ArrayList<>();
        powerups = new Powerup[50];
        for (int i = 0; i < cacheSize; i++) {
            ballCache.add(new CannonBall());
        }
        for (JsonValue v : settings.get("factions")) {
            String name = v.getString("name");
            String col = v.getString("colour");
            Vector2 pos = new Vector2(v.get("position").getFloat("x"), v.get("position").getFloat("y"));
            pos = Utilities.tilesToDistance(pos);
            Vector2 spawn = new Vector2(v.get("shipSpawn").getFloat("x"), v.get("shipSpawn").getFloat("y"));
            spawn = Utilities.tilesToDistance(spawn);
            factions.add(new Faction(name, col, pos, spawn, factions.size() + 1));
        }
    }

    public static void load_game(JsonValue _factions, JsonValue _ships, JsonValue _colleges, JsonValue _quests, JsonValue base) {

        factions.clear();
        factions = new ArrayList<>();

        Player p = GameManager.getPlayer();

        for (Ship s : ships) {
            s.getComponent(Pirate.class).setHealth(0);
            s.setDead();
            s.update();
        }
        ships.clear();
        ships = new ArrayList<>();

//        ballCache = new ArrayList<>(cacheSize);


        for (College c : colleges) {
            for (Building b : c.getBuildings()) {
                b.hide();
            }
        }
        colleges.clear();
        colleges = new ArrayList<>();
        powerups = new Powerup[50];

        System.gc();
//        for (int i = 0; i < cacheSize; i++) {
//            ballCache.add(new CannonBall());
//        }

//        Load Factions
        for (JsonValue faction : _factions) {
            String name = faction.getString("name");
            String col = faction.getString("shipColour");
            Vector2 pos = new Vector2(faction.get("position").getFloat("x"), faction.get("position").getFloat("y"));
            Vector2 spawn = new Vector2(faction.get("spawnPos").getFloat("x"), faction.get("spawnPos").getFloat("y"));
            factions.add(new Faction(name, col, pos, spawn, factions.size() + 1));
        }


//        Create player
        p.setPlayer();
        p.setFaction(1);
        try {
            p.setPlunder(base.getInt("plunder"));
        } catch (IllegalArgumentException e) {
            p.setPlunder(0);
        }
        p.setHealth(base.getInt("health"));
        try {
            p.setCannonBalls(base.getInt("cannon_balls"));
        } catch (IllegalArgumentException e) {
            p.setCannonBalls(0);
        }
        ships.add(p);

//        Create ships from load
        int counter = 0;
        for (JsonValue ship : _ships) {
            if (counter != 0) {
                NPCShip s = CreateNPCShip(ship.getInt("faction") + 1);
                s.getComponent(Transform.class).setPosition(new Vector2(ship.get("position").getInt("x"), ship.get("position").getInt("y")));
                s.getComponent(RigidBody.class).setPosition(new Vector2(ship.get("position").getInt("x"), ship.get("position").getInt("y")));
            }
            counter ++;
        }

//        System.out.println(_factions);
//        System.out.println("\n\n\n\n");
//        for (Faction f : factions) {
//            System.out.println(f.getName());
//        }

        for (JsonValue j : _colleges) {
            if (!j.getBoolean("destroyed")) {
                CreateCollege(j.getInt("factionID") + 1);
            } else if (j.getInt("factionID") == 0) {
                CreateCollege(j.getInt("factionID") + 1);
            }
        }

//        To stop update() from marking the game as complete when clearing all quests.
        QuestManager.toggleLoading(true);
        QuestManager.clearAllQuests();
//        Create quests on load
        for (JsonValue j : _quests) {
            if (j.getBoolean("completed")) {
                assert true;
            }
            else {
                if (Objects.equals(j.getString("type"), "KillQuest")) {
                    counter = 0;
                    KillQuest q = null;
                    for (Faction f : factions) {
                        try {
                            if (Objects.equals(j.getString("description"), f.getName())) {
                                College enemy = GameManager.getCollege(counter + 1);
                                q = new KillQuest(enemy);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            assert true;
                        }
                        counter++;
                    }
                    if (!(q == null)) {
                        QuestManager.addQuest(q);
                    } else {
                        System.out.println("KillQuest with null target found.");
                    }
                } else if (Objects.equals(j.getString("type"), "LocateQuest")) {
                    LocateQuest q = QuestManager.rndLocateQuestReturnable();
                    QuestManager.addQuest(q);
                } else if (Objects.equals(j.getString("type"), "KillDuckQuest")) {
                    KillDuckQuest q = new KillDuckQuest();
                    QuestManager.addQuest(q);
                } else {
                    System.out.println("Unspecified Error - Loading Quests has Broken");
                }
            }
        }
        QuestManager.removePlaceholderQuest();
        QuestManager.toggleLoading(false);
    }


    /**
     * called every frame checks id the quests are completed
     */
    public static void update() {
        QuestManager.checkCompleted();
    }

    /**
     * Player is always in ships at index 0
     *
     * @return the ship
     */
    public static Player getPlayer() {
        return (Player) ships.get(0);
    }

    /**
     * Creates the game with player maps, NPCs, colleges
     *
     * @param mapId the resource id of the tilemap
     */
    public static void SpawnGame(int mapId) {
        CreateWorldMap(mapId);
        CreatePlayer();
        final int cnt = settings.get("factionDefaults").getInt("shipCount");
        for (int i = 0; i < factions.size(); i++) {
            CreateCollege(i + 1);
            for (int j = 0; j < cnt; j++) {
                // prevents halifax from having shipcount + player
                if (i == 0 && j > cnt - 3) {
                    break;
                }
                NPCShip s = CreateNPCShip(i + 1);
                s.getComponent(Transform.class).setPosition(getFaction(i + 1).getSpawnPos());
                s.getComponent(RigidBody.class).setPosition(getFaction(i + 1).getSpawnPos());
            }
        }

    }

    /**
     * Creates player that belongs the faction with id 1
     */
    public static void CreatePlayer() {
        tryInit();
        Player p = new Player();
        p.setPlayer();
        p.setFaction(1);
        ships.add(p);
        longboi = new DuckMonster();
    }

    /**
     * Creates an NPC ship with the given faction
     *
     * @param factionId desired faction
     * @return the created ship
     */
    public static NPCShip CreateNPCShip(int factionId) {
        tryInit();
        NPCShip e = new NPCShip();
        e.setFaction(factionId);
        ships.add(e);
        return e;
    }

    /**
     * Creates the world map
     *
     * @param mapId resource id
     */
    public static void CreateWorldMap(int mapId) {
        tryInit();
        WorldMap map = new WorldMap(mapId);
        mapGraph = new TileMapGraph(map.getTileMap());
        Random r = new Random();
        for (int i = 0; i < powerups.length; i++){
            powerups[i] = new Powerup(r.nextInt(5));
        }
    }

    /**
     * Creates the college with it's building for the desired college
     *
     * @param factionId desired faction
     */
    public static void CreateCollege(int factionId) {
        tryInit();
        College c = new College(factionId);
        colleges.add(c);
    }

    private static void tryInit() {
        if (!initialized) {
            Initialize();
        }
    }

    public static Faction getFaction(int factionId) {
        tryInit();
        return factions.get(factionId - 1);
    }

    public static void reset(){
        for (Ship s : ships){
            s.getComponent(Pirate.class).setHealth(s.getComponent(Pirate.class).getMaxHealth());
            s.getComponent(Transform.class).setPosition(getFaction(s.getFactionId()).getSpawnPos());
            s.getComponent(RigidBody.class).setPosition(getFaction(s.getFactionId()).getSpawnPos());
        }
        for (College c : colleges){
            c.getComponent(Pirate.class).setHealth(100);
            for (Building b : c.getBuildings()){
                b.revive();
            }
        }
        Player p = GameManager.getPlayer();
        p.setBadWeather(false);
        p.getComponent(Transform.class).setPosition(800f, 800f);
        p.setPlunder(0);
        if (longboi.isActive()){
            longboi.deactivate();
        }
    }

    /**
     * Gets the setting object from the GameSetting.json
     *
     * @return the JSON representation fo settings
     */
    public static JsonValue getSettings() {
        tryInit();
        return settings;
    }

    public static College getCollege(int factionId) {
        tryInit();
        return colleges.get(factionId - 1);
    }

    public static DuckMonster getLongboi(){
        return longboi;
    }
    /**
     * Utilises the cached cannonballs to fire one
     *
     * @param p   parent
     * @param dir shoot direction
     */
    public static void shoot(Entity p, Vector2 dir) {
        Vector2 pos = p.getComponent(Transform.class).getPosition().cpy();
        //pos.add(dir.x * TILE_SIZE * 0.5f, dir.y * TILE_SIZE * 0.5f);
        ballCache.get(currentElement++).fire(pos, dir, p);
        currentElement %= cacheSize;
    }

    /**
     * uses a* not sure if it works but i think it does
     *
     * @param loc src
     * @param dst dst
     * @return queue of delta postions
     */
    public static QueueFIFO<Vector2> getPath(Vector2 loc, Vector2 dst) {
        return mapGraph.findOptimisedPath(loc, dst);
    }


    /**
     * Gets a random map cell, which is made of water and does not immediately border any land cells
     *
     * */
    public static Vector2 randomWaterCell(){
        Random r = new Random();
        Vector2 pos = mapGraph.getWaterNodes().get(r.nextInt(mapGraph.getWaterCount()));
        return pos;
    }

    public static ArrayList<Faction> getFactions() {return factions;}
    public static  ArrayList<Ship> getShips() {
        return ships;
    }
    public static ArrayList<College> getColleges() {
        return colleges;
    }
}
