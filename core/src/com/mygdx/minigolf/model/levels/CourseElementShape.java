package com.mygdx.minigolf.model.levels;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum CourseElementShape {
    RECTANGLE("rectangle"), ELLIPSE("ellipse"), TRIANGLE("triangle");

    private final String str;
    public final static List<String> validStrings = Collections.unmodifiableList(
            Arrays.stream(CourseElementShape.values())
                    .map(ces -> ces.str)
                    .collect(Collectors.toList())
    );

    CourseElementShape(String str) {
        this.str = str;
    }

    static CourseElementShape strValueOf(String s) throws IllegalArgumentException {
        return Arrays.stream(CourseElementShape.values())
            .filter(cse -> cse.str.contentEquals(s))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("invalid string shape string value: " + s));
    }
}
