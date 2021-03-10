package com.mygdx.minigolf.model.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Course {
    private int width, height;
    private final ArrayList<CourseElement> elements = new ArrayList<>();

    private final static String RECTANGLE = "rectangle", ELLIPSE = "ellipse", TRIANGLE = "triangle";
    private final static List<String> validShapes = Arrays.asList(RECTANGLE, ELLIPSE, TRIANGLE);

    private String getAttributeOrDefault(Element elem, String attr, String dfault) {
        String val = elem.getAttribute(attr);
        return val.length() > 0 ? val : dfault;
    }

    public Course(String filename) throws ParserConfigurationException, IOException, SAXException {
        File file = Gdx.files.internal("levels/" + filename).file().getAbsoluteFile();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        doc.getDocumentElement().normalize();
        NodeList cells = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < cells.getLength(); i++) {
            Element cell = (Element) cells.item(i);
            if (cell.hasChildNodes()) {
                Element geometry = (Element) cell.getFirstChild();
                if (cell.getAttribute("value").contentEquals("COURSE")) {
                    width = Integer.parseInt(geometry.getAttribute("width"));
                    height = Integer.parseInt(geometry.getAttribute("height"));
                } else {
                    List<String> styles = Arrays.asList(cell.getAttribute("style").split(";"));
                    CourseElement elem = new CourseElement(
                        Integer.parseInt(getAttributeOrDefault(geometry, "x", "0")),
                        Integer.parseInt(getAttributeOrDefault(geometry, "y", "0")),
                        Integer.parseInt(geometry.getAttribute("width")),
                        Integer.parseInt(geometry.getAttribute("height")),
                        Integer.parseInt(
                                styles.stream()
                                        .filter(s -> s.contains("rotation"))
                                        .findFirst()
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
                ", elements=" + elements +
                '}';
    }
}
