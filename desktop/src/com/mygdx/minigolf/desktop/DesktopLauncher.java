package com.mygdx.minigolf.desktop;


import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.HeadlessGame;

import java.util.Arrays;

/**
 * Command line args:
 * - "SERVER": Include to make game run in server mode
 * Planned args:
 * - "TICK_RATE=N": Set tick rate. Game will then update every 1/N seconds
 *
 * NOTE: May be a good idea to switch to using environment variables
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		if (Arrays.asList(arg).contains("SERVER")) {
			HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
			new HeadlessApplication(new HeadlessGame(), config);
			System.out.println("SERVER UP!");
		} else {
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.x = 100;
			config.y = 100;
			config.width = 1280;
			config.height = 720;
			new LwjglApplication(new Game(), config);
		}
	}
}
