package com.mygdx.minigolf.controller.screenControllers;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.view.screens.JoinGameScreen;
import com.mygdx.minigolf.view.screens.MainMenuScreen;
import com.mygdx.minigolf.view.screens.NewGameScreen;
import com.mygdx.minigolf.view.screens.SettingsScreen;
import com.mygdx.minigolf.view.screens.TutorialScreen;

public class ScreenController {

    private static final ScreenController INSTANCE = new ScreenController();

    public final static int MENU = 0;
    public final static int NEW_GAME = 1;
    public final static int JOIN_GAME = 2;
    public final static int TUTORIAL = 3;
    public final static int SETTINGS = 4;

    private MainMenuScreen mainMenuScreen;
    private TutorialScreen tutorialScreen;
    private SettingsScreen settingsScreen;
    private NewGameScreen newGameScreen;
    private JoinGameScreen joinGameScreen;

    private ScreenController() {
        // Private constructor, using class as Singleton
    }

    public static ScreenController getInstance() {
        return INSTANCE;
    }

    public void changeScreen(int screen, Game parent) {
        switch(screen) {
            case MENU:
                mainMenuScreen = new MainMenuScreen(parent);
                parent.setScreen(mainMenuScreen);
                break;
            case NEW_GAME:
                newGameScreen = new NewGameScreen(parent);
                parent.setScreen(newGameScreen);
                break;
            case JOIN_GAME:
                joinGameScreen = new JoinGameScreen(parent);
                parent.setScreen(joinGameScreen);
                break;
            case TUTORIAL:
                tutorialScreen = new TutorialScreen(parent);
                parent.setScreen(tutorialScreen);
                break;
            case SETTINGS:
                settingsScreen = new SettingsScreen(parent);
                parent.setScreen(settingsScreen);
                break;
        }
    }

    public void mainMenu(TextButton newGame, TextButton settings, TextButton tutorial, final TextButton join, final Game parent) {

        // When new game button on main menu is clicked
        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                changeScreen(NEW_GAME, parent);
            }
        });

        // When settings button on main menu is clicked
        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                changeScreen(SETTINGS, parent);
            }
        });

        // When tutorial button on main menu is clicked
        tutorial.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                changeScreen(TUTORIAL, parent);
            }
        });

        // When join game button is clicked
        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                changeScreen(JOIN_GAME, parent);
            }
        });
    }

    public void newGame(TextButton start, final Game parent) {
        start.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Go to game actual game
            }
        });
    }

    public void joinGame(TextButton join, final TextField code, final Game parent) {
        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Join lobby, access text in textfield
                // code.getText();
            }
        });
    }

    public void settings(final CheckBox volume, final Game parent) {
        volume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(volume.isChecked()) {
                    com.mygdx.minigolf.Game.music.play();
                } else {
                    com.mygdx.minigolf.Game.music.pause();
                }
            }
        });
    }

    public void catchBackKey(Game parent) {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        if(Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            changeScreen(MENU, parent);
        }
    }
}
