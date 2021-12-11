package com.mygdx.game.Components;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Managers.RenderLayer;
import com.mygdx.game.Managers.RenderingManager;
import com.mygdx.utils.ResourceManager;

/**
 * Add the ability for the object to be shown and positioned
 */
public class Renderable extends Component {
    private Sprite sprite;
    public Renderable(){
        super();
        type = ComponentType.Renderable;
        sprite = new Sprite();
    }

    /**
     * Calls default constructor
     * @param texId the id of the texture the sprite will take on
     * @param layer the rendering layer
     */
    public Renderable(int texId, RenderLayer layer) {
        this();
        sprite = new Sprite(ResourceManager.getTexture(texId)); // TODO: don't call the constructor
        RenderingManager.addItem(this, layer);
    }

    @Override
    public void update() {
        super.update();
        Transform c = parent.getComponent(Transform.class);
        if(c == null){
            return;
        }
        Vector2 p = c.getPosition();
        Vector2 s = c.getScale();

        sprite.setPosition(p.x, p.y);
        sprite.setRotation(MathUtils.radiansToDegrees * c.getRotation());
        sprite.setScale(s.x, s.y);
    }

    @Override
    public void render() {
        super.render();
        sprite.draw(RenderingManager.getBatch());
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
    }
}
