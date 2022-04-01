package com.mygdx.tests;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.RigidBody;
import com.mygdx.game.Components.Transform;
import com.mygdx.game.Entitys.CannonBall;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Managers.PhysicsManager;
import com.mygdx.game.Managers.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;

import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(GdxTestRunner.class)
public class CombatTests {
    @Before
    public void prepareEnv(){
        int id_ship = ResourceManager.addTexture("ship.png");
        int id_map = ResourceManager.addTileMap("Map.tmx");
        int atlas_id = ResourceManager.addTextureAtlas("Boats.txt");
        int extras_id = ResourceManager.addTextureAtlas("UISkin/skin.atlas");
        int buildings_id = ResourceManager.addTextureAtlas("Buildings.txt");
        ResourceManager.addTexture("menuBG.jpg");
        ResourceManager.addTexture("Chest.png");
        ResourceManager.loadAssets();
        INIT_CONSTANTS();
        PhysicsManager.Initialize(false);
    }
    @After
    public void cleanEnv(){
        ResourceManager.deepClean();
    }

    @Test
    public void testTakesDamage(){
        Player player = new Player();
        Pirate p = player.getComponent(Pirate.class);
        int startHealth = p.getHealth();
        p.takeDamage(1f);
        assertTrue("Damage was not taken when hit!", ((startHealth - p.getHealth()) == 1));

    }

    @Test
    public void testDies(){
        Player player = new Player();
        Pirate p = player.getComponent(Pirate.class);
        int startHealth = p.getHealth();
        for (int i = 0; i <= startHealth; i++){
            p.takeDamage(1f);
        }
        assertFalse("Ship was not killed after all hp was spent!", p.isAlive());
    }

    @Test
    public void testCannonBallMoves(){
        Player player = new Player();
        CannonBall ball = new CannonBall();
        assertFalse("Cannonball should be deactivated on creation", ball.isActive());
        Vector2 start_pos = new Vector2 (50, 50);
        Vector2 destination_sw = new Vector2 (0, 0); // to test southwest direction
        Vector2 destination_se = new Vector2 (100, 0); // to test southeast direction
        Vector2 destination_nw = new Vector2 (0, 100); // to test northwest direction
        Vector2 destination_ne = new Vector2 (100, 100); // to test northeast direction
        ball.fire(start_pos, destination_sw, player);
        Vector2 ball_velocity = ball.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!", ((ball_velocity.x < 0) && (ball_velocity.y < 0)));
        ball.fire(start_pos, destination_se, player);
        ball_velocity = ball.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!", ((ball_velocity.x > 0) && (ball_velocity.y < 0)));
        ball.fire(start_pos, destination_ne, player);
        ball_velocity = ball.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!", ((ball_velocity.x > 0) && (ball_velocity.y > 0)));
        ball.fire(start_pos, destination_nw, player);
        ball_velocity = ball.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!", ((ball_velocity.x < 0) && (ball_velocity.y > 0)));
    }
}
