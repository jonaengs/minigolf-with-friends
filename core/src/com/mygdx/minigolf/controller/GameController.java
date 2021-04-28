package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.systems.PowerUpSystem;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.ComponentMappers.PlayerMapper;

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

    public static void resetPhysicals(Entity player, LevelLoader.Level level) {
        Physical playerPhysics = PhysicalMapper.get(player);
        playerPhysics.setPosition(level.getSpawnCenter());
        playerPhysics.setVelocity(0, 0);
    }

    public Map<String, Entity> createPlayers(List<String> playerNames) {
        return playerNames.stream().collect(Collectors.toMap(
                name -> name,
                name -> game.factory.createPlayer(-1, -1, name)
        ));
    }

    public void resetPhysicals(Entity player) {
        resetPhysicals(player, currentLevel);
    }

    public void resetPlayers(Collection<Entity> players) {
        players.forEach(p -> {
                    resetPhysicals(p);
                    PlayerMapper.get(p).setCompleted(false);
                    PlayerMapper.get(p).removeExhaustedEffects();
                    PlayerMapper.get(p).resetStrokes();
                }
        );
    }

    public void loadLevel(String levelName) {
        if (currentLevel != null) {
            currentLevel.dispose(game.engine);
        }
        currentLevel = game.levelLoader.load(levelName);
        game.engine.getSystem(PowerUpSystem.class).setLevel(currentLevel);
    }
}
