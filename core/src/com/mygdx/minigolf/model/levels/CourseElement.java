package com.mygdx.minigolf.model.levels;

import java.util.stream.Stream;

public class CourseElement {
    public final int x, y, width, height, rotation;
    public final Course.CourseElementFunction function;
    public final CourseElementShape shape;

    public CourseElement(int x, int y, int width, int height, int rotation, CourseElementShape shape, Course.CourseElementFunction function) {
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
}
