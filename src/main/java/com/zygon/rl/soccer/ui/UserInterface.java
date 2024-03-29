/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.SoccerTile;
import com.zygon.rl.soccer.core.pitch.Pitch;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.game.Game;
import org.hexworks.zircon.api.CP437TilesetResources;
import org.hexworks.zircon.api.ColorThemes;
import org.hexworks.zircon.api.Components;
import org.hexworks.zircon.api.Functions;
import org.hexworks.zircon.api.SwingApplications;
import org.hexworks.zircon.api.application.AppConfig;
import org.hexworks.zircon.api.color.ANSITileColor;
import org.hexworks.zircon.api.color.TileColor;
import org.hexworks.zircon.api.component.Button;
import org.hexworks.zircon.api.component.ColorTheme;
import org.hexworks.zircon.api.component.Component;
import org.hexworks.zircon.api.component.Fragment;
import org.hexworks.zircon.api.component.HBox;
import org.hexworks.zircon.api.component.TextArea;
import org.hexworks.zircon.api.component.VBox;
import org.hexworks.zircon.api.data.Position;
import org.hexworks.zircon.api.data.Size;
import org.hexworks.zircon.api.data.Tile;
import org.hexworks.zircon.api.graphics.BoxType;
import org.hexworks.zircon.api.graphics.Layer;
import org.hexworks.zircon.api.grid.TileGrid;
import org.hexworks.zircon.api.uievent.KeyCode;
import org.hexworks.zircon.api.uievent.KeyboardEvent;
import org.hexworks.zircon.api.uievent.KeyboardEventType;
import org.hexworks.zircon.api.uievent.MouseEvent;
import org.hexworks.zircon.api.uievent.MouseEventType;
import org.hexworks.zircon.api.uievent.UIEventPhase;
import org.hexworks.zircon.api.uievent.UIEventResponse;
import org.hexworks.zircon.api.view.base.BaseView;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class UserInterface {

    private static final int GAME_SCREEN_HEIGHT = 10;
    private static final int PITCH_LAYER_Y_OFFSET = 20;

    private final Game game;

    public UserInterface(Game game) {
        this.game = game;
    }

    private static final class Sidebar implements Fragment {

        private final VBox root;

        public Sidebar(List<Component> components, Size size, Position position,
                String title) {
            this.root = Components.vbox()
                    .withPreferredSize(size)
                    .withPosition(position)
                    .withDecorations(
                            org.hexworks.zircon.api.ComponentDecorations.box(BoxType.DOUBLE, title))
                    .build();

            components.forEach(root::addComponent);
        }

        @Override
        public Component getRoot() {
            return root;
        }
    }

    /**
     * UI for the main game screen.
     */
    private static final class GameView extends BaseView {

        private static final Tile GRASS_TILE = Tile.newBuilder()
                .withBackgroundColor(ANSITileColor.GREEN)
                .withForegroundColor(ANSITileColor.WHITE)
                .buildCharacterTile();
        private static final Tile PITCH_TILE = Tile.newBuilder()
                .withBackgroundColor(ANSITileColor.GREEN)
                .withForegroundColor(ANSITileColor.WHITE)
                .withCharacter('.')
                .buildCharacterTile();
        private static final Tile GOAL_TILE = createPlayerTile('X', ANSITileColor.BRIGHT_GREEN);
        private static final Tile PATH_TILE = createPlayerTile('*', ANSITileColor.BRIGHT_MAGENTA);
        private static final Tile BALL_TILE = createPlayerTile('o', ANSITileColor.BRIGHT_YELLOW);
        private static final Tile LINE_TILE = createPlayerTile('+', ANSITileColor.BRIGHT_WHITE);

        private final UIEventProcessor processor;
        private final TileGrid tileGrid;
        private final Game game;

        private TextArea debugTextArea = null;
        private TextArea scoreTextArea = null;
        private TextArea playerInfo = null;
        private Layer pitchLayer = null;

        public GameView(TileGrid tileGrid, ColorTheme colorTheme, Game game) {
            super(tileGrid, colorTheme);

            this.tileGrid = tileGrid;
            this.game = game;
            this.processor = new UIEventProcessor(this.game);
        }

        @Override
        public void onDock() {
            super.onDock();

            // future: handle view change
            if (scoreTextArea != null) {
                throw new IllegalStateException("Not expecting game view to re-dock");
            }

            // Top info banner
            HBox gameScreenHeader = Components.hbox()
                    .withPreferredSize(tileGrid.getSize().getWidth(), GAME_SCREEN_HEIGHT)
                    .withDecorations(org.hexworks.zircon.api.ComponentDecorations.box(BoxType.DOUBLE, "Info"))
                    .build();

            debugTextArea = Components.textArea()
                    .withPosition(gameScreenHeader.getPosition().getX() + 10, 2)
                    .withPreferredSize(10, 2)
                    .build();
            gameScreenHeader.addComponent(debugTextArea);

            scoreTextArea = Components.textArea()
                    .withPosition(debugTextArea.getPosition().getX() + debugTextArea.getWidth(), 2)
                    .withPreferredSize(10, 2)
                    .build();

            gameScreenHeader.addComponent(scoreTextArea);
            getScreen().addComponent(gameScreenHeader);

            // Add the pitch
            pitchLayer = Layer.newBuilder()
                    .withSize(Pitch.FIELD_WIDTH, Pitch.FIELD_HEIGHT)
                    .withOffset(gameScreenHeader.getPosition().withRelativeY(gameScreenHeader.getHeight()))
                    .build();

            getScreen().addLayer(pitchLayer);

            playerInfo = Components.textArea()
                    .withText("More info")
                    .withPreferredSize(20, 20)
                    .withPosition(pitchLayer.getSize().getWidth(),
                            gameScreenHeader.getSize().getHeight())
                    .withDecorations(
                            org.hexworks.zircon.api.ComponentDecorations.box(BoxType.DOUBLE, "Player"))
                    .build();

            getScreen().addComponent(playerInfo);
            playerInfo.setHidden(true);

            update();

            // Using queue "add" because it WILL throw exceptions if full, to find dev bugs
            List<KeyboardEventType> keyboardEvents = List.of(KeyboardEventType.KEY_PRESSED,
                    KeyboardEventType.KEY_RELEASED, KeyboardEventType.KEY_TYPED);

            for (KeyboardEventType keyboardEventType : keyboardEvents) {
                tileGrid.processKeyboardEvents(keyboardEventType,
                        Functions.fromBiConsumer((event, phase) -> {
                            processor.add(UIEvent.create(event));
                            update();
                        }));
            }

            // Register mouse events
            List<MouseEventType> mouseEvents = List.of(MouseEventType.MOUSE_CLICKED,
                    MouseEventType.MOUSE_DRAGGED, MouseEventType.MOUSE_RELEASED, MouseEventType.MOUSE_PRESSED);

            for (MouseEventType mouseEvent : mouseEvents) {
                tileGrid.processMouseEvents(mouseEvent,
                        Functions.fromBiConsumer((event, phase) -> {
                            processor.add(UIEvent.create(event));
                            update();
                        }));
            }

            tileGrid.processMouseEvents(MouseEventType.MOUSE_MOVED,
                    Functions.fromBiConsumer(handleMouseMoved(debugTextArea)));

            Thread t = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        processor.add(UIEvent.create(new KeyboardEvent(
                                KeyboardEventType.KEY_PRESSED, "space", KeyCode.SPACE,
                                false, false, false, false)));
                        update();

                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            };
            t.setDaemon(false);
            t.start();
        }

        // synchronized is I *think* important? blocking the pitch rendering could be slow..
        public synchronized void update() {
            drawPitch();
            updateScore();
        }

        private void updateScore() {
            String score = game.getScores().entrySet().stream()
                    .map(e -> e.getValue().getGoals() + " - " + e.getKey().getName())
                    .collect(Collectors.joining("\n"));
            scoreTextArea.setText(score);
        }

        private void drawPitch() {
            Map<Location, Set<SoccerTile>> pitchUpdates = game.getPitchUpdates();

            pitchUpdates.forEach((loc, items) -> {
                final Position pos = fromPitchToLayer(loc);

                Set<SoccerTile> sortedItems = items.stream()
                        .sorted((t1, t2) -> t1.equals(SoccerTile.BALL) ? 1 : (t2.equals(SoccerTile.BALL) ? -1 : 0))
                        .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));

                for (SoccerTile tileItem : sortedItems) {
                    switch (tileItem) {
                        case BALL:
                            pitchLayer.draw(BALL_TILE, pos);
                            break;
                        case GRASS:
                            pitchLayer.draw(GRASS_TILE, pos);
                            break;
                        case GOAL:
                            pitchLayer.draw(GOAL_TILE, pos);
                            break;
                        case LINE:
                            pitchLayer.draw(LINE_TILE, pos);
                            break;
                        case PITCH:
                            pitchLayer.draw(PITCH_TILE, pos);
                            break;
                        case PLAYER_HIGHLIGHT:
                            pitchLayer.draw(PATH_TILE, pos);
                            break;
                        case PLAYER_TRACK:
                            pitchLayer.draw(PATH_TILE, pos);
                            break;
                        case PLAYER:
                            PlayerEntity player = game.getPlayer(loc);
                            Tile playerTile = createPlayerTile('P', convert(player.getPlayer().getTeam().getColor()));
                            pitchLayer.draw(playerTile, pos);
                            break;
                        default:
                            System.err.println("Unknown tile " + tileItem.name());
                            break;
                    }
                }
            });
        }

        private static TileColor convert(Color color) {
            // this is a bad conversion
            return TileColor.create(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }

        private static Tile createPlayerTile(char c, TileColor foreground) {
            return Tile.newBuilder()
                    .withBackgroundColor(ANSITileColor.GREEN)
                    .withForegroundColor(foreground)
                    .withCharacter(c)
                    .buildCharacterTile();
        }
    }

    /**
     * UI for the title screen.
     */
    private static final class TitleView extends BaseView {

        private final TileGrid tileGrid;
        private final ColorTheme colorTheme;
        private final Game game;

        public TitleView(TileGrid tileGrid, ColorTheme colorTheme, Game gameUI) {
            super(tileGrid, colorTheme);

            this.tileGrid = tileGrid;
            this.colorTheme = colorTheme;
            this.game = gameUI;
        }

        @Override
        public void onDock() {
            super.onDock();

            Button startButton = Components.button()
                    .withText("NEW GAME")
                    .withTileset(CP437TilesetResources.rexPaint20x20())
                    .build();
            startButton.handleMouseEvents(MouseEventType.MOUSE_CLICKED, (p1, p2) -> {
                game.start();
                replaceWith(new GameView(tileGrid, colorTheme, game));
                return UIEventResponse.processed();
            });
            Button quitButton = Components.button()
                    .withText("QUIT")
                    .withTileset(CP437TilesetResources.rexPaint20x20())
                    .build();
            quitButton.handleMouseEvents(MouseEventType.MOUSE_CLICKED, (p1, p2) -> {
                System.exit(0);
                return UIEventResponse.processed();
            });

            getScreen()
                    .addFragment(new Sidebar(List.of(startButton, quitButton),
                            tileGrid.getSize(), Position.create(0, 0), "SoccerRL"));
        }
    }

    public void start() {
        // a TileGrid represents a 2D grid composed of Tiles
        TileGrid tileGrid = SwingApplications.startTileGrid(
                AppConfig.newBuilder()
                        // The number of tiles horizontally, and vertically
                        .withSize(Size.create(80, 90))
                        // You can choose from a wide array of CP437, True Type or Graphical tilesets
                        // which are built into Zircon
                        .withDefaultTileset(CP437TilesetResources.rexPaint20x20())
                        .build());

        TitleView titleView = new TitleView(tileGrid, ColorThemes.forest(), game);

        titleView.dock();
    }

    // layer is already offset, so pass-through conversion
    static Position fromPitchToLayer(Location location) {
        return Position.create(location.getX(), location.getY());
    }

    // Need to subtract the score screen vertical
    static Location fromTileGridToPitch(Position position) {
        Location loc = Location.create(position.getX(), position.getY() - GAME_SCREEN_HEIGHT);

        if (loc.getX() < 0 || loc.getX() >= Pitch.FIELD_WIDTH || loc.getY() < 0 || loc.getY() >= Pitch.FIELD_HEIGHT) {
            return null;
        }

        return loc;
    }

    private static BiConsumer<MouseEvent, UIEventPhase> handleMouseMoved(
            TextArea scoreTextArea) {
        return (MouseEvent event, UIEventPhase u) -> {
            try {
                int gameX = event.getPosition().getX();
                int gameY = event.getPosition().getY();
                scoreTextArea.setText(gameX + "/" + gameY + "\n" + (gameX) + "/" + (gameY - PITCH_LAYER_Y_OFFSET));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        };
    }
}
