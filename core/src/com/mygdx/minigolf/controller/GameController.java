package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.util.ConcurrencyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GameController {
    HeadlessGame game;
    LevelLoader.Level currentLevel;

    public GameController(HeadlessGame game) {
        this.game = game;
    }

    public Map<String, Entity> createPlayers(List<String> playerNames) {
        return playerNames.stream().collect(Collectors.toMap(
                name -> name,
                ____ -> game.factory.createPlayer(-1, -1)
        ));
    }

    public void placeAtSpawn(Entity player) {
        PhysicalMapper.get(player).setPosition(currentLevel.getSpawnCenter());
    }

    public void resetPlayers(Collection<Entity> players) {
        players.forEach(p -> {
                    placeAtSpawn(p);
                    PlayerMapper.get(p).completed = false;
                }
        );
    }

    public void loadLevel(String levelName) {
        ConcurrencyUtils.waitForPostRunnable(() -> {
                    if (currentLevel != null) {
                       currentLevel.dispose(game.engine);
                    }
                    currentLevel = game.levelLoader.load(levelName);
                }
        );
    }
}
