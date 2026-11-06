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

import freerails.client.ClientConstants;
import freerails.client.ModelRootImpl;
import freerails.client.ModelRootListener;
import freerails.client.ModelRootProperty;
import freerails.client.ActionRoot;
import freerails.client.renderer.map.MapRenderer;
import freerails.client.view.FreerailsCursor;
import freerails.controller.BuildMode;
import freerails.controller.BuildTrackController;
import freerails.controller.TrackMoveProducer;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.terrain.TileTransition;
import freerails.model.world.WorldMapListener;
import freerails.util.Vector2D;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * JavaFX map and overview controls backed by the existing Java2D renderers.
 */
final class JavaFxMapView implements ModelRootListener, WorldMapListener, AutoCloseable {

    private static final TaggedLogger LOGGER = Logger.tag("JavaFxMapView");
    private final ModelRootImpl modelRoot;
    private final MapRenderer mainRenderer;
    private final MapRenderer overviewRenderer;
    private final FreerailsCursor cursor;
    private final ActionRoot actionRoot;
    private final BuildTrackController buildTrackController;
    private final Object updateLock;
    private final ExecutorService renderExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "JavaFX map rendering");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean rendering = new AtomicBoolean();
    private final AtomicBoolean renderRequested = new AtomicBoolean();
    private final AtomicBoolean failed = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong renderVersion = new AtomicLong();
    private final ScrollPane scrollPane = new ScrollPane();
    private final Pane mapContent = new Pane();
    private final ImageView mapImage = new ImageView();
    private final Pane overviewPane = new Pane();
    private final ImageView overviewImage = new ImageView();
    private final javafx.scene.shape.Rectangle overviewViewport = new javafx.scene.shape.Rectangle();
    private final Timeline animation;
    private final Consumer<String> onRenderFailure;
    private int mapWidth;
    private int mapHeight;
    private double visibleX;
    private double visibleY;
    private Vector2D dragStart;
    private double panStartScreenX;
    private double panStartScreenY;
    private double panStartHorizontalValue;
    private double panStartVerticalValue;
    private boolean panning;
    private volatile RenderViewport renderViewport = new RenderViewport(1, 1, 0, 0);
    private volatile long quickMessageExpiresAt;

    /**
     * Creates JavaFX controls for the detailed and overview maps.
     *
     * @param modelRoot game properties and world state
     * @param mainRenderer detailed map renderer
     * @param overviewRenderer overview map renderer
     * @param cursor map cursor painter
     * @param actionRoot source of existing map actions
     * @param buildTrackController asynchronous track preview controller
     * @param updateLock synchronizes game, render, and input access
     * @param onRenderFailure displays render failures to the player
     */
    JavaFxMapView(
            ModelRootImpl modelRoot,
            MapRenderer mainRenderer,
            MapRenderer overviewRenderer,
            FreerailsCursor cursor,
            ActionRoot actionRoot,
            BuildTrackController buildTrackController,
            Object updateLock,
            Consumer<String> onRenderFailure) {
        this.modelRoot = Objects.requireNonNull(modelRoot, "modelRoot");
        this.mainRenderer = Objects.requireNonNull(mainRenderer, "mainRenderer");
        this.overviewRenderer = Objects.requireNonNull(overviewRenderer, "overviewRenderer");
        this.cursor = Objects.requireNonNull(cursor, "cursor");
        this.actionRoot = Objects.requireNonNull(actionRoot, "actionRoot");
        this.buildTrackController = Objects.requireNonNull(buildTrackController, "buildTrackController");
        this.updateLock = Objects.requireNonNull(updateLock, "updateLock");
        this.onRenderFailure = Objects.requireNonNull(onRenderFailure, "onRenderFailure");

        freerails.util.Vector2D mapSize = mainRenderer.getMapSizeInPixels();
        mapWidth = mapSize.x();
        mapHeight = mapSize.y();
        mapContent.setMinSize(mapWidth, mapHeight);
        mapContent.setPrefSize(mapWidth, mapHeight);
        mapContent.setMaxSize(mapWidth, mapHeight);
        mapContent.getChildren().add(mapImage);
        scrollPane.setContent(mapContent);
        scrollPane.setPannable(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setFocusTraversable(true);
        scrollPane.setStyle("-fx-background: #202020; -fx-background-color: #202020;");

        overviewPane.setMinSize(200, 200);
        overviewPane.setPrefSize(200, 200);
        overviewPane.setMaxSize(200, 200);
        overviewImage.setPreserveRatio(false);
        overviewViewport.setFill(Color.TRANSPARENT);
        overviewViewport.setStroke(Color.WHITE);
        overviewViewport.setStrokeWidth(1);
        overviewPane.getChildren().setAll(overviewImage, overviewViewport);

        scrollPane.hvalueProperty().addListener(
                (observable, oldValue, newValue) -> captureViewportAndRender());
        scrollPane.vvalueProperty().addListener(
                (observable, oldValue, newValue) -> captureViewportAndRender());
        scrollPane.viewportBoundsProperty().addListener(
                (observable, oldValue, newValue) -> captureViewportAndRender());
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        mapImage.setOnMousePressed(this::handleMousePressed);
        mapImage.setOnMouseDragged(this::handleMouseDragged);
        mapImage.setOnMouseReleased(this::handleMouseReleased);
        overviewPane.setOnMouseClicked(event -> {
            double tileX = event.getX() / overviewRenderer.scale();
            double tileY = event.getY() / overviewRenderer.scale();
            centerOnTile(new Vector2D((int) tileX, (int) tileY));
        });

        modelRoot.addPropertyChangeListener(this);
        modelRoot.addMapListener(this);
        animation = new Timeline(new KeyFrame(Duration.millis(40), event -> requestRender()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
        captureViewportAndRender();
    }

    /**
     * Returns the detailed map scroll pane.
     *
     * @return the map scroll pane
     */
    Node getMainView() {
        return scrollPane;
    }

    /**
     * Returns the overview map with a visible-area indicator.
     *
     * @return the overview map pane
     */
    Node getOverviewView() {
        return overviewPane;
    }

    /**
     * Centers the detailed map on a map tile.
     *
     * @param tile map coordinate to center
     */
    void centerOnTile(Vector2D tile) {
        Bounds viewport = scrollPane.getViewportBounds();
        double maxX = Math.max(0, mapWidth - viewport.getWidth());
        double maxY = Math.max(0, mapHeight - viewport.getHeight());
        double x = tile.x() * mainRenderer.scale() - viewport.getWidth() / 2;
        double y = tile.y() * mainRenderer.scale() - viewport.getHeight() / 2;
        scrollPane.setHvalue(maxX == 0 ? 0 : clamp(x / maxX));
        scrollPane.setVvalue(maxY == 0 ? 0 : clamp(y / maxY));
        requestRender();
    }

    /**
     * Invalidates the map cache after a world-map move.
     *
     * @param changedTiles changed tile bounds
     */
    @Override
    public void tilesChanged(java.awt.Rectangle changedTiles) {
        synchronized (updateLock) {
            long tileCount = (long) changedTiles.width * changedTiles.height;
            if (tileCount > 100) {
                mainRenderer.refreshAll();
                overviewRenderer.refreshAll();
            } else {
                java.awt.Rectangle bounds = new java.awt.Rectangle(0, 0,
                        modelRoot.getWorld().getMapSize().x(),
                        modelRoot.getWorld().getMapSize().y());
                java.awt.Rectangle clipped = changedTiles.intersection(bounds);
                for (int x = clipped.x; x < clipped.x + clipped.width; x++) {
                    for (int y = clipped.y; y < clipped.y + clipped.height; y++) {
                        Vector2D tile = new Vector2D(x, y);
                        mainRenderer.refreshTile(tile);
                        overviewRenderer.refreshTile(tile);
                    }
                }
            }
        }
        requestRender();
    }

    /**
     * Schedules a redraw when map-visible client properties change.
     *
     * @param property changed property
     * @param oldValue previous property value
     * @param newValue updated property value
     */
    @Override
    public void propertyChange(ModelRootProperty property, Object oldValue, Object newValue) {
        if (property == ModelRootProperty.QUICK_MESSAGE) {
            quickMessageExpiresAt = newValue == null ? 0 : System.currentTimeMillis() + 5_000;
        }
        switch (property) {
            case CURSOR_POSITION, CURSOR_MESSAGE, PERMANENT_MESSAGE, QUICK_MESSAGE,
                    CURSOR_MODE, TRACK_BUILDER_MODE, PROPOSED_TRACK, THINKING_POINT,
                    IGNORE_KEY_EVENTS, SHOW_CARGO_AT_STATIONS, SHOW_STATION_NAMES,
                    SHOW_STATION_BORDERS -> requestRender();
            default -> {
            }
        }
    }

    /**
     * Stops animation and rendering resources when the gameplay window closes.
     */
    @Override
    public void close() {
        closed.set(true);
        animation.stop();
        synchronized (updateLock) {
            modelRoot.removePropertyChangeListener(this);
            modelRoot.removeMapListener(this);
        }
        renderExecutor.shutdownNow();
    }

    /**
     * Requests a coalesced map and overview render away from the JavaFX thread.
     */
    private void requestRender() {
        if (closed.get() || failed.get()) {
            return;
        }
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::requestRender);
            return;
        }
        renderRequested.set(true);
        if (rendering.compareAndSet(false, true)) {
            renderExecutor.execute(this::renderLoop);
        }
    }

    /**
     * Snapshots JavaFX viewport state before scheduling background rendering.
     */
    private void captureViewportAndRender() {
        Bounds viewport = scrollPane.getViewportBounds();
        double width = Math.max(1, viewport.getWidth());
        double height = Math.max(1, viewport.getHeight());
        double horizontalValue = scrollPane.getHvalue();
        double verticalValue = scrollPane.getVvalue();
        renderViewport = new RenderViewport(width, height, horizontalValue, verticalValue);
        visibleX = Math.round(horizontalValue * Math.max(0, mapWidth - width));
        visibleY = Math.round(verticalValue * Math.max(0, mapHeight - height));
        requestRender();
    }

    /**
     * Renders current visible map bounds and publishes completed images.
     */
    private void renderLoop() {
        try {
            while (renderRequested.getAndSet(false)) {
                long version = renderVersion.incrementAndGet();
                RenderedImages images = renderVisibleImages();
                Platform.runLater(() -> publish(images, version));
            }
        } catch (RuntimeException exception) {
            failed.set(true);
            LOGGER.error(exception, "Could not render the JavaFX map view.");
            Platform.runLater(() -> onRenderFailure.accept(
                    "Map rendering stopped: " + exception.getMessage()));
        } finally {
            rendering.set(false);
            if (renderRequested.get()) {
                requestRender();
            }
        }
    }

    /**
     * Renders map pixels and viewport information under the game update lock.
     *
     * @return rendered detailed and overview images
     */
    private RenderedImages renderVisibleImages() {
        RenderViewport viewport = renderViewport;
        int width = Math.max(1, (int) Math.ceil(viewport.width()));
        int height = Math.max(1, (int) Math.ceil(viewport.height()));
        double maxX = Math.max(0, mapWidth - viewport.width());
        double maxY = Math.max(0, mapHeight - viewport.height());
        int x = (int) Math.round(viewport.horizontalValue() * maxX);
        int y = (int) Math.round(viewport.verticalValue() * maxY);
        x = Math.max(0, Math.min(x, Math.max(0, mapWidth - width)));
        y = Math.max(0, Math.min(y, Math.max(0, mapHeight - height)));
        java.awt.Rectangle visible = new java.awt.Rectangle(x, y, width, height);

        synchronized (updateLock) {
            BufferedImage mainImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = mainImage.createGraphics();
            try {
                graphics.translate(-x, -y);
                mainRenderer.paintRect(graphics, visible);
                cursor.paintCursor(graphics,
                        new Dimension(ClientConstants.TILE_PIXEL_SIZE, ClientConstants.TILE_PIXEL_SIZE));
                paintMessages(graphics, visible);
            } finally {
                graphics.dispose();
            }

            freerails.util.Vector2D overviewSize = overviewRenderer.getMapSizeInPixels();
            int overviewWidth = Math.max(1, overviewSize.x());
            int overviewHeight = Math.max(1, overviewSize.y());
            BufferedImage overview = new BufferedImage(
                    overviewWidth, overviewHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D overviewGraphics = overview.createGraphics();
            try {
                overviewRenderer.paintRect(
                        overviewGraphics, new java.awt.Rectangle(0, 0, overviewWidth, overviewHeight));
            } finally {
                overviewGraphics.dispose();
            }

            double scaleX = overviewWidth / (double) mapWidth;
            double scaleY = overviewHeight / (double) mapHeight;
            return new RenderedImages(
                    x, y, width, height, mainImage.getRGB(0, 0, width, height, null, 0, width),
                    overviewWidth, overviewHeight,
                    overview.getRGB(0, 0, overviewWidth, overviewHeight, null, 0, overviewWidth),
                    x * scaleX, y * scaleY, visible.getWidth() * scaleX, visible.getHeight() * scaleY);
        }
    }

    /**
     * Publishes image pixels and updates the overview viewport on the FX thread.
     *
     * @param images newly rendered image data
     * @param version rendering generation
     */
    private void publish(RenderedImages images, long version) {
        if (closed.get() || version != renderVersion.get()) {
            return;
        }
        WritableImage detailed = new WritableImage(images.width(), images.height());
        detailed.getPixelWriter().setPixels(0, 0, images.width(), images.height(),
                PixelFormat.getIntArgbInstance(), images.mainPixels(), 0, images.width());
        mapImage.setImage(detailed);
        mapImage.relocate(images.x(), images.y());

        WritableImage overview = new WritableImage(images.overviewWidth(), images.overviewHeight());
        overview.getPixelWriter().setPixels(0, 0, images.overviewWidth(), images.overviewHeight(),
                PixelFormat.getIntArgbInstance(), images.overviewPixels(), 0, images.overviewWidth());
        overviewImage.setImage(overview);
        overviewImage.relocate(0, 0);
        overviewViewport.setX(images.viewportX());
        overviewViewport.setY(images.viewportY());
        overviewViewport.setWidth(images.viewportWidth());
        overviewViewport.setHeight(images.viewportHeight());

        visibleX = images.x();
        visibleY = images.y();
    }

    /**
     * Draws transient and permanent user messages into the visible map image.
     *
     * @param graphics detailed map graphics context
     * @param visible visible map bounds
     */
    private void paintMessages(Graphics2D graphics, java.awt.Rectangle visible) {
        String quickMessage = (String) modelRoot.getProperty(ModelRootProperty.QUICK_MESSAGE);
        String permanentMessage = (String) modelRoot.getProperty(ModelRootProperty.PERMANENT_MESSAGE);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.setFont(ClientConstants.USER_MESSAGE_FONT);
        if (quickMessage != null && System.currentTimeMillis() < quickMessageExpiresAt) {
            int line = 0;
            for (String text : quickMessage.split("\\R")) {
                graphics.drawString(text, visible.x + 50, visible.y + 50 + line++ * 20);
            }
        }
        if (permanentMessage != null) {
            graphics.setFont(ClientConstants.LARGE_MESSAGE_FONT);
            int textWidth = graphics.getFontMetrics().stringWidth(permanentMessage);
            int textHeight = graphics.getFontMetrics().getHeight();
            graphics.setColor(java.awt.Color.LIGHT_GRAY);
            graphics.drawString(permanentMessage,
                    visible.x + (visible.width - textWidth) / 2,
                    visible.y + (visible.height - textHeight) / 2);
        }
    }

    /**
     * Moves the game cursor to the selected map tile if the tile is valid.
     *
     * @param tile destination map coordinate
     */
    private void moveCursor(Vector2D tile) {
        if (modelRoot.getWorld().boundsContain(tile)) {
            modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, tile);
            requestRender();
        }
    }

    /**
     * Maps supported keyboard controls to existing cursor and build actions.
     *
     * @param event JavaFX key event
     */
    private void handleKeyPressed(KeyEvent event) {
        TileTransition transition = switch (event.getCode()) {
            case NUMPAD1 -> TileTransition.SOUTH_WEST;
            case NUMPAD2 -> TileTransition.SOUTH;
            case NUMPAD3 -> TileTransition.SOUTH_EAST;
            case NUMPAD4 -> TileTransition.WEST;
            case NUMPAD6 -> TileTransition.EAST;
            case NUMPAD7 -> TileTransition.NORTH_WEST;
            case NUMPAD8 -> TileTransition.NORTH;
            case NUMPAD9 -> TileTransition.NORTH_EAST;
            case DOWN -> event.isControlDown() ? TileTransition.SOUTH : null;
            case LEFT -> event.isControlDown() ? TileTransition.WEST : null;
            case RIGHT -> event.isControlDown() ? TileTransition.EAST : null;
            case UP -> event.isControlDown() ? TileTransition.NORTH : null;
            default -> null;
        };
        if (transition != null) {
            synchronized (updateLock) {
                Vector2D current = cursorPosition();
                Vector2D next = Vector2D.add(current, transition.getD());
                if (modelRoot.getWorld().boundsContain(next)) {
                    moveCursor(next);
                    TrackMoveProducer producer = actionRoot.getTrackMoveProducer();
                    if (producer != null && producer.getTrackBuilderMode() == BuildMode.BUILD_TRACK) {
                        producer.setBuildTrackStrategy((BuildTrackStrategy) modelRoot.getProperty(
                                ModelRootProperty.BUILD_TRACK_STRATEGY));
                        var status = producer.buildTrack(current, transition);
                        modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE,
                                status.isSuccess() ? null : status.getMessage());
                    }
                }
            }
            event.consume();
            return;
        }

        if (event.getCode() == KeyCode.C) {
            centerOnTile(cursorPosition());
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            synchronized (updateLock) {
                buildTrackController.hide();
                modelRoot.setProperty(ModelRootProperty.IGNORE_KEY_EVENTS, Boolean.FALSE);
            }
            event.consume();
        }
    }

    /**
     * Selects a map tile and starts a potential track drag.
     *
     * @param event JavaFX mouse event
     */
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        scrollPane.requestFocus();
        if (event.getButton() == MouseButton.PRIMARY) {
            Vector2D tile = mapPoint(event.getX(), event.getY());
            synchronized (updateLock) {
                dragStart = tile;
                moveCursor(tile);
            }
        } else if (event.getButton() == MouseButton.SECONDARY) {
            synchronized (updateLock) {
                buildTrackController.hide();
                modelRoot.setProperty(ModelRootProperty.IGNORE_KEY_EVENTS, Boolean.FALSE);
            }
            panning = true;
            panStartScreenX = event.getScreenX();
            panStartScreenY = event.getScreenY();
            panStartHorizontalValue = scrollPane.getHvalue();
            panStartVerticalValue = scrollPane.getVvalue();
        }
    }

    /**
     * Updates the cursor and preview while dragging a track route.
     *
     * @param event JavaFX mouse event
     */
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        if (event.isSecondaryButtonDown() && panning) {
            double maxX = Math.max(0, mapWidth - renderViewport.width());
            double maxY = Math.max(0, mapHeight - renderViewport.height());
            double deltaX = event.getScreenX() - panStartScreenX;
            double deltaY = event.getScreenY() - panStartScreenY;
            scrollPane.setHvalue(maxX == 0 ? 0
                    : clamp(panStartHorizontalValue - deltaX / maxX));
            scrollPane.setVvalue(maxY == 0 ? 0
                    : clamp(panStartVerticalValue - deltaY / maxY));
            event.consume();
            return;
        }
        if (event.isPrimaryButtonDown() && dragStart != null) {
            Vector2D destination = mapPoint(event.getX(), event.getY());
            synchronized (updateLock) {
                TrackMoveProducer producer = actionRoot.getTrackMoveProducer();
                if (producer != null && canPreviewTrack(producer.getTrackBuilderMode())
                        && !destination.equals(dragStart)) {
                    buildTrackController.setProposedTrack(destination, producer);
                }
            }
        }
    }

    /**
     * Commits a successful track drag using the existing track controller.
     *
     * @param event JavaFX mouse event
     */
    private void handleMouseReleased(javafx.scene.input.MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            panning = false;
        }
        if (event.getButton() == MouseButton.PRIMARY && dragStart != null) {
            synchronized (updateLock) {
                TrackMoveProducer producer = actionRoot.getTrackMoveProducer();
                var stationBuildModel = actionRoot.getStationBuildModel();
                if (producer != null && producer.getTrackBuilderMode() == BuildMode.BUILD_STATION
                        && stationBuildModel != null
                        && stationBuildModel.isStationTypeSelected()) {
                    Vector2D destination = mapPoint(event.getX(), event.getY());
                    stationBuildModel.buildStationAt(destination);
                } else if (buildTrackController.isBuilding()
                        && buildTrackController.isBuildTrackSuccessful()) {
                    Vector2D result = buildTrackController.updateWorld(actionRoot.getTrackMoveProducer());
                    modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, result);
                } else {
                    buildTrackController.hide();
                }
                dragStart = null;
            }
        }
    }

    /**
     * Converts mouse coordinates over the rendered viewport to map tile coords.
     *
     * @param x horizontal viewport position
     * @param y vertical viewport position
     * @return map tile coordinate
     */
    private Vector2D mapPoint(double x, double y) {
        return mapPointToTile(visibleX, visibleY, x, y, mainRenderer.scale());
    }

    /**
     * Converts viewport pixel coordinates to a world tile.
     *
     * @param visibleX horizontal map offset in pixels
     * @param visibleY vertical map offset in pixels
     * @param x horizontal pointer position in the viewport
     * @param y vertical pointer position in the viewport
     * @param tileSize map tile size in pixels
     * @return tile under the pointer
     */
    static Vector2D mapPointToTile(
            double visibleX, double visibleY, double x, double y, double tileSize) {
        if (!Double.isFinite(tileSize) || tileSize <= 0) {
            throw new IllegalArgumentException("Tile size must be positive and finite.");
        }
        return new Vector2D(
                (int) ((visibleX + x) / tileSize),
                (int) ((visibleY + y) / tileSize));
    }

    /**
     * Returns the current cursor position, using the map origin when unset.
     *
     * @return current cursor tile
     */
    private Vector2D cursorPosition() {
        Vector2D position = (Vector2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
        return position == null ? Vector2D.ZERO : position;
    }

    /**
     * Constrains normalized scroll values to the JavaFX ScrollPane range.
     *
     * @param value value to constrain
     * @return normalized value between zero and one
     */
    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    /**
     * Checks whether a track mode is supported by the path preview controller.
     *
     * @param mode current track mode
     * @return whether the mode supports drag preview
     */
    private static boolean canPreviewTrack(BuildMode mode) {
        return mode == BuildMode.BUILD_TRACK
                || mode == BuildMode.UPGRADE_TRACK
                || mode == BuildMode.REMOVE_TRACK;
    }

    /**
     * Holds pixels and viewport coordinates captured by the rendering worker.
     */
    private record RenderedImages(
            int x,
            int y,
            int width,
            int height,
            int[] mainPixels,
            int overviewWidth,
            int overviewHeight,
            int[] overviewPixels,
            double viewportX,
            double viewportY,
            double viewportWidth,
            double viewportHeight) {
    }

    /**
     * Captured ScrollPane values that can safely be read by the render worker.
     */
    private record RenderViewport(
            double width, double height, double horizontalValue, double verticalValue) {
    }
}
