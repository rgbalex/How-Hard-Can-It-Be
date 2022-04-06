package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Components.ComponentEvent;
import com.mygdx.game.Components.PlayerController;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Managers.*;
import com.mygdx.game.PirateGame;
import com.mygdx.game.Quests.Quest;

import static com.mygdx.utils.Constants.*;

public class GameScreen extends Page {
    private Label healthLabel;
    private Label dosh;
    private Label ammo;
    private Label points;
    private final Label questDesc;
    private final Label questName;
    private Image bar_green;
    private Image current_powup;
    private final TextureRegionDrawable invincibility_drawable = new TextureRegionDrawable(new TextureRegion(new Texture("upgrades_powerups.png"), 256, 0, 32, 32));
    private final TextureRegionDrawable weather_res_drawable = new TextureRegionDrawable(new TextureRegion(new Texture("upgrades_powerups.png"), 32, 0, 32, 32));
    private final TextureRegionDrawable speedup_drawable = new TextureRegionDrawable(new TextureRegion(new Texture("upgrades_powerups.png"), 160, 0, 32, 32));
    private int timer_points;
    private int frame_timer;
    private int alpha_timer;
    private Boolean is_bad_weather;

    private final SpriteBatch effectBatch = new SpriteBatch();
    private final Texture darken = new Texture(Gdx.files.internal("darken.png"));
    private final Sprite bad_weather_sprite_dark = new Sprite(darken, 2000,2000);

    private final Sprite[] rainfall_list = new Sprite[9];

    /*private final Label questComplete;
    private float showTimer = 0;
    // in seconds
    private static final float showDuration = 1;*/

