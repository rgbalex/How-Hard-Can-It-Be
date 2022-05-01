package com.mygdx.game.Entitys;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
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

import static com.mygdx.utils.Constants.BUILDING_SCALE;

/**
 * Buildings that you see in game.
 */
public class Building extends Entity implements CollisionCallBack {
    private String buildingName;
    private static int atlas_id;
    private boolean isFlag;
    private boolean isDefender; // if true, building acts as defender for its college (shoots and has health instead of insta dying)
    private int cannonTimer;
    private Renderable green, red;

    Building() {
        super();
        isFlag = false;
        Transform t = new Transform();
        t.setScale(BUILDING_SCALE, BUILDING_SCALE);
        Pirate p = new Pirate();
        atlas_id = ResourceManager.getId("Buildings.txt");
        Renderable r = new Renderable(atlas_id, "big", RenderLayer.Transparent);
        addComponents(t, p, r);
        isDefender = false;
        green = new Renderable(ResourceManager.getId("progress_bar_green.png"), RenderLayer.Transparent);
        red = new Renderable(ResourceManager.getId("progress_bar_red.png"), RenderLayer.Transparent);
        addComponents(green, red);
        green.setOffset(0f, 32f);
        green.setSize(32f, 5f);
        red.setOffset(32f, 32f);
        red.setSize(5f, 5f);
        green.show();
        red.show();
    }

    /**
     * Flags are indestructible and mark college locations.
     *
     * @param isFlag set to true to create a flag
     */
    Building(boolean isFlag) {
        this();
        this.isFlag = isFlag;
    }

    public boolean isNonFlag(){
        return !isFlag;
    }

    public boolean isDefender(){return isDefender;}

    /**
     * Creates a building with the given name at the specified location.
     *
     * @param pos  2D position vector
     * @param name name of building
     */
    public void create(Vector2 pos, String name) {
        Sprite s = ResourceManager.getSprite(atlas_id, name);
        Renderable r = getComponent(Renderable.class);
        r.setTexture(s);
        getComponent(Transform.class).setPosition(pos);
        buildingName = name;

        RigidBody rb = new RigidBody(PhysicsBodyType.Static, r, getComponent(Transform.class));
        rb.setCallback(this);
        addComponent(rb);
    }
    /**
     * Marks building as one that can shoot at player and adds a trigger for the player entering within aggro range.
     * */
    public void makeDefender(){
        isDefender = true;
        cannonTimer = (int) (3f / EntityManager.getDeltaTime());
        getComponent(RigidBody.class).addTrigger(500, "agro_college");
    }

    public int getHealth(){
        return getComponent(Pirate.class).getHealth();
    }

    /**
     * Replace the building with ruins and mark as broken.
     */
    private void destroy() {
        if (isFlag) {
            return;
        }
        Sprite s = ResourceManager.getSprite(atlas_id, buildingName + "-broken");
        Renderable r = getComponent(Renderable.class);
        r.setTexture(s);
        getComponent(Pirate.class).kill();
        red.hide();
        green.hide();
    }

    /**
     * Called on every frame, does nothing for non - defenders (buildings incapable of shooting)
     * For defenders, waits 3 seconds and shoots at player if within agro range.
     * For defenders, updates healthbar to reflect current health level.
     * */
    @Override
    public void update(){
        if (getComponent(Pirate.class).isAlive() && isDefender){
            if (getComponent(Pirate.class).getTarget() instanceof Player){
                if (cannonTimer == 0){
                    getComponent(Pirate.class).shoot(GameManager.getPlayer().getPosition().sub(getComponent(Transform.class).getPosition()).scl(100f));
                    cannonTimer = (int) (3f / EntityManager.getDeltaTime());
                }
                else{
                    cannonTimer --;
                }
            }
            if (getHealth() == getComponent(Pirate.class).getMaxHealth()) {
                red.hide();
                green.hide();
            } // if not harmed, health bar removed to avoid visual clutter
            else {
                red.show();
                green.show();
                //resizing green and red components according to current health
                float green_ratio = (float) getHealth() / (float) getComponent(Pirate.class).getMaxHealth();
                green.setSize(32f * green_ratio, 5f);
                red.setSize(32f * (1f - green_ratio) * BUILDING_SCALE, 5f);
                red.setOffset(32 * green_ratio * BUILDING_SCALE, 32f);
            }

        }
        else if (!getComponent(Pirate.class).isAlive() && isDefender){
            destroy();
        }
        else if (!isDefender){
            red.hide();
            green.hide();
        }
    }
    /**
     * Used on reset of the game screen.
     * Sets health back to 100 and, if destroyed, changes the sprite back to undestroyed version
     * */
    public void revive(){
        if (isFlag) {
            return;
        }
        Sprite s = ResourceManager.getSprite(atlas_id, buildingName);
        Renderable r = getComponent(Renderable.class);
        r.setTexture(s);
        Pirate p = getComponent(Pirate.class);
        p.setHealth(100);
        if (p.getTarget() != null) {p.removeTarget();}
        red.hide();
        green.hide();

    }

    public boolean isAlive() {
        return getComponent(Pirate.class).isAlive();
    }

    @Override
    public void BeginContact(CollisionInfo info) {

    }

    @Override
    public void EndContact(CollisionInfo info) {

    }

    /**
     * If the building is not a defender (doesnt shoot at player), kills the building and the cannonball that hit it.
     * Otherwise harms the building and kills the cannonball
     * For defenders, player entering the aggro range makes the building add it as a target.
     *
     * @param info CollisionInfo container
     */
    @Override
    public void EnterTrigger(CollisionInfo info) {
        if(!info.fB.isSensor()){
            if (info.a instanceof CannonBall && isAlive() && !isFlag) { // bullets travel through flags
                CannonBall b = (CannonBall) info.a;
                // the ball if from the same faction
            /*if(Objects.equals(b.getShooter().getComponent(Pirate.class).getFaction().getName(),
                    getComponent(Pirate.class).getFaction().getName())) {
                return;
            }*/
                if (! (b.getShooter() instanceof Building)){
                    if (b.getShooter() instanceof Player && !isDefender) {
                        destroy();
                    } else if (b.getShooter() instanceof Player && isDefender) {
                        getComponent(Pirate.class).takeDamage(info.b.getComponent(Pirate.class).getDmg());
                    }
                    ((CannonBall) info.a).kill();
                }
            }
        }
        else if(info.a instanceof Player){
            getComponent(Pirate.class).addTarget((Player) info.a);
            cannonTimer = (int) (4f / EntityManager.getDeltaTime());
        }
    }
    /**
     * For defender buildings, the player leaving the aggro range removes the target from the building's list of targets
     * @param info CollisionInfo container
     * */
    @Override
    public void ExitTrigger(CollisionInfo info) {
        if (info.fB.isSensor() && info.a instanceof Player){
            getComponent(Pirate.class).removeTarget();
        }

    }

    public void hide() {
        for (Renderable r : getComponents(Renderable.class)){
            r.hide();
        }
        Transform t = getComponent(Transform.class);
        t.setPosition(100000, 100000);

        RigidBody rb = getComponent(RigidBody.class);
        rb.setPosition(t.getPosition());
        rb.setVelocity(0, 0);
    }
}
