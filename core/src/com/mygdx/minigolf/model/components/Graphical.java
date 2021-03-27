package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.minigolf.controller.EntityFactory;

public class Graphical implements Component {

    private Sprite texture;

    public Graphical(Sprite sprite) {
        this.texture = sprite;
    }

    public Graphical(EntityFactory.Sprite sprite) {
        this(sprite.sprite);
    }

    public Sprite getTexture() {
        return texture;
    }

    public void setTexture(Sprite texture) {
        this.texture = texture;
    }
}
