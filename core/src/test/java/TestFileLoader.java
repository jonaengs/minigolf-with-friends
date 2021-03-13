import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

abstract class TestFileLoader {
    String dir = "";

    String getPath(String filename) {
        String fp = this.getClass().getClassLoader().getResource(dir + filename).getPath();
        if (System.getProperty("os.name").startsWith("Windows")) {
            fp = fp.substring(1);
        }
        return fp;
    }

    InputStream getFileStream(String filename) throws FileNotFoundException {
        return new FileInputStream(getPath(filename));
    }

    String getFileContents(String filename) throws IOException {
        String path = getPath(filename);
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
