package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseLoader {
    public final static String LEVELS_DIR = "levels/";
    public final static String LEVELS_FILE = "levels_list.txt";

    public static List<Course> getCourses() {
        String[] filenames = getFileNames();
        return getCourses(Arrays.stream(filenames).collect(
                Collectors.toMap(
                        fn -> fn, fn -> Gdx.files.internal(LEVELS_DIR + fn).read()
                )));
    }

    public static List<Course> getCourses(Map<String, InputStream> inputs) {
        return inputs.entrySet().stream()
                .map(e -> new Course(e.getValue(), e.getKey()))
                .collect(Collectors.toList());
    }

    public static Course getCourse(String filename) {
        return new Course(filename);
    }

    public static String[] getFileNames() {
        FileHandle dirHandle = Gdx.files.internal(LEVELS_DIR).child(LEVELS_FILE);
        return dirHandle.readString().split("\n");
    }
}
