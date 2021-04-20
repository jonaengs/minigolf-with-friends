package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.minigolf.controller.ComponentMappers.GraphicalMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.LayerComparator;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Physical;

import java.util.Comparator;

import static com.mygdx.minigolf.util.Constants.SCREEN_HEIGHT;
import static com.mygdx.minigolf.util.Constants.SCREEN_WIDTH;

public class GraphicsSystem extends SortedIteratingSystem {

    // Number of pixels per meter
    public static final float PPM = 32f;

    // Height and width of camera frustum, based off width and height of the screen and pixel per meter ratio
    public static final float FRUSTUM_WIDTH = SCREEN_WIDTH / PPM;
    public static final float FRUSTUM_HEIGHT = SCREEN_HEIGHT / PPM;

    private final OrthographicCamera cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
    private final FitViewport viewport;

    private final Array<Entity> renderQueue = new Array<>(); // A sorted array of entities based on level
    private final Comparator<Entity> comparator = new com.mygdx.minigolf.controller.LayerComparator(); // A comparator to sort entities based on their level

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public GraphicsSystem() {
        super(Family.all(Physical.class, Graphical.class).get(), new LayerComparator());

        cam.position.set(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, 0);
        viewport = new FitViewport(FRUSTUM_WIDTH, FRUSTUM_HEIGHT, cam);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // Sort the renderQueue based on layer
        renderQueue.sort(comparator);

        cam.update();

        // Render shapes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        shapeRenderer.set(ShapeType.Filled);

        for (Entity entity : renderQueue) {
            Physical physical = PhysicalMapper.get(entity);

            if (!physical.isActive()) {
                continue;
            }

            Vector2 position = physical.getPosition();

            Graphical graphical = GraphicalMapper.get(entity);
            shapeRenderer.setColor(graphical.getColor());
            shapeRenderer.identity();

            switch (physical.getShape().getType()) {
                case Circle:
                    shapeRenderer.circle(position.x, position.y, physical.getShape().getRadius(), 50);
                    break;
                case Polygon:
                    float[] triangles = graphical.getTriangles();
                    shapeRenderer.translate(position.x, position.y, 0);
                    shapeRenderer.rotate(0, 0, 1, physical.getAngle());

                    if (triangles.length >= 6) {
                        for (int i = 0; i < triangles.length; i += 6) {
                            shapeRenderer.triangle(triangles[i], triangles[i+1], triangles[i+2], triangles[i+3], triangles[i+4], triangles[i+5]);

                        }
                    }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        renderQueue.clear();
    }

    @Override
    public void processEntity(Entity entity, float dt) {
        renderQueue.add(entity);
    }

    public OrthographicCamera getCam() {
        return cam;
    }

    public FitViewport getViewport() {
        return viewport;
    }
}