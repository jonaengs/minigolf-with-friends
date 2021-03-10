package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

// TODO: Make types entities
public interface LevelAPI {
    List<Shape2D> getObstacles();
    Rectangle getSpawn();
    Ellipse getHole();
    Vector2 getSize();
}
