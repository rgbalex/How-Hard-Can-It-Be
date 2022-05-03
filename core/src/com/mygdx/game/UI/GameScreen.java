package com.mygdx.game.UI;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Components.*;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Managers.*;
import com.mygdx.game.PirateGame;
import com.mygdx.game.Quests.KillDuckQuest;
import com.mygdx.game.Quests.Quest;
import com.mygdx.utils.Constants;

import static com.mygdx.utils.Constants.*;
/**
 * The screen the player will spend the majority of time playing on.
 * Contains the game viewport, UI, provides access to the shop etc.
 * */
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
    private Boolean paused = false;
    private Boolean shopOpen = false;
    private Image shop;
    private Window shopWindow;
    private Window pauseScreen;
    private final SpriteBatch effectBatch = new SpriteBatch();
    private final Texture darken = new Texture(Gdx.files.internal("darken.png"));
    private final Sprite bad_weather_sprite_dark = new Sprite(darken, 2000,2000);
    private final Sprite[] rainfall_list = new Sprite[9];
    private Table upgrades;
    private Table shopTable;
    private final TextureRegionDrawable upg_0 = new TextureRegionDrawable(new TextureRegion(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-0")));
    private final TextureRegionDrawable upg_1 = new TextureRegionDrawable(new TextureRegion(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-1")));
    private final TextureRegionDrawable upg_2 = new TextureRegionDrawable(new TextureRegion(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-2")));
    private final TextureRegionDrawable upg_3 = new TextureRegionDrawable(new TextureRegion(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-3")));
    private Image speed_upg_bar;
    private Image health_upg_bar;
    private Image damage_upg_bar;
    private Image ammo_upg_bar;
    private int health_level = 0;
    private int ammo_level = 0;
    private int speed_level = 0;
    private int damage_level = 0;
    private TextureRegionDrawable[] upgrade_bar_components = new TextureRegionDrawable[] {upg_0, upg_1, upg_2, upg_3};
    private TextButton health_buy, ammo_buy, speed_buy, damage_buy;
    private TextButton [] shop_buttons = new TextButton[] {health_buy, ammo_buy, speed_buy, damage_buy};
    private SaveManager s;
    private Container<Table> shopContainer;
    private TextButton openShop;

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
        questWindow.setMovable(false);
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
        tutWindow.setMovable(false);
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
        table.add(new Label("Pause", parent.skin)).left(); //Pauses instead of quitting now
        table.add(new Image(parent.skin, "key-esc"));

        Window powUps = new Window("Powerups", parent.skin);
        powUps.setMovable(false);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if(!paused && !shopOpen) {
            openShop.setVisible(true);
            ScreenUtils.clear(BACKGROUND_COLOUR.x, BACKGROUND_COLOUR.y, BACKGROUND_COLOUR.z, 1);
            EntityManager.raiseEvents(ComponentEvent.Update);
            EntityManager.raiseEvents(ComponentEvent.Render);
            accumulator += EntityManager.getDeltaTime();

            // fixed update loop so that physics manager is called regally rather than somewhat randomly
            while (accumulator >= PHYSICS_TIME_STEP) {
                PhysicsManager.update();
                accumulator -= PHYSICS_TIME_STEP;
            }

            GameManager.update();
            // changed to pause the game instead
            super.render(delta);
        }
        else{
            ScreenUtils.clear(BACKGROUND_COLOUR.x, BACKGROUND_COLOUR.y, BACKGROUND_COLOUR.z, 1);
            EntityManager.raiseEvents(ComponentEvent.Render); //needs to be called or complete chaos ensues
            super.render(delta); // renders ui (not updated w/ time)
        }
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
        if(!paused && !shopOpen) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                int x = Gdx.input.getX();
                int y = Gdx.input.getY();
                if (!openShop.isPressed()) {

                    // in range 0 to VIEWPORT 0, 0 bottom left
                    Vector2 delta = new Vector2(x, y);
                    delta.sub(HALF_DIMENSIONS); // center 0, 0
                    delta.nor();
                    delta.y *= -1;
                    // unit dir to fire
                    GameManager.getPlayer().shoot(delta);
                }
            }
            if(QuestManager.currentQuest() instanceof KillDuckQuest && !GameManager.getLongboi().isActive()){ // player is beginning the quest to kill longboi
                GameManager.getLongboi().place(1200, 1500);
            }
            if (!(GameManager.getPlayer().isAlive())){parent.setScreen(parent.end);}
            shopWindow.setVisible(false);
            pauseScreen.setVisible(false);
            super.update();
            Player p = GameManager.getPlayer();
            frame_timer += 1;
            if (frame_timer > 60) {
                frame_timer = 0;
                timer_points += 1;
            }

//        Effects are drawn first so they are underneath the UI
//        Fade In
            if ((timer_points % 60) >= 10 && (timer_points % 60) < 25) {
                if (!p.getBadWeather()) {
                    p.setBadWeather(true);
                }
                bad_weather_sprite_dark.setAlpha(alpha_timer / 255f);
                for (Sprite sprite : rainfall_list) {
                    sprite.setAlpha(alpha_timer / 255f);
                }
                if (alpha_timer < 254) {
                    alpha_timer++;
                }
            }

//        Fade Out
            if ((timer_points % 60) >= 30 && (timer_points % 60) < 35) {
                if (p.getBadWeather()) {
                    p.setBadWeather(false);
                }
                bad_weather_sprite_dark.setAlpha(alpha_timer / 255f);
                for (Sprite sprite : rainfall_list) {
                    sprite.setAlpha(alpha_timer / 255f);
                }
                if (alpha_timer > 1) {
                    alpha_timer--;
                }
            }

//        Rain Animation
            if ((timer_points % 60) >= 10 && (timer_points % 60) < 40) {
                effectBatch.begin();
                bad_weather_sprite_dark.draw(effectBatch);
                for (Sprite sprite : rainfall_list) {
                    sprite.setX(sprite.getX() - 2);
                    sprite.setY(sprite.getY() - 6);
                    sprite.draw(effectBatch);
                    if (sprite.getY() < -640) {
                        sprite.setY(1080);
                    }
                    if (sprite.getX() < -640) {
                        sprite.setX(1920 - (1.5f * 640));
                    }
                }
                effectBatch.end();
            }

            healthLabel.setText(String.valueOf(p.getHealth()));
            dosh.setText(String.valueOf(p.getComponent(Pirate.class).getPlunder()));
            ammo.setText(String.valueOf(p.getAmmo()));
            if (p.isPoweredUp()) {
                if (!current_powup.isVisible()) {
                    current_powup.setVisible(true);
                }
                if (!bar_green.isVisible()) {
                    bar_green.setVisible(true);
                }
                bar_green.setSize(p.getComponent(PlayerController.class).getPowerupTimer() / 2f, 16);
                if (p.isInvincible()) {
                    current_powup.setDrawable(invincibility_drawable);
                }
                if (p.isWeatherResistant()) {
                    current_powup.setDrawable(weather_res_drawable);
                }
                if (p.isSpedUp()) {
                    current_powup.setDrawable(speedup_drawable);
                }
            } else {
                current_powup.setVisible(false);
                bar_green.setVisible(false);
            }
            points.setText(p.getPlunder() * 10 + timer_points);
            if (!QuestManager.anyQuests() && !QuestManager.loading) {
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
        else if (shopOpen){
            shopWindow.setSize(shopContainer.getWidth() * 1.5f, shopContainer.getHeight() * 1.5f);
            shopWindow.setPosition(
                    (Gdx.graphics.getWidth() - shopWindow.getWidth()) / 2f,
                    (Gdx.graphics.getHeight() - shopWindow.getHeight()) /2f );
            if (GameManager.getPlayer().getComponent(Pirate.class).getCurrentPlunder() < 50) {
                for (TextButton b : shop_buttons) {
                    b.setText("too poor :(");
                    b.setTouchable(Touchable.disabled);
                }
            }
            else{
                for (TextButton b : shop_buttons) {
                    b.setText("Buy!");
                    b.setTouchable(Touchable.enabled);
                }
            }
            shopWindow.setVisible(true);
            ammo_upg_bar.setDrawable(upgrade_bar_components[ammo_level]);
            health_upg_bar.setDrawable(upgrade_bar_components[health_level]);
            speed_upg_bar.setDrawable(upgrade_bar_components[speed_level]);
            damage_upg_bar.setDrawable(upgrade_bar_components[damage_level]);
        }
        else{ //paused
            pauseScreen.setSize(
                    Gdx.graphics.getWidth() * 0.7f,
                    Gdx.graphics.getHeight() * 0.7f);
            pauseScreen.setPosition(
                    (Gdx.graphics.getWidth() - pauseScreen.getWidth()) / 2f,
                    (Gdx.graphics.getHeight() - pauseScreen.getHeight()) /2f );
            pauseScreen.setVisible(true);
            openShop.setVisible(false);
        }
    }

    /**
     * Draw UI elements showing player health, plunder, and ammo.
     */
    @Override
    protected void CreateActors() {
        ammo_upg_bar = new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-0"));
        health_upg_bar = new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-0"));
        speed_upg_bar = new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-0"));
        damage_upg_bar = new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_tier_bar.txt"), "tier-0"));

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
        table.add(bar_green).top().left().bottom().size(
                0f,
                TILE_SIZE / 2f
        );
        table.row();
        openShop = new TextButton("Open shop", parent.skin);
        openShop.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(openShop.getText().toString().equals("Open shop")){
                    shopOpen = true;
                    openShop.setText("Exit shop");
                }
                else{
                    shopOpen = false;
                    openShop.setText("Open shop");
                }
            }
        });
        table.add(openShop).colspan(2);
        openShop.setVisible(true);
        table.top().left();
        shopTable = new Table();
        upgrades = new Table();
        shopWindow = new Window("", parent.skin);
        Label title = new Label("Ye Olde Shoppe", parent.skin);
        title.setFontScale(2f);
        shopTable.add(title).padBottom(20f).top().left();
        shopTable.row();
        shopWindow.setBackground(new TextureRegionDrawable(new Texture("shop_screen.png")));
        upgrades.row();
        upgrades.add(new Image(ResourceManager.getSprite(ResourceManager.getId("upgrades_powerups.txt"), "wrench"))).left().size(64f, 64f);
        Table health_stack = new Table();
        health_stack.row();
        health_stack.add(tag_50()).left();
        health_stack.add(new Label("Increase your ship's hull strength", parent.skin)).left();
        health_stack.row();
        health_stack.add(health_upg_bar).left().fillX().colspan(3);
        upgrades.add(health_stack).left().fillX();
        health_buy = new TextButton("Buy!", parent.skin);
        health_buy.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (health_level < 3){
                    health_level += 1;
                    health_upg_bar.setDrawable(upgrade_bar_components[health_level]);
                    Pirate p = GameManager.getPlayer().getComponent(Pirate.class);
                    p.setMaxHealth(p.getMaxHealth() + 20);
                    p.setHealth(p.getMaxHealth());
                    healthLabel.setText(p.getHealth());
                    p.spendPlunder(50);
                    dosh.setText(p.getCurrentPlunder());
                }
            }
        });
        upgrades.add(health_buy).padLeft(5f);
        upgrades.row();
        upgrades.add(new Image(ResourceManager.getSprite(ResourceManager.getId("upgrades_powerups.txt"), "cannonball_pile"))).left().size(64f, 64f);
        Table ammo_stack = new Table();
        ammo_stack.row();
        ammo_stack.add(tag_50()).left();
        ammo_stack.add(new Label("Increase cannonball capacity", parent.skin)).left();
        ammo_stack.row();
        ammo_stack.add(ammo_upg_bar).left().fillX().colspan(3);
        upgrades.add(ammo_stack).left().fillX();;
        ammo_buy = new TextButton("Buy!", parent.skin);
        ammo_buy.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (ammo_level < 3) {
                    ammo_level+= 1;
                    ammo_upg_bar.setDrawable(upgrade_bar_components[ammo_level]);
                    Pirate p = GameManager.getPlayer().getComponent(Pirate.class);
                    p.setMaxAmmo(p.getMaxAmmo() + 20);
                    p.setAmmo(p.getMaxAmmo());
                    ammo.setText(p.getAmmo());
                    p.spendPlunder(50);
                    dosh.setText(p.getCurrentPlunder());
                }
            }
        });
        upgrades.add(ammo_buy).padLeft(5f);;
        upgrades.row();
        upgrades.add(new Image(ResourceManager.getSprite(ResourceManager.getId("upgrades_powerups.txt"), "explosion"))).left().size(64f, 64f);;
        Table damage_stack = new Table();
        damage_stack.row();
        damage_stack.add(tag_50()).left();
        damage_stack.add(new Label("Increase attack damage", parent.skin)).left();
        damage_stack.row();
        damage_stack.add(damage_upg_bar).left().fillX().colspan(3);
        upgrades.add(damage_stack).left().fillX();;
        damage_buy = new TextButton("Buy!", parent.skin);
        damage_buy.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (damage_level < 3) {
                    damage_level+= 1;
                    damage_upg_bar.setDrawable(upgrade_bar_components[damage_level]);
                    Pirate p = GameManager.getPlayer().getComponent(Pirate.class);
                    p.setAttackDmg(p.getDmg() + 20f);
                    p.spendPlunder(50);
                    dosh.setText(p.getCurrentPlunder());
                }
            }
        });
        upgrades.add(damage_buy).padLeft(5f);
        upgrades.row();
        upgrades.add(new Image(ResourceManager.getSprite(ResourceManager.getId("upgrades_powerups.txt"), "beer_mug"))).left().size(64f, 64f);;
        Table speed_stack = new Table();
        speed_stack.row();
        speed_stack.add(tag_50()).left();
        speed_stack.add(new Label("Increase ship speed", parent.skin)).left();
        speed_stack.row();
        speed_stack.add(speed_upg_bar).left().fillX().colspan(3);
        upgrades.add(speed_stack).left().fillX();
        speed_buy = new TextButton("Buy!", parent.skin);
        speed_buy.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (speed_level < 3){
                    speed_level += 1;
                    speed_upg_bar.setDrawable(upgrade_bar_components[speed_level]);
                    PlayerController p = GameManager.getPlayer().getComponent(PlayerController.class);
                    p.setBase_speed(p.getBase_speed() + 10f);
                    p.setSpeed(p.getBase_speed());
                    GameManager.getPlayer().getComponent(Pirate.class).spendPlunder(50);
                    dosh.setText(GameManager.getPlayer().getComponent(Pirate.class).getCurrentPlunder());
                }
            }
        });
        upgrades.add(speed_buy).padLeft(5f);;
        upgrades.row();
        upgrades.setFillParent(true);
        shopTable.add(upgrades);
