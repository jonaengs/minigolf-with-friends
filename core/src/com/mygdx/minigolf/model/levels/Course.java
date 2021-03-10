package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Course {
    private int width, height;
    private final ArrayList<CourseElement> elements = new ArrayList<>();

    private final static String RECTANGLE = "rectangle", ELLIPSE = "ellipse", TRIANGLE = "triangle";
    private final static List<String> validShapes = Arrays.asList(RECTANGLE, ELLIPSE, TRIANGLE);

    public Course(String filename)  {
        FileHandle fh = Gdx.files.internal("levels/" + filename);
        XmlReader reader = new XmlReader();
        Element root = reader.parse(fh).getChildByNameRecursive("root");
        for (int i = 0; i < root.getChildCount(); i++) {
            Element cell = (Element) root.getChild(i);
            if (cell.getChildCount() > 0) {
                Element geometry = (Element) cell.getChild(0);
                if (cell.getAttribute("value").contentEquals("COURSE")) {
                    width = Integer.parseInt(geometry.getAttribute("width"));
                    height = Integer.parseInt(geometry.getAttribute("height"));
                } else {
                    List<String> styles = Arrays.asList(cell.getAttribute("style").split(";"));
                    CourseElement elem = new CourseElement(
                        Integer.parseInt(geometry.get("x", "0")),
                        Integer.parseInt(geometry.get("y", "0")),
                        Integer.parseInt(geometry.get("width")),
                        Integer.parseInt(geometry.get("height")),
                        Integer.parseInt(
                                styles.stream()
                                        .filter(s -> s.contains("rotation"))
                                        .findFirst()
                                        .map(s -> s.split("=")[1])
                                        .orElse("0")
                        ),
                        // Assumes that shape type is always the first element of the style attribute.
                        validShapes.contains(styles.get(0)) ? styles.get(0) : RECTANGLE,
                        cell.getAttribute("value")
                    );
                    elements.add(elem);
                }
            }
        }
    }

    public String toString() {
        return "Course{" +
                "width=" + width +
                ", height=" + height +
                // String.join requires API level 26...
                ", elements=" + elements.stream()
                    .map(e -> "\n\t" + e.toString())
                    .collect(Collectors.joining()) + "\n" +
                '}';
    }
}
