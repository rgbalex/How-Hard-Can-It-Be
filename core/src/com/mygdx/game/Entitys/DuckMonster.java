package com.mygdx.game.Entitys;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.Components.Pirate;
import com.mygdx.game.Components.Renderable;
import com.mygdx.game.Components.RigidBody;
import com.mygdx.game.Components.Transform;
import com.mygdx.game.Managers.EntityManager;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.game.Physics.CollisionCallBack;
import com.mygdx.game.Physics.CollisionInfo;
import com.mygdx.game.Physics.PhysicsBodyType;
import com.mygdx.utils.Utilities;
/**
 * Duck monster entity (inspired by longboi)
 * Spawns as part of quest
 * Shoots egg cannonballs at player and has a poisonous area around it that harms the player
 * Doesnt move, but turns to face the player
 * Once killed, player gets a bunch of plunder.
 *
 * Has a healthbar, like NPC ships.
 * */
public class DuckMonster extends Entity implements CollisionCallBack {
    private final Vector2 currentDir;
    private boolean isActive; // if 'active', it is fighting the player as part of a quest, otherwise it is offscreen
    private Renderable poison; //green circle, which hurts the player upon entering (duck lives on campus lake so is poisonous af from the water)
    private int cannonTimer;
    private Renderable green, red; // healthbar
    private Renderable sprite;
    private int poisonTimer; // to damage player if they are in poison range
    private boolean poisoning;
    private int maxHealth;
    private String dir;

    public DuckMonster(){
        super();
        currentDir = new Vector2(0, 1);
        Transform t = new Transform();
        t.setPosition(10000, 10000); // spawned offscreen
        t.setScale(2f, 2f);
        int atlas_id = ResourceManager.getId("longboi_moveset.txt");
        Pirate p = new Pirate();
        poison = new Renderable(ResourceManager.getId("poison.png"), RenderLayer.Transparent);
        poison.getSprite().setAlpha(0.5f);
        poison.setSize(128f, 128f);
        poison.setOffset(-32f, -32f);
        poison.hide();
        poisoning = false;
        poisonTimer = (int) (3 / EntityManager.getDeltaTime());
        isActive = false;
        cannonTimer = (int) (2 / EntityManager.getDeltaTime());
        green = new Renderable(ResourceManager.getId("progress_bar_green.png"), RenderLayer.Transparent);
        red = new Renderable(ResourceManager.getId("progress_bar_red.png"), RenderLayer.Transparent);
        green.setOffset(0f, 64f);
        green.setSize(32f, 5f);
        red.setOffset(32f, 64f);
        red.setSize(5f, 5f);
        green.hide();
        red.hide();
        sprite = new Renderable(atlas_id, "duck-up", RenderLayer.Transparent);
        sprite.hide();
        RigidBody rb = new RigidBody(PhysicsBodyType.Static, sprite, t, false);
        JsonValue starting = GameManager.getSettings().get("starting");
        rb.addTrigger(Utilities.tilesToDistance(starting.getFloat("argoRange_tiles")), "agro");
        rb.addTrigger(128f, "poison");
        rb.setCallback(this);
        addComponents(t, sprite, rb, poison, p, red, green);
        maxHealth = p.getHealth();
        dir = "up";
    }
    /**
     * Called upon beginning of "Kill longboi" quests.
     * Places the duck in a given spot and sets it as active.
     * Renders poison aura as visible.
     * @param x x coordinate of new position
     * @param y y coordinate of new position
     *
     * */
    public void place(float x, float y) {
        isActive = true;
        getComponent(Transform.class).setPosition(x, y);
        poison.show();
    }
    /**
     * Removes the duck off screen and sets health back to maximum.
     * */
    public void deactivate(){
        isActive = false;
        getComponent(Transform.class).setPosition(10000f, 10000f);
        getComponent(Pirate.class).setHealth(maxHealth);
    }

