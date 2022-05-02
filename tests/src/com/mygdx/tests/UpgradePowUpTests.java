package com.mygdx.tests;

import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.PlayerController;
import com.mygdx.game.Components.Transform;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Powerup;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.PhysicsManager;
import com.mygdx.game.Managers.RenderingManager;
import com.mygdx.game.Managers.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class UpgradePowUpTests {
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
        GameManager.getShips().clear();
        GameManager.getColleges().clear();
    }

    @Test
    public void testPlayerGainsSpeedFromPowerup(){
        Powerup speed = new Powerup(4, 900f, 900f);
        Player p = GameManager.getPlayer();
        speed.applyPowUp(p);
        p.update();
        assertTrue("Player failed to earn plunder for collecting powerup!", p.getPlunder() == 10);
        PlayerController pc = p.getComponent(PlayerController.class);
        assertTrue("Speed did not increase upon powerup collection!",pc.getSpeed() > pc.getBase_speed());
    }

    @Test
    public void testPlayerGainsInvincibility(){
        Powerup inv = new Powerup(3, 900f, 900f);
        Player p = GameManager.getPlayer();
        inv.applyPowUp(p);
        p.update();
        assertTrue("Player failed to earn plunder for collecting powerup!", p.getPlunder() == 10);
        assertTrue("Player did not gain invincibility!",p.isInvincible());
    }

    @Test
    public void testPlayerGainsWeatherRes(){
        Powerup weather = new Powerup(2, 900f, 900f);
        Player p = GameManager.getPlayer();
        weather.applyPowUp(p);
        p.update();
        assertTrue("Player failed to earn plunder for collecting powerup!", p.getPlunder() == 10);
        assertTrue("Did not gain weather resistance!", p.isWeatherResistant());
    }

    @Test
    public void testPowerupsDoNotCompound(){
        Powerup weather = new Powerup(2, 900f, 900f);
        Powerup inv = new Powerup(3, 950f, 900f);
        Player p = GameManager.getPlayer();
        weather.applyPowUp(p);
        p.update();
        inv.applyPowUp(p);
        p.update();
        assertTrue("Player failed to earn correct amount of plunder!",p.getPlunder() == 30);
        assertTrue("Two powerups active at once!", p.isWeatherResistant() && !p.isInvincible());
    }

    @Test
    public void testHealthRefillWorks(){
        Powerup medkit = new Powerup(0, 900f, 900f);
        Player p = GameManager.getPlayer();
        p.getComponent(Pirate.class).takeDamage(50f);
        medkit.applyPowUp(p);
        p.update();
        assertTrue(p.getHealth() == 100);
    }

    @Test
    public void testAmmoRefillWorks(){
        Powerup ammo = new Powerup(1, 900f, 900f);
        Player p = GameManager.getPlayer();
        int ammo_init = p.getAmmo();
        p.shoot();
        p.update();
        p.shoot();
        p.update();
        assertTrue(p.getAmmo() < ammo_init);
        ammo.applyPowUp(p);
        p.update();
        assertTrue("Powerup failed to restore ammo!",p.getAmmo() == ammo_init);
    }




}
