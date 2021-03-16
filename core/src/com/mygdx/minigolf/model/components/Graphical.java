package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Graphical implements Component {

    private Sprite texture;

    public Sprite getTexture() {
        return texture;
    }

    public void setTexture(Sprite texture) {
        this.texture = texture;
    }
}
