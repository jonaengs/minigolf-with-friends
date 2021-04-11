package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.controller.ComponentMappers.GraphicalMapper;

import java.util.Comparator;


public class LayerComparator implements Comparator<Entity> {

    public LayerComparator() {
    }

    @Override
    public int compare(Entity entityA, Entity entityB) {
        return Integer.compare(GraphicalMapper.get(entityA).getLayer(), GraphicalMapper.get(entityB).getLayer());
    }
}
