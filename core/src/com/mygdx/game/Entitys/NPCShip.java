package com.mygdx.game.Entitys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.AI.EnemyState;
import com.mygdx.game.Components.*;
import com.mygdx.game.Managers.EntityManager;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.game.Physics.CollisionCallBack;
import com.mygdx.game.Physics.CollisionInfo;
import com.mygdx.utils.QueueFIFO;
import com.mygdx.utils.Utilities;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

import static com.mygdx.utils.Constants.HALF_DIMENSIONS;

/**
 * NPC ship entity class.
 */
public class NPCShip extends Ship implements CollisionCallBack {
    public StateMachine<NPCShip, EnemyState> stateMachine;
    private static JsonValue AISettings;
    private final QueueFIFO<Vector2> path;
    private Renderable green, red;
    private boolean isDead;
    private int cannonTimer; // countdown to the next shot

    /**
     * Creates an initial state machine
     */
    public NPCShip() {
        super();
        path = new QueueFIFO<>();

        if (AISettings == null) {
            AISettings = GameManager.getSettings().get("AI");
        }

        stateMachine = new DefaultStateMachine<>(this, EnemyState.WANDER);
        isDead = false;
        setName("NPC");
        AINavigation nav = new AINavigation();
        addComponents(nav);
        green = new Renderable(ResourceManager.getId("progress_bar_green.png"), RenderLayer.Transparent);
        red = new Renderable(ResourceManager.getId("progress_bar_red.png"), RenderLayer.Transparent);

        RigidBody rb = getComponent(RigidBody.class);

        JsonValue starting = GameManager.getSettings().get("starting");
        cannonTimer = (int) (2 / EntityManager.getDeltaTime());
        // agro trigger
        rb.addTrigger(10000f, "followPlayer"); // for ally ships only, ignored for enemy ships
        rb.addTrigger(Utilities.tilesToDistance(starting.getFloat("argoRange_tiles")), "agro");
        addComponents(green, red);
        green.setOffset(0f, 32f);
        green.setSize(32f, 5f);
        red.setOffset(32f, 32f);
        red.setSize(5f, 5f);
        green.show();
        red.show();


    }

    /**
     * gets the top of targets from pirate component
     *
     * @return the top target
     */
    private Ship getTarget() {
        return getComponent(Pirate.class).getTarget();
    }


    /**
     * updates the state machine
     */
    @Override
    public void update() {
        if(getComponent(Pirate.class).isAlive()) {
            super.update();
            stateMachine.update();
            if(getTarget() != null && !getTarget().isAlive()){
                getComponent(Pirate.class).removeTarget();
            }
            if (getHealth() == 100f) {
                red.hide();
                green.hide();
            } // if not harmed, health bar removed to avoid visual clutter
            else {
                red.show();
                green.show();
                //resizing green and red components according to current health
                float green_ratio = getHealth() / 100f;
                green.setSize(32f * green_ratio, 5f);
                red.setSize(32f * (1f - green_ratio), 5f);
                red.setOffset(32 * green_ratio, 32f);
            }

            if (getComponent(Pirate.class).canAttack()) {
                if (cannonTimer > 0) {
                    cannonTimer -= 1;
                }
                else{
                    Ship target = getComponent(Pirate.class).getTarget(); // shoot at target that arrived last
                    if((!(isAlly() && target instanceof Player))){ // allies dont shoot at the player, just follow them
                        shootAt(target.getPosition());
                    }
                    if (!target.isAlive()){getComponent(Pirate.class).getTargets().pop();} // killed this target, remove from list
                    cannonTimer = (int) (2 / EntityManager.getDeltaTime()); // reset cannon timer
                }
            }
            if(isAlly() && getTarget() instanceof NPCShip && getComponent(Pirate.class).canAttack() && getTarget().isAlive()){
                if(cannonTimer > 0){cannonTimer -= 1;}
                else {
                    if (getTarget().isAlive()) {
                        shootAt(getTarget().getPosition());
                        cannonTimer = (int) (2 / EntityManager.getDeltaTime()); // reset cannon timer
                        getComponent(Pirate.class).getTargets().remove(getTarget());
                    } else {
                        getComponent(Pirate.class).getTargets().remove(getTarget());
                    }
                }
            }
        }
        else{ // placing the npc offscreen
            for (Renderable r : getComponents(Renderable.class)){
                r.hide();
            }
            Transform t = getComponent(Transform.class);
            t.setPosition(100000, 100000);

            RigidBody rb = getComponent(RigidBody.class);
            rb.setPosition(t.getPosition());
            rb.setVelocity(0, 0);
            getComponent(AINavigation.class).stop(); // just in case
        }
    }


