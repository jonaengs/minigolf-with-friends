package com.mygdx.minigolf.model.levels;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mygdx.minigolf.controller.EntityFactory;

import java.util.List;
import java.util.stream.Collectors;


// TODO: Create level class with methods to easily find spawn, hole, etc., delete entities and load new entities
public class LevelLoader {
    EntityFactory factory;

    public LevelLoader(EntityFactory factory) {
        this.factory = factory;
    }

    public List<Entity> loadLevel(String fileName) {
        return loadLevel(CourseLoader.load(fileName));
    }

    public List<Entity> loadLevel(List<CourseElement> course) {
        return course.stream().map(this::createEntity).collect(Collectors.toList());
    }

    static private Shape getShape(CourseElement elem) {
        Vector2 middle = new Vector2(elem.width/2, elem.height/2);
        switch (elem.shape) {
            case ELLIPSE:
                float radius = elem.width / 2;
                CircleShape circle = new CircleShape();
                circle.setRadius(radius);
                return circle;
            case TRIANGLE:
                PolygonShape triangle = new PolygonShape();
                triangle.set(new Vector2[]{
                        new Vector2(0, 0).rotateAroundDeg(middle, elem.rotation),
                        new Vector2(elem.width, elem.height / 2).rotateAroundDeg(middle, elem.rotation),
                        new Vector2(0, elem.height).rotateAroundDeg(middle, elem.rotation)
                });
                return triangle;
            case RECTANGLE:
                PolygonShape rectangle = new PolygonShape();
                rectangle.set(new Vector2[]{
                        new Vector2(0, 0).rotateAroundDeg(middle, elem.rotation),
                        new Vector2(elem.width, 0).rotateAroundDeg(middle, elem.rotation),
                        new Vector2(elem.width, elem.height).rotateAroundDeg(middle, elem.rotation),
                        new Vector2(0, elem.height).rotateAroundDeg(middle, elem.rotation),
                });
                return rectangle;
            default:
                throw new IllegalArgumentException("Illegal course element shape");
        }
    }

    private Entity createEntity(CourseElement elem) {
        switch (elem.function) {
            case HOLE:
                return factory.createHole(elem.x, elem.y, (CircleShape) getShape(elem));
            case SPAWN:
                return factory.createSpawn(elem.x, elem.y);
            case OBSTACLE:
                return factory.createObstacle(elem.x, elem.y, (PolygonShape) getShape(elem));
            case POWERUP:
                return factory.createPowerup(elem.x, elem.y, (CircleShape) getShape(elem));
            case COURSE:
                return factory.createCourse(elem.x, elem.y, (PolygonShape) getShape(elem));
            case WALL:
                return factory.createWall(elem.x, elem.y, (PolygonShape) getShape(elem));
            default:
                throw new IllegalArgumentException("Illegal course element function");
        }
    }

}
