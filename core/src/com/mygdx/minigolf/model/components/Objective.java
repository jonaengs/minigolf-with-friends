package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class Objective implements Component {

    public Circle area;

    public Circle getArea() {
        return area;
    }

    public void setArea(Circle area) {
        this.area = area;
    }

    public boolean contains(Vector2 position){
        return area.contains(position);
    }
}
