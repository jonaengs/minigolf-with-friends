/*
 *	Changes in this file is for testing purposes only, and will be removed
 */

package com.mygdx.minigolf;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.minigolf.controller.InputHandler;


public class Game extends ApplicationAdapter {

	// Width and height in world units
    public final static Integer WIDTH = 90;
    public final static Integer HEIGHT = 185;

    private Viewport viewport;
    private OrthographicCamera cam;

    private ShapeRenderer shapes;
    private World world;

    private Body groundBody;
    private Body ballBody;

    @Override
    public void create() {
        shapes = new ShapeRenderer();
        cam = new OrthographicCamera(WIDTH, HEIGHT);
        cam.setToOrtho(false, WIDTH, HEIGHT);
        viewport = new FitViewport(cam.viewportWidth, cam.viewportHeight, cam);
        cam.update();

        Gdx.input.setInputProcessor(new InputHandler(this));

        world = new World(new Vector2(0, 0), true);

        // Create a ground and a ball and add it to the world
        createGround();
        createBall();

        // Friction between ball and ground
        FrictionJointDef frictionJointDef = new FrictionJointDef();
        frictionJointDef.initialize(groundBody, ballBody, new Vector2(0, 0));
        frictionJointDef.maxForce = 50.0f;
        world.createJoint(frictionJointDef);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.update();

        shapes.setProjectionMatrix(cam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0x336600FF));
        shapes.rect(groundBody.getPosition().x, groundBody.getPosition().y, WIDTH, HEIGHT);
        shapes.setColor(Color.WHITE);
        shapes.circle(ballBody.getPosition().x, ballBody.getPosition().y, ballBody.getFixtureList().get(0).getShape().getRadius());
        shapes.end();

        world.step(1 / 60f, 6, 2);

    }

    @Override
    public void dispose() {
        shapes.dispose();
        world.dispose();
    }

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

    public Body getBallBody() {
        return ballBody;
    }

	public OrthographicCamera getCam() {
		return cam;
	}

    private void createBall() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(WIDTH / 2f, HEIGHT / 2f);

        ballBody = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(1);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;

        ballBody.createFixture(fixtureDef);

        circle.dispose();
    }

    private void createGround() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;
        groundBodyDef.position.set(0, 0);

        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(WIDTH, HEIGHT);

        groundBody.createFixture(groundBox, 0.0f);

        groundBox.dispose();
    }
}

