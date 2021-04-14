package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.levels.CourseLoader;

import java.util.Arrays;

/*
 *  Handles touch input to control movement of ball
 */
public class InputHandler extends InputAdapter {

    private final OrthographicCamera cam;
    private final Body ballBody;

    private final Vector3 dragStartPos = new Vector3();
    private final Vector3 draggingPos = new Vector3(); // Use this to draw the crosshair (when ball is not moving)

    public final static Vector2 input = new Vector2(0, 0);

    public InputHandler(OrthographicCamera cam, Body ballBody) {
        this.cam = cam;
        this.ballBody = ballBody;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        dragStartPos.set(x, y, 0);
        // cam.unproject(dragStartPos);
        cam.unproject(dragStartPos, 0, 0, CourseLoader.SCREEN_WIDTH, CourseLoader.SCREEN_HEIGHT);

        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        draggingPos.set(x, y, 0);
        cam.unproject(draggingPos);

        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        // Check if ball is moving
        if (ballBody.getLinearVelocity().isZero(0.01f)) {
            Vector3 dragEndPos = new Vector3(x, y, 0);
            // cam.unproject(dragEndPos);
            cam.unproject(dragEndPos, 0, 0, CourseLoader.SCREEN_WIDTH, CourseLoader.SCREEN_HEIGHT);

            // Convert dragging distance to amount of force to apply to the ball
            Vector3 force = dragStartPos.sub(dragEndPos);
            synchronized (input) {
                input.set(force.x, force.y);
                System.out.println("FORCE: " + force);
                System.out.println("input: " + input);
            }

            // Apply force
            ballBody.setLinearVelocity(input);
            // ballBody.applyLinearImpulse(force.x, force.y, dragEndPos.x, dragEndPos.y, true);
        }

        return true;
    }
}