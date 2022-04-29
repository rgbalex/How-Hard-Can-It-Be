package com.mygdx.tests;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Components.ComponentEvent;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.RigidBody;
import com.mygdx.game.Components.Transform;
import com.mygdx.game.Entitys.*;
import com.mygdx.game.Managers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;

import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * This class tests things related to entity combat within the game.
 * Events like damaging ships, cannonball travel, longboi poison/position are tested here.
 * */
@RunWith(GdxTestRunner.class)
public class CombatTests {
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
    }
    @After
    public void cleanEnv(){
        ResourceManager.deepClean();
        GameManager.getShips().remove(0);
        GameManager.getShips().remove(0);
        GameManager.getShips().remove(0);
    }

    @Test
    public void testPlayerTakesDamage(){
        Player player = GameManager.getPlayer();
        Pirate p = player.getComponent(Pirate.class);
        int startHealth = p.getHealth();
        p.takeDamage(1f);
        assertTrue("Damage was not taken when hit!", ((startHealth - p.getHealth()) == 1));

    }

    @Test
    public void testPlayerDies(){
        Player player = GameManager.getPlayer();
        Pirate p = player.getComponent(Pirate.class);
        int startHealth = p.getHealth();
        for (int i = 0; i <= startHealth; i++){
            p.takeDamage(1f);
        }
        assertFalse("Ship was not killed after all hp was spent!", p.isAlive());
    }

    @Test
    public void testCannonBallMoves(){
        Player player = GameManager.getPlayer();
        CannonBall ball1 = new CannonBall();
        CannonBall ball2 = new CannonBall();
        CannonBall ball3 = new CannonBall();
        CannonBall ball4 = new CannonBall();
        assertFalse("Cannonball should be deactivated on creation", ball1.isActive());
        Vector2 start_pos = new Vector2 (50, 50);
        Vector2 destination_sw = new Vector2 (0, 0); // to test southwest direction
        Vector2 destination_se = new Vector2 (100, 0); // to test southeast direction
        Vector2 destination_nw = new Vector2 (0, 100); // to test northwest direction
        Vector2 destination_ne = new Vector2 (100, 100); // to test northeast direction
        ball1.fire(start_pos, destination_sw.sub(start_pos).scl(100f), player);
        Vector2 ball_velocity = ball1.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!" + ball_velocity, ((ball_velocity.x < 0) && (ball_velocity.y < 0)));
        ball1.kill();
        ball2.fire(start_pos, destination_se.sub(start_pos).scl(100f), player);
        ball_velocity = ball2.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!" + ball_velocity, ((ball_velocity.x > 0) && (ball_velocity.y < 0)));
        ball2.kill();
        ball3.fire(start_pos, destination_ne.sub(start_pos).scl(100f), player);
        ball_velocity = ball3.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!" + ball_velocity, ((ball_velocity.x > 0) && (ball_velocity.y > 0)));
        ball3.kill();
        ball4.fire(start_pos, destination_nw.sub(start_pos).scl(100f), player);
        ball_velocity = ball4.getComponent(RigidBody.class).getVelocity();
        assertTrue("Cannonball direction is incorrect!" + ball_velocity, ((ball_velocity.x < 0) && (ball_velocity.y > 0)));
        ball4.kill();
    }

    @Test
    public void testLongboiPoisonWorks(){
        Player p = GameManager.getPlayer();
        DuckMonster duck = new DuckMonster();
        assertTrue("Duck should be inactive on spawn",duck.isAlive() && !duck.isActive());
        duck.place(1000f, 1200f);
        duck.getComponent(Pirate.class).takeDamage(10f);
        assertTrue("Longboi not taking damage!", duck.getHealth() == 90f);
        p.getComponent(Transform.class).setPosition(1000f, 1100f);
        p.update();
        duck.update();
        int poison_timer = duck.getPoisonTimer();
        for (int i = 0; i < 10; i++){
            PhysicsManager.update();
            duck.update();
            p.update();
        }
        assertTrue("Failed to register player in poison range!",poison_timer - duck.getPoisonTimer() == 10);

        p.getComponent(Transform.class).setPosition(1000f, 2000f);
        duck.update();
        poison_timer = duck.getPoisonTimer();
        for (int i = 0; i < 10; i++){
            PhysicsManager.update();
            duck.update();
            p.update();
        }
        assertTrue("Poison timer decreasing when player not in range!", poison_timer == duck.getPoisonTimer());

    }

    @Test
    public void testLongboiFacesPlayer(){
        DuckMonster duck = new DuckMonster();
        Player p = GameManager.getPlayer();
        duck.place(1000f, 1000f);
        p.getComponent(Transform.class).setPosition(1000f, 1300f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "up");
        p.getComponent(Transform.class).setPosition(1000f, 900f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "down");
        p.getComponent(Transform.class).setPosition(900f, 900f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "dl");
        p.getComponent(Transform.class).setPosition(1200f, 900f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "dr");
        p.getComponent(Transform.class).setPosition(1200f, 1200f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "ur");
        p.getComponent(Transform.class).setPosition(900f, 1200f);
        duck.update();
        p.update();
        assertTrue(duck.getDir() == "ul");
        p.getComponent(Transform.class).setPosition(900f, 1016f);
        duck.update();
        p.update();
        assertTrue(duck.getDir(), duck.getDir() == "left");
        p.getComponent(Transform.class).setPosition(1100f, 1016f);
        duck.update();
        p.update();
        assertTrue(duck.getDir(), duck.getDir() == "right");
    }

    @Test
    public void testCannonBallHurtsPlayer(){
        Player p = GameManager.getPlayer();
        NPCShip npc = (NPCShip) GameManager.getShips().get(1);
        NPCShip ally = (NPCShip) GameManager.getShips().get(2);
        assertTrue(ally.isAlly());
        int health_init = p.getHealth();
        float dmg = npc.getComponent(Pirate.class).getDmg();
        assertFalse("NPC of faction 2 should not be an ally!", npc.isAlly());
        GameManager.shoot(npc, p.getPosition().sub(npc.getPosition()).scl(100f)); // shooting at player's direction via gameManager
        for (int i = 0; i < 1000; i++ ){
            p.update();
            PhysicsManager.update();
        }
        assertTrue("Player was not hurt by collision!",health_init - p.getHealth() == dmg);

        npc.shootAt(p.getPosition()); // shooting at player's direction via NPC method
        for (int i = 0; i < 1000; i++ ){
            p.update();
            PhysicsManager.update();
        }
        assertTrue("Player was not hurt by collision!",health_init - p.getHealth() == 2 * dmg);
        ally.shootAt(p.getPosition());
        for (int i = 0; i < 2000; i++ ){
            p.update();
            PhysicsManager.update();
        }
        assertFalse("Player was hurt by friendly fire!",health_init - p.getHealth() == 3 * dmg);

    }

    @Test
    public void testEggballHurtsPlayer(){
        Player p = GameManager.getPlayer();
        DuckMonster duck = GameManager.getLongboi();
        NPCShip ally = (NPCShip) GameManager.getShips().get(2);
        duck.place(1000f, 1200f);
        GameManager.shoot(duck, p.getPosition().sub(duck.getPosition()).scl(100f));
        int health_init = p.getHealth();
        for (int i = 0; i < 3000; i++){
            p.update();
            PhysicsManager.update();
        }
        assertTrue("Longboi failed to hurt the player!",health_init - p.getHealth() == duck.getComponent(Pirate.class).getDmg());
        GameManager.shoot(duck, ally.getPosition().sub(duck.getPosition()).scl(100f));
        health_init = ally.getHealth();
        for (int i = 0; i < 3000; i++){
            ally.update();
            PhysicsManager.update();
        }
        assertTrue("Longboi is not supposed to damage anyone but the player!", health_init == ally.getHealth());
    }

    @Test
    public void testPlayerEarnsPlunder(){
        Player p = GameManager.getPlayer();
        int ammo_init = p.getAmmo();
        int plunder_init = p.getPlunder();
        NPCShip npc = (NPCShip) GameManager.getShips().get(1);
        for(int d = 0; d < 11; d ++) {
            GameManager.shoot(p, npc.getPosition().sub(npc.getPosition()).scl(100f));
            for (int i = 0; i < 2000; i++) {
                npc.update();
                PhysicsManager.update();
            }
            p.update();
        }
        assertFalse("NPC did not register as dead after having 0 hp!", npc.isAlive());
        assertTrue("Player did not gain plunder!", plunder_init < p.getPlunder());
        assertTrue("Killed npc did not travel offscreen!",npc.getPosition().x >= 10000f && npc.getPosition().y >= 10000f);
    }
}

