package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourseLoader {
    public final static String LEVELS_DIR = "levels";

    public static List<Course> getCourses() {
        System.out.println(Gdx.files.internal("levels").read().toString());
        FileHandle dirHandle = Gdx.files.internal(LEVELS_DIR);
        System.out.println(Arrays.toString(dirHandle.list()));
        System.out.println(dirHandle);
        return Arrays.stream(dirHandle.list())
                .map(fh -> new Course(fh.path()))
                .collect(Collectors.toList());
    }
}
