import com.mygdx.minigolf.model.levels.Course;
import com.mygdx.minigolf.model.levels.CourseElement;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CourseTest extends TestFileLoader {
    CourseTest() {
        this.dir = "levels/";
    }

    @Test
    void bareMinimumCourseIsValid() throws IOException {
        Course course = getCourse("bare_minimum.xml");
        course.validate();
    }

    @Test
    void missingRequiredFunctionsFailsValidate() throws IOException {
        Course course = getCourse("missing_spawn.xml");
        assertThrows(IllegalArgumentException.class, course::validate);

        course = getCourse("missing_hole.xml");
        assertThrows(IllegalArgumentException.class, course::validate);
    }

    @Test
    void missingCourseElementFailsValidate() throws IOException {
        Course course = getCourse("missing_course.xml");
        assertThrows(IllegalArgumentException.class, course::validate);
    }

    @Test
    void outOfBoundsCourseElementsFailValidate() throws IOException {
        Course course = getCourse("out_of_bounds_elem.xml");
        Exception e = assertThrows(IllegalArgumentException.class, course::validate);
        assertEquals("Course element outside course bounds", e.getMessage());
    }

    @Test
    void invalidCoursElementsFailValidate() throws IOException {
        Course course = getCourse("illegal_element_position.xml");
        Exception e = assertThrows(IllegalArgumentException.class, course::validate);
        assertEquals("Element has negative size or position", e.getMessage());
    }

    @Test
    void shapesAreParsedCorrectly() throws IOException {
        Course course = getCourse("all_shapes.xml");
        course.getElements().stream()
                .map(ce -> ce.shape)
                .collect(Collectors.toList())
                .containsAll(
                        Arrays.asList(CourseElement.Shape.values())
                );
    }

    @Test
    void rotationIsParsedCorrectly() throws IOException {
        Course course = getCourse("all_shapes.xml");
        CourseElement triangle = course.getElements().stream()
                .filter(ce -> ce.shape == CourseElement.Shape.TRIANGLE)
                .findFirst().get();
        assertEquals(-219, triangle.rotation);

        CourseElement notTriangle = course.getElements().stream()
                .filter(ce -> ce.shape != CourseElement.Shape.TRIANGLE)
                .findFirst().get();
        assertEquals(0, notTriangle.rotation);
    }
}
