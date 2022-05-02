package com.mygdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.Entitys.NPCShip;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Managers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class SaveLoadTests {
    @Before
    public void prepareEnv(){
        int id_ship = ResourceManager.addTexture("ship.png");
        int id_map = ResourceManager.addTileMap("Map.tmx");
        int atlas_id = ResourceManager.addTextureAtlas("Boats.txt");
        int extras_id = ResourceManager.addTextureAtlas("UISkin/skin.atlas");
        int buildings_id = ResourceManager.addTextureAtlas("Buildings.txt");
        int powups_atlas_id = ResourceManager.addTextureAtlas("upgrades_powerups.txt");
        ResourceManager.addTexture("menuBG.jpg");
        ResourceManager.addTexture("Chest.png");
        ResourceManager.addTexture("progress_bar_red.png");
        ResourceManager.addTexture("progress_bar_green.png");
        ResourceManager.addTexture("points_star.png");
        ResourceManager.addTextureAtlas("upgrade_tier_bar.txt");
        ResourceManager.addTextureAtlas("upgrade_pricetags.txt");
        ResourceManager.addTextureAtlas("longboi_moveset.txt");
        ResourceManager.addTextureAtlas("Eggball.txt");
        ResourceManager.addTexture("poison.png");
        ResourceManager.addTexture("arrow.png");
        ResourceManager.loadAssets();
        INIT_CONSTANTS();
        PhysicsManager.Initialize(false);
        if(GameManager.getShips().isEmpty()) {
            GameManager.CreatePlayer(); // the player entity for tests
            GameManager.CreateNPCShip(2); // the "enemy" entity for tests
            GameManager.CreateNPCShip(1); // the "ally" entity for tests
        }
        if (GameManager.getColleges().isEmpty()){
            for (int i = 0; i < 5; i ++)
            GameManager.CreateCollege(i + 1);
        }
        QuestManager.Initialize();
    }
    @After
    public void cleanEnv(){
        ResourceManager.deepClean();
        GameManager.getShips().clear();
        GameManager.getColleges().clear();
    }

    @Test
    public void testWritesToFile(){
        SaveManager s = new SaveManager(0, 0, 0, 0, 0);
        String userprofile = System.getenv("USERPROFILE");
        String fileLoc = userprofile + "\\saved_data.json";
        assertTrue("No save file has been created!", Gdx.files.internal(fileLoc).exists());
    }

    @Test
    public void testStoresStateProperly(){
        JsonReader json = new JsonReader();
        Player p = GameManager.getPlayer();
        NPCShip enemy = (NPCShip)GameManager.getShips().get(2);
        NPCShip ally = (NPCShip)GameManager.getShips().get(2);
        String userprofile = System.getenv("USERPROFILE");
        String fileLoc = userprofile + "\\saved_data.json";
        JsonValue base = json.parse(Gdx.files.internal(fileLoc));
        JsonValue ships = base.get("ships");
        JsonValue colleges = base.get("colleges");
        JsonValue quests = base.get("quests");
        for (JsonValue ship : ships){
            if (ship.getInt("faction") == 0 && ship.getString("type") == "Player"){
                float pos_x, pos_y;
                pos_x = ship.get("position").getFloat("x");
                pos_y = ship.get("position").getFloat("y");
                assertTrue("Player position stored incorrectly!", p.getPosition().x == pos_x && p.getPosition().y == pos_y);
            }
            else if (ship.getInt("faction") == 0 && ship.getString("type") == "NPC"){
                float pos_x, pos_y;
                pos_x = ship.get("position").getFloat("x");
                pos_y = ship.get("position").getFloat("y");
                assertTrue("Ally ship position stored incorrectly!", ally.getPosition().x == pos_x && ally.getPosition().y == pos_y);
            }
            else if (ship.getInt("faction") == 1 && ship.getString("type") == "NPC"){
                float pos_x, pos_y;
                pos_x = ship.get("position").getFloat("x");
                pos_y = ship.get("position").getFloat("y");
                assertTrue("Enemy ship position stored incorrectly!", enemy.getPosition().x == pos_x && enemy.getPosition().y == pos_y);
            }
        }
        for (JsonValue college : colleges){
            int faction = college.getInt("factionID");
            assertTrue("College of faction " + faction + " was registered as destroyed even though it wasnt!", !college.getBoolean("destroyed"));
        }
        boolean duckQuestExists = false;
        for (JsonValue quest : quests){
            if(quest.getString("type").equals("KillDuckQuest")){duckQuestExists = true;}
            assertFalse("Quest was recorded as completed before completion!", quest.getBoolean("completed"));
        }
        assertTrue("Duck quest missing from list of saved quests!", duckQuestExists);
    }

}
