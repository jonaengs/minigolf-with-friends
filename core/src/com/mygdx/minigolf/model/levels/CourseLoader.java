package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mygdx.minigolf.model.levels.CourseElement.Function.COURSE;
import static com.mygdx.minigolf.model.levels.CourseElement.Function.HOLE;
import static com.mygdx.minigolf.model.levels.CourseElement.Function.SPAWN;
import static com.mygdx.minigolf.util.Constants.SCREEN_HEIGHT;
import static com.mygdx.minigolf.util.Constants.SCREEN_WIDTH;


/**
 * A Course element is what draw.io calls a "shape".
 * When adding a shape to the diagram, the following xml is added as a child of the <root>-element:
 * <mxCell value="[...]", style="...", ...>
 * <mxGeometry [x="...",] [y="...",] width="...", height="..." ... />
 * </mxCell>
 * - value: Defines the function of the shape (if any). Ex: SPAWN, HOLE, COURSE
 * - - The cell with value="BACKGROUND" is ignored. It is simply used to visualize the screen.
 * - style: Defines the look of the shape. Attributes are separated by semicolons
 * - - style[0] defines the shape type if the shape is not a rectangle
 * - - If the shape has been rotated, this is noted in the "rotation=..." attribute
 */
public class CourseLoader {
    public final static String LEVELS_DIR = "levels/";
    public final static String LEVELS_FILE = "levels_list.txt";

    private static final List<CourseElement.Function> requiredFunctions = Arrays.asList(
            SPAWN, HOLE, COURSE
    );

    static public List<CourseElement> load(String filename) {
        return load(Gdx.files.internal(LEVELS_DIR + filename).read());
    }

    static public List<CourseElement> load(InputStream data) {
        ArrayList<CourseElement> elements = new ArrayList<>();
        XmlReader.Element root = new XmlReader().parse(data).getChildByNameRecursive("root");  // closes InputStream
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element node = root.getChild(i);
            if (node.getChildCount() > 0 && !node.getAttribute("value").contentEquals("BACKGROUND")) {
                XmlReader.Element geometry = node.getChild(0);
                List<String> styles = Arrays.asList(node.getAttribute("style").split(";"));
                elements.add(new CourseElement(
                        Float.parseFloat(geometry.get("x", "0")),
                        Float.parseFloat(geometry.get("y", "0")),
                        Float.parseFloat(geometry.get("width")),
                        Float.parseFloat(geometry.get("height")),
                        Float.parseFloat(getRotation(styles)),
                        // Assumes that shape type is always the first element of the style attribute.
                        CourseElement.Shape.strValueOf(styles.get(0)),
                        CourseElement.Function.strValueOf(node.getAttribute("value"))
                ));
            }
        }
        return Collections.unmodifiableList(elements);
    }

    private static String getRotation(List<String> styles) {
        return styles.stream()
                .filter(s -> s.contains("rotation"))
                .findFirst()
                .map(s -> s.split("=")[1])
                .orElse("0");
    }

    static public void validate(List<CourseElement> elements) throws IllegalArgumentException {
        float scaledWidth = SCREEN_WIDTH / GraphicsSystem.PPM, scaledHeight = SCREEN_HEIGHT / GraphicsSystem.PPM;
        List<CourseElement.Function> elementFunctions = elements.stream().map(e -> e.function).collect(Collectors.toList());
        if (!elementFunctions.containsAll(requiredFunctions))
            throw new IllegalArgumentException("Course required functions not satisfied");
        if (elements.stream().peek(System.out::println).anyMatch(ce -> ce.x + ce.width > scaledWidth || ce.y + ce.height > scaledHeight))
            throw new IllegalArgumentException("Course element outside screen bounds");
        elements.forEach(CourseElement::validate);
    }

    public static List<List<CourseElement>> getCourses() {
        String[] filenames = getFileNames();
        return getCourses(Arrays.stream(filenames).collect(
                Collectors.toMap(
                        fn -> fn, fn -> Gdx.files.internal(LEVELS_DIR + fn).read()
                )));
    }

    public static List<List<CourseElement>> getCourses(Map<String, InputStream> inputs) {
        return inputs.values().stream()
                .map(CourseLoader::load)
                .collect(Collectors.toList());
    }

    public static String[] getFileNames() {
        FileHandle dirHandle = Gdx.files.internal(LEVELS_DIR).child(LEVELS_FILE);
        return dirHandle.readString().split("\n");  // readString closes file
    }
}
