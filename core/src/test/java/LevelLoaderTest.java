import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.levels.Course;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LevelLoaderTest extends TestFileLoader {
    LevelLoaderTest() {
        this.dir = "levels/copies/";
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
                            }
                        }
                ))
        );
        /*
        Below code crashes due tp Gdx not being initialized. Need headless application to run.

        EntityFactory.setEngine(new Engine());
        new World(new Vector2(0, 0), true);
        List<List<Entity>> levelsContents = courses.stream().map(LevelLoader::loadLevel).collect(Collectors.toList());
         */
    }
}
