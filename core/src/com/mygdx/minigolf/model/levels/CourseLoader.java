package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourseLoader {
    public final static String LEVELS_DIR = "levels/";
    public final static String LEVELS_FILE = "levels_list.txt";


    public static List<Course> getCourses() {
        FileHandle dirHandle = Gdx.files.internal(LEVELS_DIR).child(LEVELS_FILE);
        return Arrays.stream(dirHandle.readString().split("\n"))
                .peek(System.out::println)
                .map(Course::new)
                .peek(Course::validate)
                //@TODO: .map(Level::new)
                .collect(Collectors.toList());
    }
}
