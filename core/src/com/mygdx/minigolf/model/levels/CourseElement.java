package com.mygdx.minigolf.model.levels;

import java.util.Arrays;
import java.util.stream.Stream;

public class CourseElement {
    public final int x, y, width, height, rotation;
    public final String shape, function;

    public CourseElement(int x, int y, int width, int height, int rotation, String shape, String function) {
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
        if (!Course.validShapes.contains(shape))
            throw new IllegalArgumentException("Invalid shape");
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
}