    /**
     * is meant to path find to the target but didn't work
     */
    public void goToTarget() {
        /*path = GameManager.getPath(
                Utilities.distanceToTiles(getPosition()),
                Utilities.distanceToTiles(getTarget().getPosition()));*/
    }
    /**
     * Shoots from ship's position to a given position
     * (As opposed to regular shoot(), which shoots from ship's position at a given **direction** )
     * */
    public void shootAt(Vector2 pos){
        shoot(pos.sub(getPosition()));
    }

    /**
     * creates a new steering behaviour that will make the NPC beeline for the target doesn't factor in obstetrical
     */
    public void followTarget() {
            if (getTarget() == null) {
                stopMovement();
                return;
            }
            AINavigation nav = getComponent(AINavigation.class);

            Arrive<Vector2> arrives = new Arrive<>(nav,
                    getTarget().getComponent(Transform.class))
                    .setTimeToTarget(AISettings.getFloat("accelerationTime"))
                    .setArrivalTolerance(AISettings.getFloat("arrivalTolerance"))
                    .setDecelerationRadius(AISettings.getFloat("slowRadius"));

            nav.setBehavior(arrives);
    }

    /**
     * stops all movement and sets the behaviour to null
     */
    public void stopMovement() {
        AINavigation nav = getComponent(AINavigation.class);
        nav.setBehavior(null);
        nav.stop();
    }

    /**
     * Meant to cause the npc to wander
     */
    public void wander() {

    }
    /**
     * Returns whether the ship belongs to the same faction as the player
     * Allied ships will shoot at ships of different factions but not at the player.
     * */
    public boolean isAlly(){
        return getFactionId() == GameManager.getPlayer().getFactionId();
    }

    @Override
    public void BeginContact(CollisionInfo info) {
        if(!(info.fB.isSensor())) { // prevents bullets from registering collisions upon entering ship's 'agro' range
            if (info.a instanceof CannonBall && ((CannonBall) info.a).getShooter() instanceof Player && !isAlly()) { // shot by player and hit the npc ship of an enemy faction
                CannonBall ball = ((CannonBall) info.a);
                Ship shooter = (Ship) ball.getShooter();
                ball.kill();
                if(getHealth() <= shooter.getComponent(Pirate.class).getDmg() && getHealth() > 0){ // will die from this shot and has not died yet
                    getComponent(Pirate.class).takeDamage(shooter.getComponent(Pirate.class).getDmg());
                    shooter.plunder(100);
                    getComponent(Pirate.class).kill();
                    return;
                }
                else {
                    getComponent(Pirate.class).takeDamage(shooter.getComponent(Pirate.class).getDmg());
                }
            }
            else if ((info.a instanceof CannonBall && ((CannonBall) info.a).getShooter() instanceof Ship && ((Ship)((CannonBall) info.a).getShooter()).getFactionId() != getFactionId()) && (isAlly())) {
                CannonBall ball = ((CannonBall) info.a);
                Ship shooter = (Ship) ball.getShooter();
                getComponent(Pirate.class).takeDamage(shooter.getComponent(Pirate.class).getDmg());
                ball.kill();
                getComponent(Pirate.class).addTarget(shooter);
                cannonTimer = (int) (2 / EntityManager.getDeltaTime()); // reset cannon timer
            }
        }

    }

    @Override
    public void EndContact(CollisionInfo info) {

    }

    /**
     * if the agro fixture hit a ship set it as the target
     *
     * @param info the collision info
     */
    @Override
    public void EnterTrigger(CollisionInfo info) {
        if(info.fB.getUserData() == "agro" && !isAlly()){ // allies dont care, they will only help the player out in combat
            if (info.a instanceof Ship) {
                Ship other = (Ship) info.a;
                if ((Objects.equals(other.getComponent(Pirate.class).getFaction().getName(), getComponent(Pirate.class).getFaction().getName())) || (isAlly() && other instanceof Player)) {
                    // is the same faction
                    return;
                }
                // add the new collision as a new target
                Pirate pirate = getComponent(Pirate.class);
                if(!pirate.getTargets().contains(other)) {
                    pirate.addTarget(other);
                }
                cannonTimer = (int) (2 / EntityManager.getDeltaTime());
            }
        }
        else if (info.fB.getUserData() == "followPlayer" && isAlly()){
            if(info.a instanceof Player && !(getComponent(Pirate.class).getTargets().contains((Ship) info.a))){
                Pirate pirate = getComponent(Pirate.class);
                pirate.addTarget((Player) info.a);
            }
        }

    }

    /**
     * if a target has left remove it from the potential targets Queue
     *
     * @param info collision info
     */
    @Override
    public void ExitTrigger(CollisionInfo info) {
        if(info.fB.getUserData() == "agro"){
            if ((info.a instanceof Ship)) {
                Pirate pirate = getComponent(Pirate.class);
                Ship o = (Ship) info.a;
                // remove the object from the targets list
                for (Ship targ : pirate.getTargets()) {
                    if (targ == o) {
                        pirate.getTargets().remove(targ);
                        break;
                    }
                }
            }
        }
    }
}