    /**
     * Boots up the actual game: starts PhysicsManager, GameManager, EntityManager,
     * loads texture atlases into ResourceManager. Draws quest and control info.
     *
     * @param parent PirateGame UI screen container
     * @param id_map the resource id of the tile map
     */
    public GameScreen(PirateGame parent, int id_map) {
        super(parent);
        INIT_CONSTANTS();
        PhysicsManager.Initialize(false);

        /*int id_ship = ResourceManager.addTexture("ship.png");
        int id_map = ResourceManager.addTileMap("Map.tmx");
        int atlas_id = ResourceManager.addTextureAtlas("Boats.txt");
        int extras_id = ResourceManager.addTextureAtlas("UISkin/skin.atlas");
        int buildings_id = ResourceManager.addTextureAtlas("Buildings.txt");
        ResourceManager.loadAssets();*/

        int powerups_atlas_id = ResourceManager.getId("upgrades_powerups.txt");
        GameManager.SpawnGame(id_map);
        //QuestManager.addQuest(new KillQuest(c));

        EntityManager.raiseEvents(ComponentEvent.Awake, ComponentEvent.Start);

        Window questWindow = new Window("Current Quest", parent.skin);
        Quest q = QuestManager.currentQuest();
        Table t = new Table();
        questName = new Label("NAME", parent.skin);
        t.add(questName);
        t.row();
        questDesc = new Label("DESCRIPTION", parent.skin);
        if (q != null) {
            questName.setText(q.getName());
            questDesc.setText(q.getDescription());
        }
        /*questComplete = new Label("", parent.skin);
        actors.add(questComplete);*/

        t.add(questDesc).left();
        questWindow.add(t);
        actors.add(questWindow);

        Table t1 = new Table();
        t1.top().right();
        t1.setFillParent(true);

        actors.add(t1);
        Window tutWindow = new Window("Controls", parent.skin);
        Table table = new Table();
        tutWindow.add(table);
        t1.add(tutWindow);

        table.add(new Label("Move with", parent.skin)).top().left();
        table.add(new Image(parent.skin, "key-w"));
        table.add(new Image(parent.skin, "key-s"));
        table.add(new Image(parent.skin, "key-a"));
        table.add(new Image(parent.skin, "key-d"));
        table.row();
        table.add(new Label("Shoot in direction of mouse", parent.skin)).left();
        //table.add(new Image(parent.skin, "space"));
        table.add(new Image(parent.skin, "mouse"));
        table.row();
        table.add(new Label("Shoot in direction of ship", parent.skin)).left();
        table.add(new Image(parent.skin, "space"));
        table.row();
        table.add(new Label("Quit", parent.skin)).left();
        table.add(new Image(parent.skin, "key-esc"));

        Window powUps = new Window("Powerups", parent.skin);
        Table powUpsTable = new Table();
        powUpsTable.setFillParent(true);
        powUpsTable.bottom().right();
        powUps.add(powUpsTable);
        powUpsTable.add(new Image(ResourceManager.getSprite(powerups_atlas_id, "powerup-0"))).left();
        powUpsTable.add(new Label("Repairs your hull", parent.skin)).left();
        powUpsTable.row();
        powUpsTable.add(new Image(ResourceManager.getSprite(powerups_atlas_id, "powerup-1"))).left();
        powUpsTable.add(new Label("Replenishes your ammo", parent.skin)).left();
        powUpsTable.row();
        powUpsTable.add(new Image(ResourceManager.getSprite(powerups_atlas_id, "powerup-2"))).left();
        powUpsTable.add(new Label("Provides bad weather resistance", parent.skin)).left();
        powUpsTable.row();
        powUpsTable.add(new Image(ResourceManager.getSprite(powerups_atlas_id, "powerup-3"))).left();
        powUpsTable.add(new Label("Invincibility!", parent.skin)).left();
        powUpsTable.row();
        powUpsTable.add(new Image(ResourceManager.getSprite(powerups_atlas_id, "powerup-4"))).left();
        powUpsTable.add(new Label("Provides a speed boost", parent.skin)).left();

        Table t2 = new Table();
        t2.bottom().right();
        t2.add(powUps);
        t2.setFillParent(true);
        actors.add(t2);


//        Effect setup
        Texture rainfall = new Texture(Gdx.files.internal("rainfall.png"));
        Sprite rainfall_sprite = new Sprite(rainfall, 640, 640);
        rainfall_sprite.setX(0);
        rainfall_sprite.setY(0);
        rainfall_sprite.setAlpha(0f);
        rainfall_list[0] = rainfall_sprite;

        Sprite rainfall_sprite1 = new Sprite(rainfall, 640, 640);
        rainfall_sprite1.setX(640);
        rainfall_sprite1.setY(0);
        rainfall_sprite1.setAlpha(0f);
        rainfall_list[1] = rainfall_sprite1;

        Sprite rainfall_sprite2 = new Sprite(rainfall, 640, 640);
        rainfall_sprite2.setX(0);
        rainfall_sprite2.setY(640);
        rainfall_sprite2.setAlpha(0f);
        rainfall_list[2] = rainfall_sprite2;

        Sprite rainfall_sprite3 = new Sprite(rainfall, 640, 640);
        rainfall_sprite3.setX(640);
        rainfall_sprite3.setY(640);
        rainfall_sprite3.setAlpha(0f);
        rainfall_list[3] = rainfall_sprite3;

        Sprite rainfall_sprite4 = new Sprite(rainfall, 640, 640);
        rainfall_sprite4.setX(640+640);
        rainfall_sprite4.setY(0);
        rainfall_sprite4.setAlpha(0f);
        rainfall_list[4] = rainfall_sprite4;

        Sprite rainfall_sprite5 = new Sprite(rainfall, 640, 640);
        rainfall_sprite5.setX(640+640);
        rainfall_sprite5.setY(640);
        rainfall_sprite5.setAlpha(0f);
        rainfall_list[5] = rainfall_sprite5;

        Sprite rainfall_sprite6 = new Sprite(rainfall, 640, 640);
        rainfall_sprite6.setX(640+640);
        rainfall_sprite6.setY(640+640);
        rainfall_sprite6.setAlpha(0f);
        rainfall_list[6] = rainfall_sprite6;

        Sprite rainfall_sprite7 = new Sprite(rainfall, 640, 640);
        rainfall_sprite7.setX(640);
        rainfall_sprite7.setY(640+640);
        rainfall_sprite7.setAlpha(0f);
        rainfall_list[7] = rainfall_sprite7;

        Sprite rainfall_sprite8 = new Sprite(rainfall, 640, 640);
        rainfall_sprite8.setX(0);
        rainfall_sprite8.setY(640+640);
        rainfall_sprite8.setAlpha(0f);
        rainfall_list[8] = rainfall_sprite8;
    }

    private float accumulator;

    /**
     * Called every frame calls all other functions that need to be called every frame by rasing events and update methods
     *
     * @param delta delta time
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(BACKGROUND_COLOUR.x, BACKGROUND_COLOUR.y, BACKGROUND_COLOUR.z, 1);
        EntityManager.raiseEvents(ComponentEvent.Update, ComponentEvent.Render);

        accumulator += EntityManager.getDeltaTime();

        // fixed update loop so that physics manager is called regally rather than somewhat randomly
        while (accumulator >= PHYSICS_TIME_STEP) {
            PhysicsManager.update();
            accumulator -= PHYSICS_TIME_STEP;
        }

        GameManager.update();
        // show end screen if esc is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            parent.setScreen(parent.end);
        }
        super.render(delta);
    }

    /**
     * disposed of all stuff it something is missing from this method you will get memory leaks
     */
    @Override
    public void dispose() {
        super.dispose();
        ResourceManager.cleanUp();
        EntityManager.cleanUp();
        RenderingManager.cleanUp();
        PhysicsManager.cleanUp();
    }

    /**
     * Resize camera, effectively setting the viewport to display game assets
     * at pixel ratios other than 1:1.
     *
     * @param width  of camera viewport
     * @param height of camera viewport
     */
    @Override
    public void resize(int width, int height) {
        //((Table) actors.get(0)).setFillParent(false);
        super.resize(width, height);
        OrthographicCamera cam = RenderingManager.getCamera();
        cam.viewportWidth = width / ZOOM;
        cam.viewportHeight = height / ZOOM;
        cam.update();

        // ((Table) actors.get(0)).setFillParent(true);
    }

