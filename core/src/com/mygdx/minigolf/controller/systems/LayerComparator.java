package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.components.Graphical;

import java.util.Comparator;


public class LayerComparator implements Comparator<Entity> {
    private final ComponentMapper<Graphical> graphicalMapper = ComponentMapper.getFor(Graphical.class);

    public LayerComparator() {
    }

    @Override
    public int compare(Entity entityA, Entity entityB) {
        return Float.compare(graphicalMapper.get(entityA).getLayer(), graphicalMapper.get(entityB).getLayer());
    }
}
