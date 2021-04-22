package com.mygdx.minigolf.util;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

public class ComponentMappers {
    public static class PhysicalMapper {
        public static final ComponentMapper<Physical> mapper = ComponentMapper.getFor(Physical.class);
        public static Physical get(Entity entity) {
            return mapper.get(entity);
        }
    }
    public static class GraphicalMapper {
        public static final ComponentMapper<Graphical> mapper = ComponentMapper.getFor(Graphical.class);
        public static Graphical get(Entity entity) {
            return mapper.get(entity);
        }
    }
    public static class ObjectiveMapper {
        public static final ComponentMapper<Objective> mapper = ComponentMapper.getFor(Objective.class);
        public static Objective get(Entity entity) {
            return mapper.get(entity);
        }
    }
    public static class PlayerMapper {
        public static final ComponentMapper<Player> mapper = ComponentMapper.getFor(Player.class);
        public static Player get(Entity entity) {
            return mapper.get(entity);
        }
    }
}
