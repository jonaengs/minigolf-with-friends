package com.mygdx.minigolf.model.levels;

import java.util.Arrays;
import java.util.stream.Stream;

public class CourseElement {
    public final int x, y, width, height, rotation;
    public final Function function;
    public final Shape shape;

    protected CourseElement(int x, int y, int width, int height, int rotation, Shape shape, Function function) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
