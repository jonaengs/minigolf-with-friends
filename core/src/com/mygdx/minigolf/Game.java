package com.mygdx.minigolf;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.controller.systems.PhysicsDebugSystem;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;

import java.util.List;

public class Game extends HeadlessGame {

    @Override
    public void create() {
        super.create();

        GraphicsSystem graphicsSystem = new GraphicsSystem();
        engine.addSystem(graphicsSystem);

        // --- Start dummy demo code ---
        Entity player = factory.createControllablePlayer(9, 12, graphicsSystem.getCam());
        levelLoader.loadLevel(CourseLoader.getCourses().get(0));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.4f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime()); // TODO: Move stuff to GameView
    }

    @Override
    public void dispose() {
    }
}
