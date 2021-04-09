package com.mygdx.minigolf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

public class Game extends HeadlessGame {
	SpriteBatch batch;
	Texture img;
	public Music music;
	protected Screen screen;
	private static Game instance; // singleton, gdx is the only one creating the game instance


	@Override
	public void create() {
		super.create();
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		instance = this;

		music = Gdx.audio.newMusic(Gdx.files.internal("music/Maxime Abbey - Operation Stealth - The Ballad of J. & J.ogg"));
		music.setLooping(true);
		ScreenController.changeScreen(ScreenController.MAIN_MENU_VIEW);
	}

	@Override
	public void render() {
		if (screen != null) screen.render(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}


	@Override
	public void resize (int width, int height) {
		if (screen != null) screen.resize(width, height);
	}

	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
	}

	public void setScreen(Screen screen) {
		if (this.screen != null) this.screen.hide();
		this.screen = screen;
		if (this.screen != null) {
			this.screen.show();
			this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}

	public Screen getScreen() {
		return screen;
	}

	public static Game getInstance() {
		return instance;
	}
}