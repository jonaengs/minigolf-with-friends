package com.mygdx.minigolf.model.levels;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.components.Graphical;

import java.util.List;
import java.util.stream.Collectors;


public class LevelLoader {
    EntityFactory factory;

    public LevelLoader(EntityFactory factory) {
        this.factory = factory;
    }

    public List<Entity> loadLevel(String fileName) {
        return loadLevel(CourseLoader.getCourse(fileName));
    }

    public List<Entity> loadLevel(Course course) {
        return course.getElements().stream().map(this::createEntity).collect(Collectors.toList());
    }

    static private Shape getShape(CourseElement elem) {
        switch (elem.shape) {
            case ELLIPSE:
                return new CircleShape();
            case TRIANGLE:
                PolygonShape shape = new PolygonShape();
                shape.set(new float[]{
                        0, 0,
                        elem.width, 0,
                        elem.width / 2f, elem.height
                });
                return shape;
            case RECTANGLE:
                shape = new PolygonShape();
                shape.setAsBox(elem.width, elem.height);
                return shape;
            default:
                throw new IllegalArgumentException("Illegal course element shape");
        }
    }

    private Entity createEntity(CourseElement elem) {
        switch (elem.function) {
            case HOLE:
                return factory.createHole(elem.x, elem.y);
            case SPAWN:
                return factory.createSpawn(elem.x, elem.y);
            case OBSTACLE:
                return factory.createObstacle(elem.x, elem.y, getShape(elem));
            case POWERUP:
                return factory.createPowerup(elem.x, elem.y);
            case COURSE:
                return factory.createCourse(elem.x, elem.y);
            default:
                throw new IllegalArgumentException("Illegal course element function");
        }
    }

}
