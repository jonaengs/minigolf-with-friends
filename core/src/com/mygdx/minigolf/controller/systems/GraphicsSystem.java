package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Physical;

import java.util.Comparator;

public class GraphicsSystem extends SortedIteratingSystem {

    // Number of pixels per meter
    private static final float PPM = 32.0f;

    // Height and width of camera frustum, based off width and height of the screen and pixel per meter ratio
    private static final float FRUSTUM_WIDTH = Gdx.graphics.getWidth() / PPM;
    private static final float FRUSTUM_HEIGHT = Gdx.graphics.getHeight() / PPM;

    private final OrthographicCamera cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);

    private final SpriteBatch batch;
    private final Array<Entity> renderQueue = new Array<>(); // A sorted array of entities based on level
    private final Comparator<Entity> comparator = new LayerComparator(); // A comparator to sort entities based on their level

    private final ComponentMapper<Physical> physicalMapper = ComponentMapper.getFor(Physical.class);
    private final ComponentMapper<Graphical> graphicalMapper = ComponentMapper.getFor(Graphical.class);

    // A shape renderer used for testing purpose (not using textures)
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public GraphicsSystem(SpriteBatch batch) {
        super(Family.all(Physical.class, Graphical.class).get(), new LayerComparator());

        this.batch = batch;
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
        shapeRenderer.setColor(Color.WHITE);

        for (Entity entity : renderQueue) {
            Physical physical = physicalMapper.get(entity);

            switch (physical.getShape().getType()) {
                case Circle:
                    shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.circle(physical.getPosition().x, physical.getPosition().y, physical.getShape().getRadius(), 50);
                    break;
                case Polygon:
                    shapeRenderer.set(ShapeRenderer.ShapeType.Line);
                    PolygonShape polygonShape = (PolygonShape) physical.getShape();

                    Vector2 vertex1 = new Vector2();
                    Vector2 vertex2 = new Vector2();

                    for (int i = 0; i < polygonShape.getVertexCount(); i++) {
                        polygonShape.getVertex(i, vertex1);
                        if (i == 0) {
                            polygonShape.getVertex(polygonShape.getVertexCount() - 1, vertex2);
                        } else {
                            polygonShape.getVertex(i - 1, vertex2);
                        }
                        vertex1.add(physical.getPosition());
                        vertex2.add(physical.getPosition());
                        shapeRenderer.line(vertex2, vertex1);
                    }
            }
        }

        shapeRenderer.end();

        /*
        // Render textures
        batch.setProjectionMatrix(cam.combined);
        batch.enableBlending();
        batch.begin();

        for (Entity entity : renderQueue) {
            Physical physical = physicalMapper.get(entity);
            Graphical graphical = graphicalMapper.get(entity);

            if (graphical.getTexture() == null) {
                continue;
            }

            float width = graphical.getTexture().getRegionWidth();
            float height = graphical.getTexture().getRegionHeight();

            float originX = width / 2f;
            float originY = height / 2f;

            batch.draw(
                    graphical.getTexture(),
                    physical.getPosition().x - originX,
                    physical.getPosition().y - originY,
                    width,
                    height
            );
        }

        batch.end();
        */

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