package com.mygdx.tests;

import com.badlogic.gdx.Gdx;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class AssetTests {
    @Test
    public void testShipAssetsExist() {
        assertTrue("Ship skin not found!", Gdx.files.internal("ship.png").exists());
        assertTrue("Enemy skins not found!", Gdx.files.internal("boats.png").exists());
        assertTrue("Pointer arrow skin not found!", checkExists("arrow.png"));
        assertTrue(
                "Healthbar components not found!",
                checkExists("progress_bar_green.png") && checkExists("progress_bar_green.png")
        );

    }

    @Test
    public void testUISkinExists(){
        assertTrue(
                checkExists("UISkin/default.png") &&
                        checkExists("UISkin/default.fnt") &&
                        checkExists("UISkin/skin.atlas") &&
                        checkExists("UISkin/skin.json") &&
                        checkExists("UISkin/uiskin.png")
        );
    }

    @Test
    public void testCollegeAssetsExist(){
        assertTrue("College skins not found!", Gdx.files.internal("other.png").exists());
    }
    
    @Test
    public void testChestAssetExists(){
        assertTrue("Chest skins not found!", Gdx.files.internal("Chest.png").exists());
    }

    @Test
    public void testBadWeatherAssetsExist(){
        assertTrue(checkExists("rainfall.png") && checkExists("darken.png"));
    }

    @Test
    public void testLongboiAssetsExist(){
        assertTrue("Longboi ball not found!", checkExists("Eggball.png"));
        assertTrue(
                "Longboi textures not found!",
                checkExists("longboi_moveset.png") && checkExists("longboi_moveset.txt")
        );
        assertTrue(checkExists("poison.png"));
    }



    public boolean checkExists(String filename){
        return Gdx.files.internal(filename).exists();
    }
}
