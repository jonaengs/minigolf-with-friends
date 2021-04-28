package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.util.ComponentMappers;
import com.mygdx.minigolf.util.ComponentMappers.GraphicalMapper;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.Constants;

/*
 *  Handles touch input to control movement of ball
 */
public class InputHandler extends InputAdapter {

    public final static Vector2 input = new Vector2(0, 0);
    private final OrthographicCamera cam;
    private final Body ball;
    private final Entity player;

    private final Body directionIndicatorBody;
    private final Body strengthIndicatorBody;

    private final Graphical strengthIndicatorGraphical;

    private final Vector3 dragStartPos = new Vector3();
    private final Vector3 draggingPos = new Vector3();

    public InputHandler(OrthographicCamera cam, Entity player, EntityFactory factory) {
        this.cam = cam;
        this.player = player;
        this.ball = PhysicalMapper.get(player).getBody();
        Entity directionIndicator = factory.createInputDirectionIndicator(ball.getPosition());
        Entity strengthIndicator = factory.createInputStrengthIndicator(ball.getPosition());

        this.directionIndicatorBody = PhysicalMapper.get(directionIndicator).getBody();
        this.strengthIndicatorGraphical = GraphicalMapper.get(strengthIndicator);
        this.strengthIndicatorBody = PhysicalMapper.get(strengthIndicator).getBody();
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

        if (ball.getLinearVelocity().isZero()) {
            if (!directionIndicatorBody.isActive()) {
                directionIndicatorBody.setTransform(ball.getPosition(), 0);
                directionIndicatorBody.setActive(true);
            }

            if (!strengthIndicatorBody.isActive()) {
                strengthIndicatorBody.setTransform(ball.getPosition(), 0);
                strengthIndicatorBody.setActive(true);
            }

        }

        float angle = (new Vector2(dragStartPos.x, dragStartPos.y).sub(draggingPos.x, draggingPos.y)).angleRad();

        // Set angle of direction indicator
        if (directionIndicatorBody.isActive()) {
            directionIndicatorBody.setTransform(ball.getPosition(), (float) (angle + Math.PI / 2));
        }

        // Set angle and length of strength indicator
        if (strengthIndicatorBody.isActive()) {
            strengthIndicatorBody.setTransform(ball.getPosition(), (float) (angle - Math.PI / 2));

            float[] triangles = strengthIndicatorGraphical.getTriangles();
            triangles[5] = Math.min(-Vector2.dst(dragStartPos.x, dragStartPos.y, draggingPos.x, draggingPos.y) * 0.4f, -0.41f);
            strengthIndicatorGraphical.setTriangles(triangles);
        }

        return true;
    }

    // TODO: Disable before level has loaded. Maybe just add a boolean
    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (directionIndicatorBody.isActive()) {
            directionIndicatorBody.setActive(false);
        }

        if (strengthIndicatorBody.isActive()) {
            strengthIndicatorBody.setActive(false);
        }

        if (ball.getLinearVelocity().isZero(Constants.MOVING_MARGIN)) {
            Vector3 dragEndPos = new Vector3(x, y, 0);
            cam.unproject(dragEndPos);

            // Convert dragging distance to amount of force to apply to the ball
            Vector3 force = dragStartPos.sub(dragEndPos);
            synchronized (input) {
                input.set(force.x, force.y);
                System.out.println("input: " + input);
            }
        }

        return true;
    }
}