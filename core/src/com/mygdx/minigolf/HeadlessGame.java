package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;

// See link below for example of use
// https://github.com/TomGrill/gdx-testing/blob/master/tests/src/de/tomgrill/gdxtesting/GdxTestRunner.java
public class HeadlessGame implements ApplicationListener {
    // Possible plan:
        // implement game logic here
        // extend with Game class. Implement Headless-less things there.

    private Engine engine;
    private EntityFactory factory;

    @Override
    public void create() {
        engine = new Engine();
        factory = new EntityFactory(engine, new World(new Vector2(0, 0), true), new OrthographicCamera());
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public Engine getEngine() {
        return engine;
    }

    public EntityFactory getFactory() {
        return factory;
    }
}
