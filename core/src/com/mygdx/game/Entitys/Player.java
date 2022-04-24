package com.mygdx.game.Entitys;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.PlayerController;
import com.mygdx.game.Components.Renderable;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.ResourceManager;

/**
 * Player's ship entity.
 */
public class Player extends Ship {

    /**
     * Adds ship with PlayerController component and sets its speed.
     *
     * @param speed of movement across map
     */
    private boolean poweredUp; // true when the player is under the effect of a powerup (excluding health and ammo refills.)
    private boolean invincible; // true when under the effect of a mario star (invincibility powup)
    private boolean WeatherResistant; // true when under the effect of weather resistance
    private boolean SpedUp;
    private boolean badWeather = false;
    private Renderable pointerArrow;

    private Player(float speed) {
        super();
        poweredUp = false;
        PlayerController pc = new PlayerController(this, speed);
        pointerArrow = new Renderable(ResourceManager.getId("arrow.png"), RenderLayer.Transparent);
        pointerArrow.setSize(16, 8);
        pointerArrow.setOffset(8f, 36f);
        addComponents(pc, pointerArrow);
        setName("Player");
    }

    /**
     * Adds ship with PlayerController component, loading its speed from GameManager settings.
     */
    public Player() {
        this(GameManager.getSettings().get("starting").getFloat("playerSpeed"));
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
    }
    public int getAmmo() {
        return getComponent(Pirate.class).getAmmo();
    }

    public boolean isPoweredUp(){return poweredUp;}

    public void setPoweredUp(boolean p){poweredUp = p;}

    public boolean isInvincible() { return invincible; }

    public void setInvincible(boolean invincible) { this.invincible = invincible; }

    public boolean isWeatherResistant() { return WeatherResistant; }

    public void setWeatherResistant(boolean weatherResistant) { WeatherResistant = weatherResistant; }

    public boolean isSpedUp() { return SpedUp; }

    public void setSpedUp(boolean spedUp) { SpedUp = spedUp; }

    public void setBadWeather(boolean weather) {badWeather = weather;}

    public boolean getBadWeather() {return badWeather;}

    public void setPlayer() {
        this.isPlayer = true;
    }

    public void setHealth(int health) {getComponent(Pirate.class).setHealth(health);}
    public void setPlunder(int plunder) {getComponent(Pirate.class).setPlunder(plunder);}
    public void setCannonBalls(int cannonBalls) {getComponent(Pirate.class).setAmmo(cannonBalls);}
    public Renderable getPointerArrow(){return pointerArrow;}

}
