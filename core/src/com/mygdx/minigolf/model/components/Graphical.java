package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.minigolf.controller.EntityFactory;

public class Graphical implements Component {

    private Sprite texture;
    private int layer;

    public Graphical(Sprite sprite) {
        this(sprite, 1);
    }

    public Graphical(Sprite sprite, int layer) {
        this.texture = sprite;
        this.layer = layer;
    }

    public Graphical(EntityFactory.Sprite sprite, int layer) {
        this(sprite.sprite, layer);
    }

    public Sprite getTexture() {
        return texture;
    }

    public void setTexture(Sprite texture) {
        this.texture = texture;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}