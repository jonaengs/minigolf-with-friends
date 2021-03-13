package com.mygdx.minigolf.model.levels;

import com.badlogic.ashley.utils.ImmutableArray;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum CourseElementShape {
    RECTANGLE("rectangle"), ELLIPSE("ellipse"), TRIANGLE("triangle");

    public final String str;
    public final static List<String> validStrings = Collections.unmodifiableList(
            Arrays.stream(CourseElementShape.values())
                    .map(ces -> ces.str)
                    .collect(Collectors.toList())
    );

    CourseElementShape(String str) {
        this.str = str;
    }

}
