package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

import javax.swing.text.html.parser.Entity;


public class Game extends com.badlogic.gdx.Game {
    private static Game instance;
    public Music music;
    public static Vector2 spawnPosition = new Vector2(15,14);

    @Override
    public void create() {
        instance = this;

        music = Gdx.audio.newMusic(Gdx.files.internal("music/Maxime Abbey - Operation Stealth - The Ballad of J. & J.ogg"));
        music.setLooping(true);
        ScreenController.changeScreen(ScreenController.MAIN_MENU_VIEW);
        //ScreenController.changeScreen(ScreenController.SCORE_VIEW);
    }

    public static Game getInstance() {
        return instance;
    }
}
