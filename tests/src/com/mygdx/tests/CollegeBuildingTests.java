package com.mygdx.tests;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Entitys.Building;
import com.mygdx.game.Entitys.College;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.PhysicsManager;
import com.mygdx.game.Managers.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.mygdx.game.Components.*;
import static com.mygdx.utils.Constants.INIT_CONSTANTS;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class CollegeBuildingTests {
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
    public void testEnemyCollegeHasDefender(){
        College c = GameManager.getColleges().get(0);
        boolean hasDefender = false;
        for (Building b : c.getBuildings()){
            if (b.isDefender()){
                hasDefender = true;
            }
        }
        assertTrue("Enemy college doesnt have a defender!", hasDefender);
    }

    @Test
    public void testDefenderTakesDamage(){
        College c = GameManager.getColleges().get(0);
        Building defender = null;
        for (Building b : c.getBuildings()){
            if (b.isDefender()){
                defender = b;
            }
        }
        defender.update();
        int health_init = defender.getHealth();
        Player p = GameManager.getPlayer();
        Vector2 pos = defender.getComponent(Transform.class).getPosition();
        p.getComponent(Transform.class).setPosition(pos.x + 100f, pos.y + 100f);
        System.out.println(defender.getComponent(Transform.class).getPosition());
        p.shoot(defender.getComponent(Transform.class).getPosition().sub(p.getPosition()).scl(100f));
        for (int i = 0; i < 5000; i++){
            defender.update();
            PhysicsManager.update();
        }
        assertTrue("Defender building did not take damage!", defender.getHealth() < health_init);

    }

    @Test
    public void testNonDefendersDieInstantly(){
        College c = GameManager.getColleges().get(0);
        Building non_defender = null;
        for (Building b : c.getBuildings()){
            if (!b.isDefender() && b.isNonFlag()){
                non_defender = b;
                break;
            }
        }
        Player p = GameManager.getPlayer();
        p.shoot(non_defender.getComponent(Transform.class).getPosition().sub(p.getPosition()).scl(100f));
        for (int i = 0; i < 3000; i++){
            non_defender.update();
            PhysicsManager.update();
        }
        assertTrue("Non defender building did not die after being shot!", !non_defender.isAlive());
    }

    @Test
    public void testFlagsTakeNoDamage(){
        College c = GameManager.getColleges().get(0);
        Building flag = null;
        for (Building b : c.getBuildings()){
            if (!b.isNonFlag()){
                flag = b;
                break;
            }
        }
        Player p = GameManager.getPlayer();
        p.shoot(flag.getComponent(Transform.class).getPosition().sub(p.getPosition()).scl(100f));
        for (int i = 0; i < 3000; i++){
            flag.update();
            PhysicsManager.update();
        }
        assertTrue("Flag hurt by cannonball!", flag.isAlive() && flag.getHealth() == 100f);

    }

}
