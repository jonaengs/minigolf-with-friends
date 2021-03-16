import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.levels.Course;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class LevelLoaderTest extends TestFileLoader {
    LevelLoaderTest() {
        this.dir = "levels/copies/";
    }


    @BeforeAll
    public static void setup() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        Gdx.gl = mock(GL20.class);
        HeadlessGame game = new HeadlessGame();
        game.create();
        new HeadlessApplication(game, config);
    }

    @Test
    public void allCoursesLoad() throws IOException {
        String content = getFileContents(CourseLoader.LEVELS_FILE);
        List<Course> courses = CourseLoader.getCourses(
                Arrays.stream(content.split("\n"))
                .collect(Collectors.toMap(
                        file -> file, file -> {
                            try {
                                return new FileInputStream(getPath(file));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }}
                )));
        List<List<Entity>> levelsContents = courses.stream()
                .map(LevelLoader::loadLevel)
                .collect(Collectors.toList());
    }
}
