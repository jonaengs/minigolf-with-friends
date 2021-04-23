package com.mygdx.minigolf.view;

import com.mygdx.minigolf.model.GameData;

// SingletonFactory
public class ViewFactory {
    private static GameView gameView;
    private static TutorialView tutorialView;
    private static SettingsView settingsView;
    private static LobbyView lobbyView;
    private static JoinGameView joinGameView;
    private static MainMenuView mainMenuView;
    private static ScoreView scoreView;

    public static GameView GameView() {
        if (gameView == null)
            gameView = new GameView();
        return gameView;
    }

    public static TutorialView TutorialView() {
        if (tutorialView == null)
            tutorialView = new TutorialView();
        return tutorialView;
    }

    public static SettingsView SettingsView() {
        if (settingsView == null)
            settingsView = new SettingsView();
        return settingsView;
    }

    public static LobbyView LobbyView() {
        GameData gameData = GameData.get();
        if (lobbyView == null)
            lobbyView = new LobbyView(gameData.lobbyID, gameData.playerNames);
        return lobbyView;
    }

    public static JoinGameView JoinGameView() {
        GameData gameData = GameData.get();
        if (joinGameView == null)
            joinGameView = new JoinGameView(gameData.lobbyID);
        return joinGameView;
    }

    public static MainMenuView MainMenuView() {
        if (mainMenuView == null)
            mainMenuView = new MainMenuView();
        return mainMenuView;
    }

    public static ScoreView ScoreView() {
        GameData gameData = GameData.get();
        if (scoreView == null)
            scoreView = new ScoreView(gameData.players, gameData.scores);
        return scoreView;
    }
}
