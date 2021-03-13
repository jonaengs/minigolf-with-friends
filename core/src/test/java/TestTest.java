import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.XmlReader;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTest {

    private String getPath(String filename) {
        String fp = this.getClass().getClassLoader().getResource(filename).getPath();
        if (System.getProperty("os.name").startsWith("Windows")) {
            fp = fp.substring(1);
        }
        return fp;
    }

    @Test
    public void ReadResourceTest() throws IOException {
        String fp = getPath("test.txt");
        // https://stackoverflow.com/questions/27886918/running-tests-in-libgdx-using-intellij
        byte[] content = Gdx.files == null ?
                Files.readAllBytes(Paths.get(fp))
                : Gdx.files.internal(fp).readBytes();
        assertTrue(new String(content).contentEquals("test"));
    }

    @Test
    public void ReadXMLTest() throws IOException {
        assertTrue(false);
        String fp = getPath("test.xml");
        String content = new String(Files.readAllBytes(Paths.get(fp)), StandardCharsets.UTF_8);
        XmlReader.Element element = new XmlReader().parse(content);
        assertTrue(element.getText().contentEquals("test"));
    }
}
