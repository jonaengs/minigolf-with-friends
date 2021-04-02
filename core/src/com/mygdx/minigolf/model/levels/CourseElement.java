package com.mygdx.minigolf.model.levels;

import com.mygdx.minigolf.controller.systems.GraphicsSystem;

import java.util.Arrays;
import java.util.stream.Stream;

public class CourseElement {
    public final float x, y, width, height, rotation;
    public final Function function;
    public final Shape shape;

    // Using GraphicsSystem values here is not exactly good decoupling. Consider moving to somewhere else like LevelLoader
    protected CourseElement(float x, float y, float width, float height, float rotation, Shape shape, Function function) {
        this.x = x / GraphicsSystem.PPM;
        this.y = y / GraphicsSystem.PPM;
        this.width = width / GraphicsSystem.PPM;
        this.height = height / GraphicsSystem.PPM;
        this.rotation = rotation;
        this.shape = shape;
        this.function = function;
    }

    public void validate() {
        if (Stream.of(x, y, width, height).anyMatch(i -> i < 0))
            throw new IllegalArgumentException("Negative size or position");
    }

    public String toString() {
        return "CourseElement{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                ", shape='" + shape + '\'' +
                ", function='" + function + '\'' +
                '}';
    }

    public enum Function {
        SPAWN, HOLE, COURSE, POWERUP, OBSTACLE;

        static Function strValueOf(String s) throws IllegalArgumentException {
            return Arrays.stream(Function.values())
                    .filter(func -> func.name().contentEquals(s.toUpperCase()))
                    .findFirst()
                    .orElse(OBSTACLE);
        }
    }

    public enum Shape {
        // Use str in case internal representation (enum) diverges from external representation (string in xml)
        RECTANGLE("rectangle"), ELLIPSE("ellipse"), TRIANGLE("triangle");

        private final String str;

        Shape(String str) {
            this.str = str;
        }

        static Shape strValueOf(String s) throws IllegalArgumentException {
            return Arrays.stream(Shape.values())
                    .filter(shape -> shape.str.contentEquals(s.toLowerCase()))
                    .findFirst()
                    .orElse(RECTANGLE);
        }
    }
}
