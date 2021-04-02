package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.minigolf.controller.EntityFactory;

public class Graphical implements Component {

    public final Color color;
    private int layer;

    public Graphical(Color color) {
        this(color, 1);
    }

    public Graphical(Color color, int layer) {
        this.color = color;
        this.layer = layer;
    }

    public Graphical(EntityFactory.Sprite sprite, int layer) {
        this(sprite.color, layer);
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}