    /**
     * Update the UI with new values for health, quest status, etc.
     * also called once per frame but used for actors by my own convention
     */
    //private String prevQuest = "";
    @Override
    protected void update() {
        super.update();
        Player p = GameManager.getPlayer();
        frame_timer += 1;
        if (frame_timer > 60) {
            frame_timer = 0;
            timer_points += 1;
        }

//        Effects are drawn first so they are underneath the UI
//        Fade In
        if ((timer_points%60) >= 10 && (timer_points%60) < 25) {
            if (!p.getBadWeather()) {
                p.setBadWeather(true);
            }
            bad_weather_sprite_dark.setAlpha(alpha_timer / 255f);
            for (Sprite sprite : rainfall_list) {sprite.setAlpha(alpha_timer / 255f);}
            if (alpha_timer < 254) {alpha_timer++;}
        }

//        Fade Out
        if ((timer_points%60) >= 30 && (timer_points%60) < 35) {
            if (p.getBadWeather()) {
                p.setBadWeather(false);
            }
            bad_weather_sprite_dark.setAlpha(alpha_timer / 255f);
            for (Sprite sprite : rainfall_list) {sprite.setAlpha(alpha_timer / 255f);}
            if (alpha_timer > 1) {alpha_timer --;}
        }

//        Rain Animation
        if ((timer_points%60) >= 10 && (timer_points%60) < 40) {
            effectBatch.begin();
            bad_weather_sprite_dark.draw(effectBatch);
            for (Sprite sprite : rainfall_list) {
                sprite.setX(sprite.getX() - 2);
                sprite.setY(sprite.getY() - 6);
                sprite.draw(effectBatch);
                if (sprite.getY() < -640) {sprite.setY(1080);}
                if (sprite.getX() < -640) {sprite.setX(1920 - (1.5f * 640));}
            }
            effectBatch.end();
        }

        healthLabel.setText(String.valueOf(p.getHealth()));
        dosh.setText(String.valueOf(p.getPlunder()));
        ammo.setText(String.valueOf(p.getAmmo()));
        if (p.isPoweredUp()){
            if (!current_powup.isVisible()){ current_powup.setVisible(true); }
            if (!bar_green.isVisible()){ bar_green.setVisible(true); }
            bar_green.setSize(p.getComponent(PlayerController.class).getPowerupTimer()/2f, 16);
            if (p.isInvincible()){current_powup.setDrawable(invincibility_drawable);}
            if (p.isWeatherResistant()){current_powup.setDrawable(weather_res_drawable);}
            if (p.isSpedUp()){current_powup.setDrawable(speedup_drawable);}
        }
        else{
            current_powup.setVisible(false);
            bar_green.setVisible(false);
        }
        points.setText(p.getPlunder() * 10 + timer_points);
        if (!QuestManager.anyQuests()) {
            parent.end.win();
            parent.setScreen(parent.end);
        } else {
            Quest q = QuestManager.currentQuest();
            /*if(Objects.equals(prevQuest, "")) {
                prevQuest = q.getDescription();
            }
            if(!Objects.equals(prevQuest, q.getDescription())) {
                questComplete.setText("Quest Competed");
                prevQuest = "";
            }*/
            questName.setText(q.getName());
            questDesc.setText(q.getDescription());
        }
        /*if(!questComplete.getText().equals("")) {
            showTimer += EntityManager.getDeltaTime();
        }
        if(showTimer >= showDuration) {
            showTimer = 0;
            questComplete.setText("");
        }*/
    }

    /**
     * Draw UI elements showing player health, plunder, and ammo.
     */
    @Override
    protected void CreateActors() {
        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(false);
        actors.add(table);

        table.add(new Image(parent.skin.getDrawable("heart"))).top().left().size(1.25f * TILE_SIZE);
        healthLabel = new Label("N/A", parent.skin);
        table.add(healthLabel).top().left().size(50);

        table.row();

        table.add(new Image(parent.skin.getDrawable("coin"))).top().left().size(1.25f * TILE_SIZE);
        dosh = new Label("N/A", parent.skin);
        table.add(dosh).top().left().size(50);

        table.row();

        table.add(new Image(parent.skin.getDrawable("ball"))).top().left().size(1.25f * TILE_SIZE);
        ammo = new Label("N/A", parent.skin);
        table.add(ammo).top().left().size(50);
        bar_green = new Image(ResourceManager.getTexture("progress_bar_green.png"));
        current_powup = new Image();
        table.row();
        table.add(new Image(ResourceManager.getTexture("points_star.png")));
        points = new Label("N/A", parent.skin);
        table.add(points).top().left().size(50);
        table.row();
        table.add(current_powup).top().left().center().size(1.25f * TILE_SIZE);
        table.add(bar_green).top().left().size(1.25f * TILE_SIZE);
        table.top().left();



    }
}
