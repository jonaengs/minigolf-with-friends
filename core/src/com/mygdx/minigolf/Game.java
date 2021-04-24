package com.mygdx.minigolf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.mygdx.minigolf.controller.ClientGameController;
import com.mygdx.minigolf.controller.ScreenController;
import com.mygdx.minigolf.view.ViewFactory;

import java.io.IOException;


public class Game extends com.badlogic.gdx.Game {
    private static Game instance;
    public Music music;
    public ClientGameController gameController;
    public ScreenController screenController;

    public static Game getInstance() {
        return instance;
    }

    @Override
    public void create() {
        instance = this;
        music = Gdx.audio.newMusic(Gdx.files.internal("music/Maxime Abbey - Operation Stealth - The Ballad of J. & J.ogg"));
        music.setLooping(true);
        music.setVolume(1);

        screenController = new ScreenController(this);
        screenController.changeScreen(ViewFactory.MainMenuView());

        try {
            gameController = new ClientGameController(ViewFactory.GameView());
        } catch (IOException e) {
            // No use continuing without a game controller. Let it crash.
            throw new RuntimeException(e);
        }
    }
}