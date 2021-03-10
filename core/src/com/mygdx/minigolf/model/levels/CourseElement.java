package com.mygdx.minigolf.model.levels;

public class CourseElement {
    private final int x, y, width, height, rotation;
    private final String shape, function;

    public CourseElement(int x, int y, int width, int height, int rotation, String shape, String function) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.shape = shape;
        this.function = function;
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
