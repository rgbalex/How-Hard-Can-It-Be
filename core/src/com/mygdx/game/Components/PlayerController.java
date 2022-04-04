package com.mygdx.game.Components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Managers.EntityManager;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderingManager;

import static com.mygdx.utils.Constants.HALF_DIMENSIONS;

/**
 * Responsible for the keyboard/mouse control of the player
 */
public class PlayerController extends Component {
    private Player player;
    private float speed;
    private int powerupTimer;
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
        this.speed = speed;
    }

    /**
     * Reads keyboard and mouse inputs, moving and shooting as required.
     */
    @Override
    public void update() {
        super.update();
        final float s = speed;
        if (powerupTimer > 0){
            if (powerupTimer == 1){
                stopPowerups();
            }
            powerupTimer--;
        }
        Vector2 dir = getDirFromWASDInput();
        ((Ship) parent).setShipDirection(dir);
        dir.scl(s);

        RigidBody rb = parent.getComponent(RigidBody.class);
        rb.setVelocity(dir);

        RenderingManager.getCamera().position.set(new Vector3(player.getPosition(), 0.0f));
        RenderingManager.getCamera().update();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();

            // in range 0 to VIEWPORT 0, 0 bottom left
            Vector2 delta = new Vector2(x, y);
            delta.sub(HALF_DIMENSIONS); // center 0, 0
            delta.nor();
            delta.y *= -1;
            // unit dir to fire
            ((Ship) parent).shoot(delta);
        }

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
        speed = speed * multiplier;
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
     * Sets all powerup related flags to false and restores speed to setting specified in the JSON file.
     * */
    public void stopPowerups(){
        player.setPoweredUp(false);
        player.setInvincible(false);
        player.setWeatherResistant(false);
        player.setSpedUp(false);
        powerupTimer = 0;
        speed = GameManager.getSettings().get("starting").getFloat("playerSpeed"); //resets speed
    }

    public int getPowerupTimer(){return powerupTimer;}

}
