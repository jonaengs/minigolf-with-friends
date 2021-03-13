package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Course element is what draw.io calls a "shape".
 * When adding a shape to the diagram, the following xml is added as a child of the <root>-element:
 * <mxCell value="[...]", style="...", ...>
 * <mxGeometry [x="...",] [y="...",] width="...", height="..." ... />
 * </mxCell>
 * - value: Defines the function of the shape (if any). Ex: SPAWN, HOLE, COURSE
 * - The cell with value="COURSE..." defines the course shape [and name]
 * - Defining a course's name: value="COURSE Level-name"
 * - style: Defines the look of the shape. Attributes are separated by semicolons
 * - style[0] defines the shape type if the shape is not a rectangle
 * - If the shape has been rotated, this is noted in the "rotation=..." attribute
 */
public class Course {
    public final static List<String> requiredFunctions = Arrays.asList(
            "SPAWN", "HOLE"
    );

    public final String name;
    public final int width, height;
    private final ArrayList<CourseElement> elements = new ArrayList<>();

    public void validate() throws IllegalArgumentException {
        List<String> elementFunctions = elements.stream().map(e -> e.function).collect(Collectors.toList());
        if (!elementFunctions.containsAll(requiredFunctions))
            throw new IllegalArgumentException("Course required functions not satisfied");
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Illegal course dimensions");
        if (elements.stream().anyMatch(ce -> ce.x + ce.width > width || ce.y + ce.height > height))
            throw new IllegalArgumentException("Course element outside course bounds");
        elements.forEach(CourseElement::validate);
    }

    public Course(String filename) {
        this(Gdx.files.internal(CourseLoader.LEVELS_DIR + filename).read(), filename);
    }

    public Course(InputStream data, String filename) {
        // tmps for name, width and height as they are final and cannot be assigned in the loop.
        String[] split = null;
        int t_width = 0, t_height = 0;

        Element root = new XmlReader().parse(data).getChildByNameRecursive("root");  // closes stream
        for (int i = 0; i < root.getChildCount(); i++) {
            Element cell = (Element) root.getChild(i);
            if (cell.getChildCount() > 0) {
                Element geometry = (Element) cell.getChild(0);
                if (cell.get("value").startsWith("COURSE")) {
                    if (split != null)
                        throw new IllegalArgumentException("Found multiple COURSE shapes");
                    t_width = Integer.parseInt(geometry.getAttribute("width"));
                    t_height = Integer.parseInt(geometry.getAttribute("height"));
                    split = cell.get("value").split(" ", 2);
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
                            CourseElementShape.validStrings.contains(styles.get(0).toUpperCase()) ?
                                    CourseElementShape.valueOf(styles.get(0))
                                    : CourseElementShape.RECTANGLE,
                            cell.getAttribute("value")
                    );
                    elements.add(elem);
                }
            }
        }
        name = split != null && split.length == 2 ? split[1] : filename.split("\\.")[0];
        width = t_width;
        height = t_height;
    }

    public List<CourseElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public String toString() {
        return "Course " + name + " {" +
                "width=" + width +
                ", height=" + height +
                // String.join requires API level 26...
                ", elements=" + elements.stream()
                .map(e -> "\n\t" + e)
                .collect(Collectors.joining()) + "\n" +
                '}';
    }
}
