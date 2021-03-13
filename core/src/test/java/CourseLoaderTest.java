import com.mygdx.minigolf.model.levels.CourseLoader;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CourseLoaderTest extends TestFileLoader {
    CourseLoaderTest() {
        this.dir = "levels/copies/";
    }

    @Test
    public void allCoursesLoad() throws IOException {
        String content = getFileContents(CourseLoader.LEVELS_FILE);
        CourseLoader.getCourses(
                Arrays.stream(content.split("\n"))
                .collect(Collectors.toMap(
                        file -> file, file -> {
                            try {
                                return new FileInputStream(getPath(file));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ))
        );
    }
}
