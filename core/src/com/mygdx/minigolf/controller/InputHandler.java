package com.mygdx.minigolf.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.minigolf.Game;

/*
 *  Handles touch input to control movement of ball
 */
public class InputHandler extends InputAdapter {

    private final Game game;

    private boolean ballMoveOnTouchDown = false;

    private final Vector3 dragStartPos = new Vector3();
    private final Vector3 draggingPos = new Vector3(); // Use this to draw the crosshair (when ball is not moving)

    public InputHandler(Game game) {
        this.game = game;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        // Check if ball is moving
        if (game.getBallBody().getLinearVelocity().x == 0 && game.getBallBody().getLinearVelocity().y == 0) {
            ballMoveOnTouchDown = false;
            dragStartPos.set(x, y, 0);
            game.getCam().unproject(dragStartPos);
        } else {
            ballMoveOnTouchDown = true;
        }

        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (!ballMoveOnTouchDown) {
            draggingPos.set(x, y, 0);
            game.getCam().unproject(draggingPos);
        }

        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (!ballMoveOnTouchDown) {
            Vector3 dragEndPos = new Vector3(x, y, 0);
            game.getCam().unproject(dragEndPos);

            // Convert dragging distance to amount of force to apply to the ball
            Vector3 force = dragStartPos.sub(dragEndPos);

            // Apply force
            game.getBallBody().applyLinearImpulse(force.x, force.y, dragEndPos.x, dragEndPos.y, true);
        }

        return true;
    }
}