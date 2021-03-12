import com.mygdx.minigolf.model.levels.CourseLoader;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CourseLoaderTest {

    String getPath(String filename) throws IOException {
        return this.getClass().getClassLoader().getResource("levels/copies/" + filename).getPath().substring(1);
    }

    String readFile(String filename) throws IOException {
        String path = getPath(filename);
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void allCoursesLoad() throws IOException {
        String content = readFile(CourseLoader.LEVELS_FILE);
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
