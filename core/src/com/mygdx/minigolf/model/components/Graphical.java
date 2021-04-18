package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.minigolf.controller.EntityFactory;

public class Graphical implements Component {

    public static boolean enabled = true; // Allows headless games to run. TODO: Find better solution

    public float[] triangles;
    public Color color;
    private int layer;

    public Graphical(Color color, int layer) {
        if (enabled) {
            this.color = color;
            this.layer = layer;
        }
    }

    public Graphical(EntityFactory.Sprite sprite, int layer) {
        this(sprite.color, layer);
    }

    public Graphical(Color color, int layer, float[] triangles) {
        this(color, layer);
        if (enabled) {
            this.triangles = triangles;
        }

    }

    public float[] getTriangles() {
        return triangles;
    }

    public void setTriangles(float[] triangles) {
        this.triangles = triangles;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}