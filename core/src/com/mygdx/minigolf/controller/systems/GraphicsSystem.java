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

        // Render shapes (for testing purposes)
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Entity entity : renderQueue) {
            Physical physical = physicalMapper.get(entity);
            Graphical graphical = graphicalMapper.get(entity);

            switch (physical.getShape().getType()) {
                case Circle:
                    shapeRenderer.setColor(Color.WHITE);
                    shapeRenderer.circle(physical.getPosition().x, physical.getPosition().y, physical.getShape().getRadius(), 50);
                    break;
                case Polygon:
                    shapeRenderer.setColor(Color.GRAY);
                    // Assuming polygon is a rectangle (as this is just for testing)
                    shapeRenderer.rect(physical.getPosition().x, physical.getPosition().y, graphical.getWidth(), graphical.getHeight());
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

}