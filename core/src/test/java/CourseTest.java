import com.mygdx.minigolf.model.levels.Course;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CourseTest {

    InputStream getFileStream(String filename) throws IOException {
        String fp = this.getClass().getClassLoader().getResource("levels/" + filename).getPath().substring(1);
        return new FileInputStream(fp);
    }

    @Test
    void bareMinimumCourseIsValid() throws IOException {
        String file = "bare_minimum.xml";
        Course course = new Course(getFileStream(file), file);
        course.validate();
    }

    @Test
    void missingRequiredFunctionsFailsValidate() throws IOException {
        String file = "missing_spawn.xml";
        Course course = new Course(getFileStream(file), file);
        assertThrows(IllegalArgumentException.class, course::validate);

        file = "missing_hole.xml";
        course = new Course(getFileStream(file), file);
        assertThrows(IllegalArgumentException.class, course::validate);
    }

    @Test
    void missingCourseElementFailsValidate() throws IOException {
        String file = "missing_course.xml";
        Course course = new Course(getFileStream(file), file);
        assertThrows(IllegalArgumentException.class, course::validate);
    }

    @Test
    void negativeCourseDimensionsFailValidate() throws IOException {
        String file = "invalid_course_dims.xml";
        Course course = new Course(getFileStream(file), file);
        Exception e = assertThrows(IllegalArgumentException.class, course::validate);
        assertEquals(e.getMessage(), "Illegal course dimensions");
    }

    @Test
    void outOfBoundsCourseElementsFailValidate() throws IOException {
        String file = "out_of_bounds_elem.xml";
        Course course = new Course(getFileStream(file), file);
        Exception e = assertThrows(IllegalArgumentException.class, course::validate);
        assertEquals("Course element outside course bounds", e.getMessage());
    }

    @Test
    void invalidCoursElementsFailValidate() throws IOException {
        String file = "illegal_element_position.xml";
        Course course = new Course(getFileStream(file), file);
        Exception e = assertThrows(IllegalArgumentException.class, course::validate);
        assertEquals("Negative size or position", e.getMessage());
    }
}
