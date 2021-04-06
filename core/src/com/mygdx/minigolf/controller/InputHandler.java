package com.mygdx.minigolf.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

/*
 *  Handles touch input to control movement of ball
 */
public class InputHandler extends InputAdapter {

    private final OrthographicCamera cam;
    private final Body ballBody;

    private final Vector3 dragStartPos = new Vector3();
    private final Vector3 draggingPos = new Vector3(); // Use this to draw the crosshair (when ball is not moving)

    public InputHandler(OrthographicCamera cam, Body ballBody) {
        this.cam = cam;
        this.ballBody = ballBody;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        dragStartPos.set(x, y, 0);
        cam.unproject(dragStartPos);

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
        if (ballBody.getLinearVelocity().x == 0 && ballBody.getLinearVelocity().y == 0) {
            Vector3 dragEndPos = new Vector3(x, y, 0);
            cam.unproject(dragEndPos);

            // Convert dragging distance to amount of force to apply to the ball
            Vector3 force = dragStartPos.sub(dragEndPos);

            // Apply force
            ballBody.applyLinearImpulse(force.x, force.y, dragEndPos.x, dragEndPos.y, true);
        }

        return true;
    }
}