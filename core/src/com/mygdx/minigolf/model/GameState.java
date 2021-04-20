package com.mygdx.minigolf.model;

import com.mygdx.minigolf.model.levels.LevelLoader.Level;

import java.util.List;
import java.util.Map;
// TODO: Create GameController class. Has methods for: Spawning players, hiding completed players, etc.
public class GameState {
    List<String> playerList;
    Map<String, Integer> scores;
    Level currentLevel;
    // STATE = IN_GAME, SCORE_SCREEN, LOADING_LEVEL, GAME_OVER,
}
