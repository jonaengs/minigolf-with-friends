package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.levels.CourseElement.Function;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mygdx.minigolf.model.levels.CourseElement.Function.COURSE;
import static com.mygdx.minigolf.model.levels.CourseElement.Function.HOLE;
import static com.mygdx.minigolf.model.levels.CourseElement.Function.SPAWN;

/**
 * A Course element is what draw.io calls a "shape".
 * When adding a shape to the diagram, the following xml is added as a child of the <root>-element:
 * <mxCell value="[...]", style="...", ...>
 * <mxGeometry [x="...",] [y="...",] width="...", height="..." ... />
 * </mxCell>
 * - value: Defines the function of the shape (if any). Ex: SPAWN, HOLE, COURSE
 * - - The cell with value="COURSE..." defines the course shape [and name]
 * - - Defining a course's name: value="COURSE Level-name"
 * - - If no course name is defined, the filename (excluding ".xml") will be the course name
 * - style: Defines the look of the shape. Attributes are separated by semicolons
 * - - style[0] defines the shape type if the shape is not a rectangle
 * - - If the shape has been rotated, this is noted in the "rotation=..." attribute
 */
public class Course {
    private static final List<Function> requiredFunctions = Arrays.asList(
            SPAWN, HOLE, COURSE
    );
    private final ArrayList<CourseElement> elements = new ArrayList<>();

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    protected Course(String filename) {
        this(Gdx.files.internal(CourseLoader.LEVELS_DIR + filename).read(), filename);
    }

    protected Course(InputStream data, String filename) {
        Element root = new XmlReader().parse(data).getChildByNameRecursive("root");  // closes InputStream
        for (int i = 0; i < root.getChildCount(); i++) {
            Element node = root.getChild(i);
            if (node.getChildCount() > 0 && !node.getAttribute("value").contentEquals("BACKGROUND")) {
                Element geometry = node.getChild(0);
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
    }

    private static String getRotation(List<String> styles) {
        return styles.stream()
                .filter(s -> s.contains("rotation"))
                .findFirst()
                .map(s -> s.split("=")[1])
                .orElse("0");
    }

    public void validate() throws IllegalArgumentException {
        float scaledWidth = WIDTH / GraphicsSystem.PPM, scaledHeight = HEIGHT / GraphicsSystem.PPM;
        List<CourseElement.Function> elementFunctions = elements.stream().map(e -> e.function).collect(Collectors.toList());
        if (!elementFunctions.containsAll(requiredFunctions))
            throw new IllegalArgumentException("Course required functions not satisfied");
        if (elements.stream().peek(System.out::println).anyMatch(ce -> ce.x + ce.width > scaledWidth || ce.y + ce.height > scaledHeight))
            throw new IllegalArgumentException("Course element outside course bounds");
        elements.forEach(CourseElement::validate);
    }

    public List<CourseElement> getElements() {
        return Collections.unmodifiableList(elements);
    }
}
