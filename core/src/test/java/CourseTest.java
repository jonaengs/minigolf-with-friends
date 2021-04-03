import com.mygdx.minigolf.model.levels.CourseElement;
import com.mygdx.minigolf.model.levels.CourseLoader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CourseTest extends TestFileLoader {
    CourseTest() {
        this.dir = "levels/";
    }

    @Test
    void bareMinimumCourseIsValid() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("bare_minimum.xml"));
        CourseLoader.validate(course);
    }

    @Test
    void missingRequiredFunctionsFailsValidate() throws IOException {
        final List<CourseElement> course1 = CourseLoader.load(getFileStream("missing_spawn.xml"));
        assertThrows(IllegalArgumentException.class, () -> CourseLoader.validate(course1));

        final List<CourseElement> course2 = CourseLoader.load(getFileStream("missing_hole.xml"));
        assertThrows(IllegalArgumentException.class, () -> CourseLoader.validate(course2));
    }

    @Test
    void missingCourseElementFailsValidate() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("missing_course.xml"));
        assertThrows(IllegalArgumentException.class, () -> CourseLoader.validate(course));
    }

    @Test
    void outOfBoundsCourseElementsFailValidate() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("out_of_bounds_elem.xml"));
        Exception e = assertThrows(IllegalArgumentException.class, () -> CourseLoader.validate(course));
        assertEquals("Course element outside screen bounds", e.getMessage());
    }

    @Test
    void invalidCoursElementsFailValidate() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("illegal_element_position.xml"));
        Exception e = assertThrows(IllegalArgumentException.class, () -> CourseLoader.validate(course));
        assertEquals("Element has negative size or position", e.getMessage());
    }

    @Test
    void shapesAreParsedCorrectly() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("all_shapes.xml"));
        course.stream()
                .map(ce -> ce.shape)
                .collect(Collectors.toList())
                .containsAll(
                        Arrays.asList(CourseElement.Shape.values())
                );
    }

    @Test
    void rotationIsParsedCorrectly() throws IOException {
        List<CourseElement> course = CourseLoader.load(getFileStream("all_shapes.xml"));
        CourseElement triangle = course.stream()
                .filter(ce -> ce.shape == CourseElement.Shape.TRIANGLE)
                .findFirst().get();
        assertEquals(-219, triangle.rotation);

        CourseElement notTriangle = course.stream()
                .filter(ce -> ce.shape != CourseElement.Shape.TRIANGLE)
                .findFirst().get();
        assertEquals(0, notTriangle.rotation);
    }
}
