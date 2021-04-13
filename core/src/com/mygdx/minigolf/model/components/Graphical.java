package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.mygdx.minigolf.controller.EntityFactory;

public class Graphical implements Component {

    public static boolean enabled = true; // Allows headless games to run. TODO: Find better solution

    private PolygonRegion polygonRegion;
    private Color color;
    private int layer;

    public Graphical(Color color, int layer) {
        if (enabled) {
            this.color = color;
            this.layer = layer;
        }
    }

    public Graphical(Color color, int layer, float[] vertices) {
        this(color, layer);
        if (enabled) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
            pixmap.setColor(color);
            pixmap.fill();
            polygonRegion = new PolygonRegion(
                    new TextureRegion(new Texture(pixmap)),
                    vertices,
                    new EarClippingTriangulator().computeTriangles(vertices).toArray()
            );
        }

    }

    public Graphical(EntityFactory.Sprite sprite, int layer) {
        this(sprite.color, layer);
    }

    public PolygonRegion getPolygonRegion() {
        return polygonRegion;
    }

    public void setPolygonRegion(PolygonRegion polygonRegion) {
        this.polygonRegion = polygonRegion;
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