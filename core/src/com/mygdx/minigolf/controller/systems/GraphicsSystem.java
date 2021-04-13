package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.minigolf.controller.ComponentMappers.TransformMapper;
import com.mygdx.minigolf.controller.ComponentMappers.GraphicalMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.LayerComparator;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Transform;

import java.util.Comparator;

public class GraphicsSystem extends SortedIteratingSystem {

    // Number of pixels per meter
    public static final float PPM = 32f;

    // Height and width of camera frustum, based off width and height of the screen and pixel per meter ratio
    public static final float FRUSTUM_WIDTH = Gdx.graphics.getWidth() / PPM;
    public static final float FRUSTUM_HEIGHT = Gdx.graphics.getHeight() / PPM;

    private final OrthographicCamera cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);

    private final Array<Entity> renderQueue = new Array<>(); // A sorted array of entities based on level
    private final Comparator<Entity> comparator = new com.mygdx.minigolf.controller.LayerComparator(); // A comparator to sort entities based on their level

    // A shape renderer used for testing purpose (not using textures)
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final PolygonSpriteBatch polygonSpriteBatch = new PolygonSpriteBatch();

    public GraphicsSystem() {
        super(Family.all(Graphical.class, Transform.class).get(), new LayerComparator());

        cam.position.set(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, 0);
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
        shapeRenderer.set(ShapeType.Filled);
        polygonSpriteBatch.setProjectionMatrix(cam.combined);
        polygonSpriteBatch.begin();

        for (Entity entity : renderQueue) {
            Transform transform = TransformMapper.get(entity);

            if (!transform.isVisible()) {
                continue;
            }

            Graphical graphical = GraphicalMapper.get(entity);

            if (graphical.getPolygonRegion() != null) {
                // Use graphical polygon region if it is set
                TextureRegion region = graphical.getPolygonRegion().getRegion();
                polygonSpriteBatch.draw(
                        graphical.getPolygonRegion(),
                        transform.getPosition().x, transform.getPosition().y,
                        region.getRegionWidth() / 2f, region.getRegionHeight() / 2f,
                        region.getRegionWidth(), region.getRegionHeight(),
                        transform.getScale().x, transform.getScale().y,
                        transform.getRotation()
                );

                continue;
            }

            Physical physical = PhysicalMapper.get(entity);

            // As fallback: Use body shape (circle) if entity has physical component
            if (physical != null) {
                if (physical.getShape().getType() == Shape.Type.Circle) {
                    shapeRenderer.setColor(graphical.getColor());
                    shapeRenderer.circle(transform.getPosition().x, transform.getPosition().y, physical.getShape().getRadius(), 50);
                }
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
}