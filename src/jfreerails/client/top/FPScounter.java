package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

/**
 * Provides a method that draws a String showing the average FPS over the last
 * complete 5000ms interval.
 * 
 * @author Luke
 * 
 */
public class FPScounter {

	private final double[] fpsValues = new double[400];

	private int newFrameCount = 0;

	private String newFPSstr = "starting..";

	private long lastFrameTime;

	private final int fontSize;

	private final Color bgColor;

	FPScounter() {
		this.fontSize = 10;
		bgColor = new Color(0, 0, 128);
	}

	// Display the average number of FPS.
	void updateFPSCounter() {
		long currentTime = System.nanoTime();

		if (newFrameCount == 0) {
			lastFrameTime = currentTime;
		}
		double dt = currentTime - lastFrameTime;
		double fps = 1000000000d / dt;
		fpsValues[newFrameCount % fpsValues.length] = fps;
		newFrameCount++;

		int n = fpsValues.length;
		if (newFrameCount > fpsValues.length) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;

			double mean = 0;
			for (int i = 0; i < fpsValues.length; i++) {
				min = Math.min(min, fpsValues[i]);
				max = Math.max(max, fpsValues[i]);
				mean += fpsValues[i];
			}
			mean = mean / n;
			if (mean > max)
				throw new IllegalStateException();

			if (mean < min)
				throw new IllegalStateException();

			double variance = 0;
			for (int i = 0; i < fpsValues.length; i++) {
				double xMinusU = fpsValues[i] - mean;
				variance += xMinusU * xMinusU;
			}
			variance = variance / n;
			if (newFrameCount % 20 == 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("FPS\n");
				sb.append(" n  ");
				sb.append(n);
				sb.append('\n');
				sb.append(" \u03BC  ");
				sb.append(Math.round(mean));
				sb.append('\n');
				sb.append(" \u03C3  ");
				sb.append(Math.round(Math.sqrt(variance)));
				sb.append('\n');
				sb.append(" min  ");
				sb.append(Math.round(min));
				sb.append('\n');
				sb.append(" max  ");
				sb.append(Math.round(max));
				sb.append('\n');

				newFPSstr = sb.toString();

			}

		}

		// g.setColor(Color.WHITE);
		// g.fillRect(50, 50, 50, 20);
		// g.setColor(Color.BLACK);
		// g.drawString(newFPSstr, 50, 65);
		lastFrameTime = currentTime;
	}

	void drawFPS(Graphics2D g) {
		int rectWidth;
		int rectHeight;
		int rectX;
		int rectY;

		int positionX = 50;
		int positionY = 70;

		Color textColor = Color.WHITE;

		String[] lines = newFPSstr.split("\n");
		rectWidth = 60;
		rectHeight = (int) ((fontSize + 1) * 1.2 * lines.length);
		rectY = (int) (positionY - fontSize * 1.2);
		rectX = positionX;

		g.setColor(bgColor);
		g.fillRect(rectX, rectY, rectWidth, rectHeight);

		g.setColor(textColor);
		// g.setFont(font);
		for (String s : lines) {
			g.drawString(s, positionX, positionY);
			positionY += fontSize * 1.2;
		}
	}
}