//      shopWindow.add(shopTable);
        shopContainer = new Container <Table>();
        shopContainer.setActor(shopTable);
        shopTable.setFillParent(true);
        shopTable.setTransform(true);
        shopWindow.setVisible(false);
        shopWindow.add(shopContainer).center().bottom().fillX();
        actors.add(shopWindow);



        pauseScreen = new Window("", parent.skin);
        Table pauseTable = new Table();
        TextButton resume = new TextButton("Resume", parent.skin);
        TextButton restart = new TextButton("Restart", parent.skin);
        TextButton save = new TextButton("Save", parent.skin);
        restart.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                GameManager.reset();
                QuestManager.reset();
                Player p = GameManager.getPlayer();
                dosh.setText(p.getPlunder());
                p.getComponent(PlayerController.class).stopPowerups();
                Pirate pirate = p.getComponent(Pirate.class);
                pirate.setMaxHealth(pirate.getMaxHealth() - (health_level * 20));
                pirate.setAttackDmg(pirate.getDmg() - (float)(ammo_level * 20));
                PlayerController c = p.getComponent(PlayerController.class);
                c.setBase_speed(c.getBase_speed() - (float)(speed_level * 10));
                pirate.setMaxAmmo(pirate.getMaxAmmo() - (ammo_level * 20));
                pirate.setAmmo(pirate.getMaxAmmo());
                pirate.setHealth(pirate.getMaxHealth());
                health_level = 0;
                speed_level = 0;
                ammo_level = 0;
                damage_level = 0;
                timer_points = 0;
                paused = false;
            }
        });
        resume.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                paused = false;
            }
        });
        save.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                s = new SaveManager(health_level, ammo_level, speed_level, damage_level, timer_points, "");
            }
        });
        TextButton mainMenu = new TextButton("Main Menu", parent.skin);
        mainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameManager.reset();
                paused = false;
                parent.setScreen(parent.menu);
            }
        });
        Label pauseLabel = new Label("Pause", parent.skin);
        pauseLabel.setFontScale(2f);
        pauseTable.add(pauseLabel);
        pauseTable.row();
        pauseTable.add(resume).padBottom(10f);
        pauseTable.row();
        pauseTable.add(restart).padBottom(10f);
        pauseTable.row();
        pauseTable.add(save).padBottom(10f);
        pauseTable.row();
        pauseTable.add(mainMenu).padBottom(10f);

