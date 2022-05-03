package com.mygdx.tests;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.Components.RigidBody;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.PhysicsManager;
import com.mygdx.game.Managers.ResourceManager;
import org.junit.*;
import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;

@RunWith(GdxTestRunner.class)
public class MovementTests {
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
        GameManager.CreatePlayer(); // the player entity for tests
        GameManager.CreateNPCShip(2); // the "enemy" entity for tests
        GameManager.CreateNPCShip(1); // the "ally" entity for tests
        GameManager.CreateCollege(2);
    }
    @After
    public void cleanEnv(){
        ResourceManager.deepClean();
        GameManager.getShips().clear();
        GameManager.getColleges().clear();
    }
    @Test
    public void testMovesNorth(){ testMoveDirection("-up");}
    @Test
    public void testMovesSouth(){testMoveDirection("-down");}
    @Test
    public void testMovesEast(){testMoveDirection("-right");}
    @Test
    public void testMovesWest(){ testMoveDirection("-left");}
    @Test
    public void testMovesNorthEast(){testMoveDirection("-ur");}
    @Test
    public void testMovesNorthWest(){testMoveDirection("-ul");}
    @Test
    public void testMovesSouthEast(){
        testMoveDirection("-dr");
    }
    @Test
    public void testMovesSouthWest(){testMoveDirection("-dl");}

    public void testMoveDirection(String dir){
        /**
         * Tests one of the 8 possible directions a ship can move in.
         * @param dir : the direction to be tested.
         *
         * */
        ObjectMap shipDirections = new ObjectMap<>();
        shipDirections.put("-up", new Vector2(0f, 100f));
        shipDirections.put("-down", new Vector2(0f, -100f));
        shipDirections.put("-right", new Vector2(100f, 0f));
        shipDirections.put("-left", new Vector2(-100f, 0f));
        shipDirections.put("-ur", new Vector2(100f, 100f));
        shipDirections.put("-ul", new Vector2(-100f, 100f));
        shipDirections.put("-dr", new Vector2(100f, -100f));
        shipDirections.put("-dl", new Vector2(-100f, -100f));

        Vector2 moveDir = (Vector2) shipDirections.get(dir);
        Player player = GameManager.getPlayer();
        RigidBody player_body = player.getComponent(RigidBody.class);
        Vector2 start = player.getPosition().cpy();
        player.update();
        player_body.setVelocity(moveDir);
        player_body.update();
        PhysicsManager.update();
//        player.update();
//        //player_body.setVelocity(moveDir);
//        player_body.update();
//        PhysicsManager.update();
        Vector2 delta = player.getPosition().cpy().sub(start);
        delta = new Vector2(delta.x * moveDir.x, delta.y * moveDir.y);
        //Check that ship has moved in the correct direction:
        assertTrue(
                "Ship moved in incorrect direction!" + delta,
                (
                        ((delta.x >= 0.0) && // ship moved in the correct direction in the x axis
                        (delta.y >= 0.0)) // ship moved in the correct direction in the y axis
                ));

        }
}
