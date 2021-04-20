import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.levels.CourseElement;
import com.mygdx.minigolf.model.levels.CourseLoader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LevelLoaderTest extends TestFileLoader {
    LevelLoaderTest() {
        this.dir = "levels/copies/";
    }

    HeadlessGame game;

    @BeforeAll
    public void setup() throws InterruptedException {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        game = new HeadlessGame();

        // config.renderInterval = 1/30f;
        new HeadlessApplication(game, config);
        // Game runs in separate thread. Must wait for it to start. Better solution needed.
        Graphical.enabled = false;
        Thread.sleep(1000);
    }

    @Test
    public void allCoursesLoad() throws IOException {
        String content = getFileContents(CourseLoader.LEVELS_FILE);
        List<List<CourseElement>> courses = CourseLoader.getCourses(
                Arrays.stream(content.split("\n"))
                        .collect(Collectors.toMap(
                                fileName -> fileName, fileName -> {
                                    try {
                                        return new FileInputStream(getPath(fileName));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )));
        /*
        List<List<Entity>> levelsContents = courses.stream()
                .map(game.levelLoader::loadLevel)
                .collect(Collectors.toList());
         */
    }
}
