/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.launcher;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Displays the first-milestone JavaFX shell and offers the current Swing launcher as a fallback.
 */
public final class JavaFxLauncher extends Application {

    private static LaunchRequest launchRequest;

    /** Prevents construction outside the JavaFX application lifecycle. */
    public JavaFxLauncher() {}

    /**
     * Starts JavaFX with a temporary route to the existing Swing launcher.
     *
     * @param swingFallback displays the Swing launcher when selected
     * @param onStarted runs after the JavaFX shell is displayed
     * @param onStopped runs when the JavaFX application shuts down
     */
    public static void launch(Runnable swingFallback, Runnable onStarted, Runnable onStopped) {
        launchRequest = new LaunchRequest(
                Objects.requireNonNull(swingFallback, "swingFallback"),
                Objects.requireNonNull(onStarted, "onStarted"),
                Objects.requireNonNull(onStopped, "onStopped"));
        Application.launch(JavaFxLauncher.class);
    }

    /**
     * Creates and displays the minimal JavaFX shell.
     *
     * @param stage the primary JavaFX window
     */
    @Override
    public void start(Stage stage) {
        LaunchRequest request = Objects.requireNonNull(launchRequest, "launchRequest");
        Label title = new Label("FreeRails JavaFX preview");
        Label message = new Label(
                "The launcher and gameplay screens will be migrated in later milestones.");
        Button continueButton = new Button("Continue to the current Swing launcher");
        continueButton.setOnAction(event -> {
            request.swingFallback().run();
            stage.close();
        });
        VBox content = new VBox(12, title, message, continueButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        stage.setTitle("FreeRails");
        stage.setScene(new Scene(content));
        stage.show();
        request.onStarted().run();
    }

    /** Runs the configured shutdown action when JavaFX closes. */
    @Override
    public void stop() {
        if (launchRequest != null) {
            launchRequest.onStopped().run();
        }
    }

    /** Holds the callbacks for the active JavaFX application lifecycle. */
    private record LaunchRequest(Runnable swingFallback, Runnable onStarted, Runnable onStopped) {}
}
