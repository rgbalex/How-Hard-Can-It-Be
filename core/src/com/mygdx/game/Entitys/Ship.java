package com.mygdx.game.Entitys;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.Components.*;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.game.Physics.PhysicsBodyType;

import java.util.Objects;

public class Ship extends Entity {
    private static int shipCount = 0;
    private static ObjectMap<Vector2, String> shipDirections;

    public Ship() {
        super(5);
        setName("Ship (" + shipCount++ + ")");

        shipDirections = new ObjectMap<>();
        shipDirections.put(new Vector2(0, 1), "-up");
        shipDirections.put(new Vector2(0, -1), "-down");
        shipDirections.put(new Vector2(1, 0), "-right");
        shipDirections.put(new Vector2(-1, 0), "-left");

        Transform t = new Transform();
        Living l = new Living();
        Renderable r = new Renderable(5, "white-up", RenderLayer.Transparent);
        RigidBody rb = new RigidBody(PhysicsBodyType.Dynamic, r, t);
        Pirate p = new Pirate();

        addComponents(t, r, rb, l, p);
    }

    public void setFaction(int factionId) {
        getComponent(Pirate.class).setFactionId(factionId);
    }

    public static String getShipDirection(Vector2 dir) {
        if (shipDirections.containsKey(dir)){
            return shipDirections.get(dir);
        }
        return "";
    }

    private String getColour() {
        return getComponent(Pirate.class).getFaction().getColour();
    }

    public void setShipDirection(String direction) {
        if(Objects.equals(direction, "")) {
            return;
        }
        Renderable r = getComponent(Renderable.class);
        Sprite s = ResourceManager.getSprite(5, getColour() + direction);
        r.getSprite().setU(s.getU());
        r.getSprite().setV(s.getV());
        r.getSprite().setU2(s.getU2());
        r.getSprite().setV2(s.getV2());
    }
}