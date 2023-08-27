/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.game.Game;
import org.hexworks.zircon.api.CP437TilesetResources;
import org.hexworks.zircon.api.Functions;
import org.hexworks.zircon.api.SwingApplications;
import org.hexworks.zircon.api.application.AppConfig;
import org.hexworks.zircon.api.color.ANSITileColor;
import org.hexworks.zircon.api.color.TileColor;
import org.hexworks.zircon.api.data.Position;
import org.hexworks.zircon.api.data.Size;
import org.hexworks.zircon.api.data.Tile;
import org.hexworks.zircon.api.graphics.Layer;
import org.hexworks.zircon.api.graphics.LayerHandle;
import org.hexworks.zircon.api.grid.TileGrid;
import org.hexworks.zircon.api.uievent.KeyboardEventType;

/**
 *
 * @author zygon
 */
public class Example {

    private final Game game;

    public Example(Game game) {
        this.game = game;
    }

    private static final Tile PLAYER_TILE = Tile.newBuilder()
            .withBackgroundColor(ANSITileColor.BLACK)
            .withForegroundColor(ANSITileColor.WHITE)
            .withCharacter('@')
            .buildCharacterTile();

    private static Tile createPlayerTile(char c) {
        return Tile.newBuilder()
                .withBackgroundColor(ANSITileColor.BLACK)
                .withForegroundColor(ANSITileColor.WHITE)
                .withCharacter(c)
                .buildCharacterTile();
    }

    public static void main(String[] args) {
        TileGrid tileGrid = SwingApplications.startTileGrid(
                AppConfig.newBuilder()
                        .withSize(100, 60)
                        .withDefaultTileset(CP437TilesetResources.yobbo20x20())
                        .build());

        Layer layer = Layer.newBuilder()
                .withSize(Size.one())
                .withOffset(Position.create(tileGrid.getWidth() / 2, tileGrid.getHeight() / 2))
                .withFiller(createPlayerTile('@'))
                .build();

        Layer layer2 = Layer.newBuilder()
                .withSize(Size.one())
                .withOffset(Position.create(tileGrid.getWidth() / 2, tileGrid.getHeight() / 2))
                .withFiller(Tile.newBuilder()
                        .withForegroundColor(ANSITileColor.RED)
                        .withBackgroundColor(TileColor.transparent())
                        .withCharacter('O')
                        .build())
                .build();

        LayerHandle player = tileGrid.addLayer(layer);
        LayerHandle player2 = tileGrid.addLayer(layer2);

        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED, Functions.fromBiConsumer((event, phase) -> {
            switch (event.getCode()) {
                case UP:
                    player.moveUpBy(1);
                    player2.moveUpBy(1);
                    break;
                case DOWN:
                    player.moveDownBy(1);
                    player2.moveDownBy(1);
                    break;
                case LEFT:
                    player.moveLeftBy(1);
                    player2.moveLeftBy(1);
                    break;
                case RIGHT:
                    player.moveRightBy(1);
                    player2.moveRightBy(1);
                    break;
            }
        }));
    }
}
