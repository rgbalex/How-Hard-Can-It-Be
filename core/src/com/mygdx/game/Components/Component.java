package com.mygdx.game.Components;

import com.mygdx.game.Entitys.Entity;
import com.mygdx.game.Managers.EntityManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Base class for the Components
 */
public abstract class Component {
    protected ComponentType type;
    protected Entity parent;
    protected ArrayList<ComponentType> requirements;
    protected boolean reqsMet;

    protected Component() {
        reqsMet = false;
        type = ComponentType.Unknown;
        parent = null;
        requirements = new ArrayList<>();
        EntityManager.addComponent(this);
    }

    public void setParent(Entity e) {
        parent = e;
    }

    public Entity getParent() {
        return parent;
    }

    /**
     * Sets the required components
     *
     * @param reqs take a guess
     */
    public final void setRequirements(ComponentType... reqs) {
        requirements.addAll(Arrays.asList(reqs));
    }

    /**
     * Checks if the passed requirements exist will crash if they aren't
     */
    private void checkRequirements() {
        if (reqsMet) {
            return;
        }
        for (ComponentType t : requirements) {
            Component c = parent.getComponent(t);
            if (c == null) {
                throw new RuntimeException("Component: " + t.name() + " Is not found for " + type.name());
            }
        }
        reqsMet = true;
    }

    public final ComponentType getType() {
        return type;
    }

    /**
     * Called once before start prior to the update loop.
     */
    public void awake() {
        checkRequirements();
    }

    /**
     * Called once after awake but prior to the update loop.
     */
    public void start() {
        checkRequirements();
    }

    /**
     * Called once after the update loop has finished.
     */
    public void cleanUp() {
        checkRequirements();
        
    }

    /**
     * Called once per frame
     */
    public void update() {
        checkRequirements();
    }

    /**
     * Called once per frame used exclusively for rendering
     */
    public void render() {
        checkRequirements();
    }
}
