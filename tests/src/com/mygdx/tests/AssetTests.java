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
    }

    @Test
    public void testCollegeAssetsExist(){
        assertTrue("College skins not found!", Gdx.files.internal("other.png").exists());
    }
    
    @Test
    public void testChestAssetExists(){
        assertTrue("Chest skins not found!", Gdx.files.internal("Chest.png").exists());
    }

}
