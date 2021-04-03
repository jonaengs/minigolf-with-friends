import com.mygdx.minigolf.model.levels.CourseElement;
import com.mygdx.minigolf.model.levels.CourseLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

abstract class TestFileLoader {
    String dir = "";

    String getPath(String filename) {
        String filePath = dir + filename;
        String absPath = Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(filePath)
        ).getPath();
        if (System.getProperty("os.name").startsWith("Windows")) {
            absPath = absPath.substring(1);
        }
        return absPath;
    }

    InputStream getFileStream(String filename) throws FileNotFoundException {
        return new FileInputStream(getPath(filename));
    }

    String getFileContents(String filename) throws IOException {
        String path = getPath(filename);
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    List<CourseElement> getCourse(String filename) throws IOException {
        return CourseLoader.load(getFileStream(filename));
    }
}
