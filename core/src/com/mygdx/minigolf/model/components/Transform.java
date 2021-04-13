package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class Transform implements Component {
    private final Vector2 position = new Vector2();
    private final Vector2 scale = new Vector2(1f, 1f);
    private float rotation = 0.0f;
    private boolean visible = true;

    public Transform(float x, float y) {
        position.set(x, y);
    }

    public Transform(float x, float y, float rotation) {
        this(x, y);
        this.rotation = rotation;
    }

    public Transform(float x, float y, boolean visible) {
        this(x, y);
        this.visible = visible;
    }

    public Transform(float x, float y, float rotation, boolean visible) {
        this(x, y, rotation);
        this.visible = visible;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public Vector2 getScale() {
        return scale;
    }

    public void setScale(Vector2 scale) {
        this.scale.set(scale);
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
