package com.mygdx.game.Components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Managers.EntityManager;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.QuestManager;
import com.mygdx.game.Managers.RenderingManager;
import com.mygdx.game.Quests.KillDuckQuest;
import com.mygdx.game.Quests.KillQuest;
import com.mygdx.game.Quests.LocateQuest;
import com.mygdx.game.Quests.Quest;
import com.mygdx.utils.Utilities;

import static com.mygdx.utils.Constants.HALF_DIMENSIONS;

/**
 * Responsible for the keyboard/mouse control of the player
 */
public class PlayerController extends Component {
    private Player player;
    private float current_speed;
    private float base_speed; // when not powered up
    private int powerupTimer;
    private final Vector2 modifier = new Vector2(-16,-24);
    public PlayerController() {
        super();
        powerupTimer = 0;
        type = ComponentType.PlayerController;
        setRequirements(ComponentType.RigidBody);
    }

    /**
     * @param player the parent
     * @param speed  speed
     */
    public PlayerController(Player player, float speed) {
        this();
        this.player = player;
        this.current_speed = speed;
        this.base_speed = speed;
    }

    /**
     * Reads keyboard and mouse inputs, moving and shooting as required.
     * This function tracks spacebar presses to shoot in the direction of the ship.
     * GameScreen tracks mouse clicks to shoot in direction of the mouse, ignoring on screen button presses.
     */
    @Override
    public void update() {
        super.update();
        final float s = current_speed;
        if (QuestManager.anyQuests()){
            Quest q = QuestManager.currentQuest();
            Vector2 pos;
            if (q instanceof KillQuest){
                pos = ((KillQuest) q).getLocation();
            }
            else if (q instanceof LocateQuest){
                pos = ((LocateQuest) q).getLocation();
            }
            else {
                pos = GameManager.getLongboi().getPosition();
            }
            Vector2 ppos = ((Player) parent).getPosition();
            if (Utilities.distanceToTiles(pos.dst(ppos)) > 7){
                ((Player) parent).getPointerArrow().show();
                ((Player) parent).getPointerArrow().getSprite().setRotation(((Player)parent).angleTo(pos));
            }
            else{
                ((Player) parent).getPointerArrow().hide();
            }
        }
        else{
            ((Player) parent).getPointerArrow().hide();
        }
        if (powerupTimer > 0){
            if (powerupTimer == 1){
                stopPowerups();
            }
            powerupTimer--;
        }
        Vector2 dir = getDirFromWASDInput();
        ((Ship) parent).setShipDirection(dir);
        dir.scl(s);
//        Here we can scale speed and weather effects by difficulty.
//        modifier.scl(difficulty);
        RigidBody rb = parent.getComponent(RigidBody.class);
        if (player.getBadWeather()) {
            if (!player.isWeatherResistant()) {
                dir = dir.add(modifier);
            }
        }
        rb.setVelocity(dir);

        RenderingManager.getCamera().position.set(new Vector3(player.getPosition(), 0.0f));
        RenderingManager.getCamera().update();



        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // unit dir to fire
            ((Ship) parent).shoot();
        }
    }

    /**
     * Converts WASD or arrows to direction of travel
     *
     * @return -1 <= (x, y) <= 1
     */
    private Vector2 getDirFromWASDInput() {
        Vector2 dir = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.DPAD_UP)) {
            dir.y += 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN)) {
            dir.y -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) {
            dir.x -= 1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) {
            dir.x += 1;
        }
        return dir;
    }
    /**
     * Multiplies speed by a given amount for a given amount of time, then undoes the speed gain
     *
     * @param multiplier the factor by which the speed increases
     * @param duration the duration (in seconds) that this powerup is active for
     * */
    public void gainSpeed(float multiplier, int duration){
        player.setPoweredUp(true);
        player.setSpedUp(true);
        powerupTimer = (int) (duration / EntityManager.getDeltaTime());
        current_speed = current_speed * multiplier;
    }

    /**
     * Multiplies speed by a given amount for a given amount of time, then undoes the speed gain
     * @param duration the duration (in seconds) that this powerup is active for
     * */
    public void gainInvincibility(int duration){
        player.setPoweredUp(true);
        player.setInvincible(true);
        powerupTimer = (int) (duration / EntityManager.getDeltaTime());
        // invincibility stuff
    }

    /**
     * Multiplies speed by a given amount for a given amount of time, then undoes the speed gain
     * @param duration the duration (in seconds) that this powerup is active for
     * */
    public void gainWeatherRes(int duration){
        player.setPoweredUp(true);
        player.setWeatherResistant(true);
        powerupTimer = (int) (duration / EntityManager.getDeltaTime());
    }

    /**
     * Sets all powerup related flags to false and restores speed to current speed upgrade level.
     * */
    public void stopPowerups(){
        player.setPoweredUp(false);
        player.setInvincible(false);
        player.setWeatherResistant(false);
        player.setSpedUp(false);
        powerupTimer = 0;
        current_speed = base_speed; //resets speed
    }

    public int getPowerupTimer(){return powerupTimer;}
    public void setSpeed(float newSpeed){current_speed = newSpeed;}
    public void setBase_speed(float newSpeed){base_speed = newSpeed;}
    public float getBase_speed(){return base_speed;}
    public float getSpeed(){return current_speed;}

}
