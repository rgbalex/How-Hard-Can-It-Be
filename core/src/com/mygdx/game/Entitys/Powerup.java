package com.mygdx.game.Entitys;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.Components.*;
import com.mygdx.game.Managers.EntityManager;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.game.Physics.CollisionCallBack;
import com.mygdx.game.Physics.CollisionInfo;
import com.mygdx.game.Physics.PhysicsBodyType;

public class Powerup extends Entity implements CollisionCallBack {
    /** A collectable powerup that appears on the playing field.
    *
    * Can be of 5 different types:
    *   0 - health repair - resets player's health to full (sprite : hammer)
    *   1 - ammo refill - resets player's ammo to full (sprite : 3 cannonballs)
    *   2 - bad weather resistance - makes player unaffected by bad weather for 10 seconds (sprite : xxx bottle)
    *   3 - invincibility - makes player unaffected by enemy cannonballs/seamonsters/bad weather for 10 seconds (sprite : mario star)
    *   4 - increased speed - doubles the player's speed for 10 seconds (sprite : ship with arrow overlayed)
    * Like cannonballs, there is a fixed amount of powerups created on startup.
     * Upon contact with player, the powerup moves offscreen until it is placed somewhere again.
     * Powerups cannot be compounded (e.g can't experience invincibility and increased speed),
     * but health and ammo refills work even when under the effect of another powerup.
    * */
    private int type;
    private boolean justCollected;
    public Powerup(int type){
        super();
        this.type = type;
        justCollected = false;
        Transform t = new Transform();
        t.setPosition(-1000, 1000); // created offscreen
        t.setScale(0.5f, 0.5f);
        Renderable r = new Renderable(
                ResourceManager.getId(
                "upgrades_powerups.txt"),
                "powerup-" + type,
                RenderLayer.Transparent
        );
        RigidBody rb = new RigidBody(PhysicsBodyType.Static, r, t, false);
        rb.setCallback(this);
        addComponents(t, r, rb);
    }

    public void place(float x, float y){
        getComponent(Transform.class).setPosition(x, y);
    }

    /**
     * Changes the powerup's type (effect on player) and sprite.
     * */
    public void updateType(int newtype){
        Sprite s = ResourceManager.getSprite(ResourceManager.getId("upgrades_powerups.txt"), "powerup-" + newtype);
        getComponent(Renderable.class).setTexture(s);
        type = newtype;
    }

    @Override
    public void update(){
        super.update();
        if (justCollected){
            justCollected = false;
            moveOffScreen();
        }
    }

    public void moveOffScreen(){
        place(-1000f, -1000f);
    }

    public void applyPowUp(Player p){
        switch (type){
            case 0: // health refill
                p.getComponent(Pirate.class).setHealth(GameManager.getSettings().get("starting").getInt("health"));
                break;
            case 1: // ammo refill
                p.getComponent(Pirate.class).setAmmo(GameManager.getSettings().get("starting").getInt("ammo"));
                break;
            case 2: //bad weather res
                if (!(p.isPoweredUp())){
                    p.getComponent(PlayerController.class).gainWeatherRes(4);
                }
                break;
            case 3: //invincibility
                if (!(p.isPoweredUp())){
                    p.getComponent(PlayerController.class).gainInvincibility(4);
                }
                break;
            case 4: //speed boost
                if (!(p.isPoweredUp())){
                    p.getComponent(PlayerController.class).gainSpeed(2f, 4);
                }
                break;
        }
    }

    @Override
    public void EnterTrigger(CollisionInfo info) {

    }

    @Override
    public void BeginContact(CollisionInfo info) {
        if (info.b instanceof Player) {
            justCollected = true;
            applyPowUp(((Player) info.b));
        }
    }

    @Override
    public void EndContact(CollisionInfo info) {

    }


    @Override
    public void ExitTrigger(CollisionInfo info) {

    }
}