    public boolean isActive(){return isActive;}
    public String getDir(){return dir;}
    /**
     * Called every frame.
     * Calculates the sprite required to make the duck face the player.
     * Counts down to the next shot and shoots if player is in agro range.
     * Updates healthbar to reflect current health
     * If player is in the poison aura, counts down to the next time the player is hurt by the aura
     * */
    @Override
    public void update(){
        if(isActive){
            // turning duck to face player
            float angle = angleTo(GameManager.getPlayer().getPosition());
            if (angle < 22.5f && angle > -22.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-right"));
                dir = "right";
            }
            else if (angle > 22.5f && angle < 67.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-ur")); //top-right
                dir = "ur";
            }
            else if (angle > 67.5f && angle < 112.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-up"));
                dir = "up";
            }
            else if (angle > 112.5f && angle < 157.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-ul")); //top-left
                dir = "ul";
            }
            else if ((angle > 157.5f && angle < 180f) || (angle < -157.5f && angle > -180f)){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-left"));
                dir = "left";
            }
            else if (angle < -22.5f && angle > -67.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-dr")); //bottom-right
                dir = "dr";
            }
            else if (angle < -67.5f && angle > -112.5f){
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-down"));
                dir = "down";
            }
            else{
                sprite.setTexture(ResourceManager.getSprite(ResourceManager.getId("longboi_moveset.txt"), "duck-dl")); //bottom-left
                dir = "dl";
            }
            if (getHealth() == 100f) {
                red.hide();
                green.hide();
            } // if not harmed, health bar removed to avoid visual clutter
            else {
                red.show();
                green.show();
                //resizing green and red components according to current health
                float green_ratio = (float) getHealth() / (float) maxHealth; // cast to float or division by zero errors ensue
                green.setSize(32f * green_ratio, 5f);
                red.setSize(32f * (1f - green_ratio), 5f);
                red.setOffset(64f * green_ratio, 64f);
            }

            sprite.show();
            if(getComponent(Pirate.class).getTarget() != null){
                if(cannonTimer < 0) {
                    shootAt(getComponent(Pirate.class).getTarget().getPosition());
                    cannonTimer = (int) (2 / EntityManager.getDeltaTime());
                }
                else{
                    cannonTimer --;
                }
            }
            if(poisoning){
                poisonTimer --;
                if (poisonTimer <= 0){
                    GameManager.getPlayer().getComponent(Pirate.class).takeDamage(3f);
                    poisonTimer = (int) (3 / EntityManager.getDeltaTime());
                }
            }
            if(getHealth() <= 0){
                isActive = false;
                remove();
            }
        }
    }
    /**
     * Gets the angle between the x axis and the line between the duck and the point specified by pos
     * @param pos the point to 'draw' the line to.
     * */
    public float angleTo (Vector2 pos){
        Transform t = getComponent(Transform.class);
        float b = pos.x - t.getPosition().x;
        float a = pos.y - t.getPosition().y;
        return MathUtils.atan2(a, b) * 180f / 3.14f;
    }
    public int getHealth(){return getComponent(Pirate.class).getHealth();}

    public boolean isAlive(){
        return getComponent(Pirate.class).isAlive();
    }

    /**
     * Shoots from duck's position to a given position
     * (As opposed to regular shoot(), which shoots from ship's position at a given **direction** )
     * */
    public void shootAt(Vector2 pos){
        getComponent(Pirate.class).shoot(pos.sub(getPosition()));
    }

    public Vector2 getPosition(){
        return getComponent(Transform.class).getPosition();
    }

    /**
     * Upon collision with a cannonball fired by the player, kill the cannonball and take damage.
     * */
    @Override
    public void BeginContact(CollisionInfo info) {
        if(!info.fB.isSensor()){
            try {
                if (info.a instanceof CannonBall && ((CannonBall) info.a).getShooter() instanceof Player) {
                    Player p = (Player) ((CannonBall) info.a).getShooter();
                    getComponent(Pirate.class).takeDamage(p.getComponent(Pirate.class).getDmg());
                    ((CannonBall) info.a).kill();
                }
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
    }

    @Override
    public void EndContact(CollisionInfo info) {

    }
    /**
     * Adds player to target if agro range entered.
     * Tells duck to begin counting down to poison the player if poison range entered.
     * */
    @Override
    public void EnterTrigger(CollisionInfo info) {
        if(info.fB.getUserData() == "agro"){
            if(info.a instanceof Player && !getComponent(Pirate.class).getTargets().contains((Ship) info.a)){
                getComponent(Pirate.class).addTarget((Ship) info.a);
                cannonTimer = (int) (2 / EntityManager.getDeltaTime());
            }
        }
        if (info.fB.getUserData() == "poison" && info.a instanceof Player){
            poisoning = true;
            poisonTimer = (int) (3 / EntityManager.getDeltaTime());
        }
    }

    public int getPoisonTimer(){
        return poisonTimer;
    }

    @Override
    public void ExitTrigger(CollisionInfo info) {
        if(info.fB.getUserData() == "agro"){
            if(info.a instanceof Player){
                getComponent(Pirate.class).getTargets().pop();
            }
        }
        if (info.fB.getUserData() == "poison" && info.a instanceof Player){
            poisoning = false;
        }
    }
    /**
     * Sets baseline health level for seamonster and sets current health level to the max.
     * Because the seamonster is not revived (or healed) during the game, this method is used purely on spawn (Menuscreen class)
     * */
    public void setHealth(int newHealth){
        getComponent(Pirate.class).setHealth(newHealth);
        maxHealth = newHealth;
    }
    /**
     * Places the duck offscreen and makes renderable invisible
     * */
    public void remove(){
        for(Renderable r : getComponents(Renderable.class)){
            r.hide();
        }
        getComponent(Transform.class).setPosition(1000f, 1000f);
    }

}
