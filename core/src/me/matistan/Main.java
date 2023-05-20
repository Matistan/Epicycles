package me.matistan;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends ApplicationAdapter {
	ShapeRenderer r;
	public static final int SCREEN_WIDTH = 1728;
	public static final int SCREEN_HEIGHT = 972;
	float radius, offset, freq, prevx, prevy, px, py, speed, xOffset, yOffset, zoom;
	double time, delta;
	Slider sliderLimit, sliderSpeed, sliderZoom;
	TextButton limitButton, speedButton, newButton, loadButton, centerButton, zoomButton;
	CheckBox centerCamera;
	TextField textField;
	Skin skin;
	Stage stage;
	boolean buttonReleased, buttonJustReleased, newDraw;
	int N, mouseX, mouseY, limit, signalLimit;
	ArrayList<ComplexNumber> signal, drawing;
	ArrayList<Circle> epicycles;

	@Override
	public void create () {
		r = new ShapeRenderer();
		r.translate(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
		skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		buttonReleased = true;
		newDraw = false;
		signalLimit = 400;
		signal = new ArrayList<>();
		epicycles = new ArrayList<>();
		drawing = new ArrayList<>();
		mouseX = 0;
		mouseY = 0;
		xOffset = 0;
		yOffset = 0;
		speed = 1;
		time = 0;
		N = 0;

		sliderLimit = new Slider(0, N, 1, false, skin);
		sliderLimit.setSize(400, 20);
		sliderLimit.setPosition(25, 25);
		stage.addActor(sliderLimit);

		limitButton = new TextButton("Circles: " + limit, skin);
		limitButton.setSize(400, 40);
		limitButton.setPosition(25, 50);
		limitButton.setTouchable(Touchable.disabled);
		stage.addActor(limitButton);

		sliderSpeed = new Slider(0, 4, 0.01f, false, skin);
		sliderSpeed.setSize(200, 20);
		sliderSpeed.setPosition(450, 25);
		sliderSpeed.setValue(1);
		stage.addActor(sliderSpeed);

		speedButton = new TextButton("Speed: " + speed, skin);
		speedButton.setSize(200, 40);
		speedButton.setPosition(450, 50);
		speedButton.setTouchable(Touchable.disabled);
		stage.addActor(speedButton);

		newButton = new TextButton("NEW", skin);
		newButton.setSize(50, 40);
		newButton.setPosition(675, 25);
		newButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				signal.clear();
				epicycles.clear();
				drawing.clear();
				N = 0;
				newDraw = true;
			}
		});
		stage.addActor(newButton);

		textField = new TextField("", skin);
		textField.setSize(300, 40);
		textField.setPosition(750, 25);
		stage.addActor(textField);

		loadButton = new TextButton("LOAD", skin);
		loadButton.setSize(50, 40);
		loadButton.setPosition(1075, 25);
		loadButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				File dane = new File(textField.getText());
				try {
					if(dane.exists()) {
						newDraw = true;
						signal.clear();
						epicycles.clear();
						drawing.clear();
						N = 0;
						Scanner scanner = new Scanner(dane);
						while(scanner.hasNext()) {
							signal.add(new ComplexNumber(Float.parseFloat(scanner.next()), Float.parseFloat(scanner.next())));
							N += 1;
						}
					}
				} catch (Exception ignored) {}
			}
		});
		stage.addActor(loadButton);

		centerCamera = new CheckBox(null, skin);
		centerCamera.setSize(25, 25);
		centerCamera.setPosition(1212, 25);
		stage.addActor(centerCamera);

		centerButton = new TextButton("Center the camera", skin);
		centerButton.setSize(150, 30);
		centerButton.setPosition(1150, 50);
		centerButton.setTouchable(Touchable.disabled);
		stage.addActor(centerButton);

		sliderZoom = new Slider(1, 1000, 0.05f, false, skin);
		sliderZoom.setSize(400, 20);
		sliderZoom.setPosition(1325, 25);
		sliderZoom.setValue(1);
		stage.addActor(sliderZoom);

		zoomButton = new TextButton("Zoom: " + zoom, skin);
		zoomButton.setSize(400, 40);
		zoomButton.setPosition(1325, 50);
		zoomButton.setTouchable(Touchable.disabled);
		stage.addActor(zoomButton);
	}

	@Override
	public void render () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		buttonJustReleased = !buttonReleased && !Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		buttonReleased = !Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		if(newDraw) {
			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				createSignal();
				drawSignal();
			}
			if(buttonJustReleased && N != 0) {
				createPath();

				limit = N;
				sliderLimit.setRange(0, N);
				sliderLimit.setValue(N);
				newDraw = false;
			}
		} else {
			limit = (int) sliderLimit.getValue();
			limitButton.setText("Circles: " + limit);
			delta = speed * 2 * Math.PI / N;
			if(time >= Math.PI * 2) {
				time = 0;
			}
			r = new ShapeRenderer();
			if(epicycles.size() > 0 && drawing.size() > 0) {
				if(centerCamera.isChecked()) {
					px = 0;
					py = 0;
					for(int i = 0; i < limit; i++) {
						px += (float) (Math.cos(epicycles.get(i).freq * time + epicycles.get(i).phase) * epicycles.get(i).radius);
						py += (float) (Math.sin(epicycles.get(i).freq * time + epicycles.get(i).phase) * epicycles.get(i).radius);
					}
					xOffset = -px;
					yOffset = -py;
				} else {
					xOffset = 0;
					yOffset = 0;
				}
			}
			r.translate(SCREEN_WIDTH / 2f + xOffset * zoom, SCREEN_HEIGHT / 2f + yOffset * zoom, 0);
			createDrawing();
			ScreenUtils.clear(0, 0, 0, 1);

			drawSignal();
			r.begin(ShapeRenderer.ShapeType.Line);
			r.setColor(0, 255, 255, 0.2f);
			px = 0;
			py = 0;
			for(int i = 0; i < limit; i++) {
				radius = epicycles.get(i).radius;
				offset = epicycles.get(i).phase;
				freq = epicycles.get(i).freq;
				r.circle(px * zoom, py * zoom, radius * zoom);
				prevx = px;
				prevy = py;
				px += (float) (Math.cos(freq * time + offset) * radius);
				py += (float) (Math.sin(freq * time + offset) * radius);
				r.setColor(255, 165, 0, 1f);
				r.line(prevx * zoom, prevy * zoom, px * zoom, py * zoom);
				r.setColor(255, 255, 255, 1f);
				for(int j = (int) (time * N / (2 * Math.PI)); j <= N; j++) {
					if(j > 0) {
						r.line(drawing.get(j).re * zoom, drawing.get(j).im * zoom, drawing.get(j - 1).re * zoom, drawing.get(j - 1).im * zoom);
					}
				}
				r.setColor(0, 255, 0, 1f);
				for(int j = 1; j < time * N / (2 * Math.PI); j++) {
					r.line(drawing.get(j).re * zoom, drawing.get(j).im * zoom, drawing.get(j - 1).re * zoom, drawing.get(j - 1).im * zoom);
				}
				r.setColor(0, 255, 255, 0.2f);
			}
			r.end();
			time += delta;
		}
		mouseX = Gdx.input.getX();
		mouseY = SCREEN_HEIGHT - 1 - Gdx.input.getY();
		speed = sliderSpeed.getValue();
		speedButton.setText("Speed: " + String.format("%.2f",speed));
		zoom = sliderZoom.getValue();
		zoomButton.setText("Zoom: " + String.format("%.2f",zoom));
		stage.draw();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	void drawSignal() {
		r.begin(ShapeRenderer.ShapeType.Line);
		r.setColor(255, 0, 0, 1f);
		for(int i = 1; i < N; i++) {
			r.line(signal.get(i).re * zoom, signal.get(i).im * zoom, signal.get(i - 1).re * zoom, signal.get(i - 1).im * zoom);
		}
		r.end();
	}

	void createPath() {
		if(signal.size() > signalLimit) {
			fixSignal();
		}
		for(int k = 0; k < N; k++) {
			ComplexNumber sum = new ComplexNumber(0, 0);
			for(int n = 0; n < N; n++) {
				float phi = (float) ((2 * Math.PI * k * n) / N);
				sum.add(signal.get(n).multiply(new ComplexNumber((float) Math.cos(phi), (float) -(Math.sin(phi)))));
			}
			sum.re /= N;
			sum.im /= N;
			if(k < N / 2) {
				epicycles.add(new Circle(sum, k, (float) Math.sqrt(Math.pow(sum.re, 2) + Math.pow(sum.im, 2)), (float) Math.atan2(sum.im, sum.re)));
			} else {
				epicycles.add(new Circle(sum, k-N, (float) Math.sqrt(Math.pow(sum.re, 2) + Math.pow(sum.im, 2)), (float) Math.atan2(sum.im, sum.re)));
			}
		}
		sort();
		delta = 2 * Math.PI / N;
		time = 0;
	}

	void createDrawing() {
		drawing.clear();
		for(float t = 0; t < 2 * Math.PI; t += 2 * Math.PI / N) {
			px = 0;
			py = 0;
			for(int i = 0; i < limit; i++) {
				px += (float) (Math.cos(epicycles.get(i).freq * t + epicycles.get(i).phase) * epicycles.get(i).radius);
				py += (float) (Math.sin(epicycles.get(i).freq * t + epicycles.get(i).phase) * epicycles.get(i).radius);
			}
			drawing.add(new ComplexNumber(px, py));
		}
		drawing.add(drawing.get(0));
	}

	void fixSignal() {
		for(int i = 0; i < signalLimit; i++) {
			signal.set(i, signal.get(i * signal.size() / signalLimit));
		}
		for(int i = signalLimit; i < signal.size(); i++) {
			signal.remove(i);
			i--;
		}
		N = signalLimit;
	}

	void createSignal() {
		if(Gdx.input.getX() != mouseX || SCREEN_HEIGHT - 1 - Gdx.input.getY() != mouseY) {
			signal.add(new ComplexNumber(Gdx.input.getX() - SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f - 1 - Gdx.input.getY()));
			N += 1;
		}
	}

	void sort() {
		for(int i = 0; i < N - 1; i++) {
			if(epicycles.get(i).radius < epicycles.get(i + 1).radius) {
				Circle temp = epicycles.get(i);
				epicycles.set(i, epicycles.get(i + 1));
				epicycles.set(i + 1, temp);
				i = -1;
			}
		}
	}
	@Override
	public void dispose () {
		r.dispose();
		stage.dispose();
	}
}