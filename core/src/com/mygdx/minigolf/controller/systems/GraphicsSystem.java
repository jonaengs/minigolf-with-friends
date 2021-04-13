package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.minigolf.controller.ComponentMappers.GraphicalMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.LayerComparator;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;

import java.util.Comparator;

public class GraphicsSystem extends SortedIteratingSystem {

    // Number of pixels per meter
    public static final float PPM = 32f;

    // Height and width of camera frustum, based off width and height of the screen and pixel per meter ratio
    public static final float FRUSTUM_WIDTH = CourseLoader.SCREEN_WIDTH / PPM;
    public static final float FRUSTUM_HEIGHT = CourseLoader.SCREEN_HEIGHT / PPM;

    private final OrthographicCamera cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
    private final FitViewport viewport;

    private final Array<Entity> renderQueue = new Array<>(); // A sorted array of entities based on level
    private final Comparator<Entity> comparator = new com.mygdx.minigolf.controller.LayerComparator(); // A comparator to sort entities based on their level

    // A shape renderer used for testing purpose (not using textures)
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final PolygonSpriteBatch polygonSpriteBatch = new PolygonSpriteBatch();

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
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        polygonSpriteBatch.setProjectionMatrix(cam.combined);
        polygonSpriteBatch.begin();

        for (Entity entity : renderQueue) {
            Physical physical = PhysicalMapper.get(entity);
            shapeRenderer.setColor(GraphicalMapper.get(entity).color);
            switch (physical.getShape().getType()) {
                case Circle:
                    shapeRenderer.set(ShapeType.Filled);
                    shapeRenderer.circle(physical.getPosition().x, physical.getPosition().y, physical.getShape().getRadius(), 50);
                    break;
                case Polygon:
                    polygonSpriteBatch.draw(GraphicalMapper.get(entity).polygonRegion, physical.getPosition().x, physical.getPosition().y);
            }
        }
        
        polygonSpriteBatch.end();
        shapeRenderer.end();
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