//        pauseTable.add(quit);
        pauseScreen.add(pauseTable);
        pauseScreen.setBackground(new TextureRegionDrawable(new Texture("shop_screen.png")));
        pauseScreen.setMovable(false);
        actors.add(pauseScreen);
        pauseScreen.setVisible(false);
    }

    public void loadPlayerUpgrades(int _damage_level, int _ammo_level, int _speed_level, int _health_level) {
        damage_level = _damage_level;
        damage_upg_bar.setDrawable(upgrade_bar_components[damage_level]);
        Pirate p = GameManager.getPlayer().getComponent(Pirate.class);
        p.setAttackDmg(p.getDmg() + (damage_level*20f));
        p.spendPlunder(50);
        dosh.setText(p.getCurrentPlunder());

        ammo_level = _ammo_level;
        ammo_upg_bar.setDrawable(upgrade_bar_components[ammo_level]);
        p.setMaxAmmo(p.getMaxAmmo() + (ammo_level * 20));
        p.setAmmo(p.getMaxAmmo());
        ammo.setText(p.getAmmo());
        p.spendPlunder(50);
        dosh.setText(p.getCurrentPlunder());

        speed_level = _speed_level;
        speed_upg_bar.setDrawable(upgrade_bar_components[speed_level]);
        PlayerController _p = GameManager.getPlayer().getComponent(PlayerController.class);
        _p.setBase_speed(_p.getBase_speed() +  (speed_level * 10f));
        _p.setSpeed(_p.getBase_speed());
        GameManager.getPlayer().getComponent(Pirate.class).spendPlunder(50);
        dosh.setText(GameManager.getPlayer().getComponent(Pirate.class).getCurrentPlunder());

        health_level = _health_level;
        health_upg_bar.setDrawable(upgrade_bar_components[health_level]);
        p.setMaxHealth(p.getMaxHealth() + (health_level * 20));
        p.setHealth(p.getMaxHealth());
        healthLabel.setText(p.getHealth());
        p.spendPlunder(50);
        dosh.setText(p.getCurrentPlunder());
    }


    /**
     * Returns a price tag of 50 coins.
     * */
    protected Image tag_50(){
        return new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_pricetags.txt"), "tag-50"));
    }
    /**
     * Returns a price tag of 30 coins.
     * */
    protected Image tag_30(){
        return new Image(ResourceManager.getSprite(ResourceManager.getId("upgrade_pricetags.txt"), "tag-30"));
    }
}
