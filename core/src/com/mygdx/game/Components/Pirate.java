package com.mygdx.game.Components;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.Entitys.NPCShip;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Entitys.Ship;
import com.mygdx.game.Faction;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.RenderingManager;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.utils.QueueFIFO;

/**
 * Gives the concepts of health plunder, etc. Allows for firing of cannonballs, factions, death, targets
 */
public class Pirate extends Component {
    private int points;
    private int factionId;
    private int plunder;
    private int currentPlunder;
    protected boolean isAlive;
    private int health;
    private int ammo;
    //The following fields and their getters/setters have been added for assesment 2:
    private float attackDmg;
    private int maxAmmo;
    private int maxHealth;
    //----------------------------------------------------

    /**
     * The enemy that is being targeted by the AI.
     */
    private final QueueFIFO<Ship> targets;


    public Pirate() {
        super();
        targets = new QueueFIFO<>();
        type = ComponentType.Pirate;
        plunder = GameManager.getSettings().get("starting").getInt("plunder");
        points = GameManager.getSettings().get("starting").getInt("points");
        factionId = 1;
        isAlive = true;
        JsonValue starting = GameManager.getSettings().get("starting");
        health = starting.getInt("health");
        attackDmg = starting.getInt("damage");
        ammo = starting.getInt("ammo");
        maxAmmo = ammo;
        maxHealth = health;
        currentPlunder = plunder;

    }

    public void addTarget(Ship target) {
        targets.add(target);
    }

    public int getPlunder() {
        return plunder;
    }

    public void addPlunder(int money) {
        plunder += money;
        currentPlunder += money;
    }

    public void spendPlunder(int amount){
        currentPlunder -= amount;
    }
    public int getCurrentPlunder(){
        return currentPlunder;
    }


    public Faction getFaction() {
        return GameManager.getFaction(factionId);
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public void setHealth(int health){this.health = health;}

    public void takeDamage(float dmg) {
        health -= dmg;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }


    /**
     * Will shoot a cannonball assigning this.parent as the cannonball's parent
     *
     * @param dir the direction to shoot in
     */
    public void shoot(Vector2 dir) {
        if (ammo == 0) {
            return;
        }
        if (parent instanceof Player){
            ammo--; // npc's get infinite ammo
        }
        GameManager.shoot(parent, dir);
    }

    /**
     * Adds ammo
     *
     * @param ammo amount to add
     */
    public void reload(int ammo) {
        this.ammo += ammo;
    }

    public int getHealth() {
        return health;
    }

    /**
     * if dst to target is less than attack range
     * target will be null if not in agro range
     */
    public boolean canAttack() {
        if (targets.peek() != null) {
            final Ship p = (Ship) parent;
            final Vector2 pos = p.getPosition();
            final float dst = pos.dst(targets.peek().getPosition());
            // withing attack range
            return dst <= Ship.getAttackRange();
        }
        return false;
    }

    /**
     * if dst to target is >= attack range
     * target will be null if not in agro range
     */
    public boolean isAgro() {
        if (targets.peek() != null) {
            final Ship p = (Ship) parent;
            final Vector2 pos = p.getPosition();
            final float dst = pos.dst(targets.peek().getPosition());
            // out of attack range but in agro range
            return dst >= Ship.getAttackRange();
        }
        return false;
    }

    public Ship getTarget() {
        return targets.peek();
    }

    public void removeTarget() {
        if (!targets.isEmpty()) {
            targets.pop();
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Marks component (and parent entity) as killed.
     */
    public void kill() {
        health = 0;
        isAlive = false;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public int getAmmo() {
        return ammo;
    }


    public QueueFIFO<Ship> getTargets() {
        return targets;
    }

    public int getMaxAmmo(){return maxAmmo;}
    public void setMaxAmmo(int newAmmo){maxAmmo = newAmmo;}

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    public float getDmg(){
        return attackDmg;
    }
    public void setAttackDmg(float dmg){
        attackDmg = dmg;
    }
    public void setPlunder(int plunder) {
        this.plunder = plunder;
    }

    public void setAlive(boolean b) {
        if (!b) {
            kill();
        }
    }
}
