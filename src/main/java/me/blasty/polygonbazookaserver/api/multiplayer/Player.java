package me.blasty.polygonbazookaserver.api.multiplayer;

import lombok.Getter;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.multiplayer.util.RNG;

@Getter
public class Player {

    private final TileType[][] board;

    private TileType fallingBlockOrigin;
    private TileType fallingBlockOrbit;

    private TileType nextFallingBlockOrigin;
    private TileType nextFallingBlockOrbit;
    private TileType nextNextFallingBlockOrigin;
    private TileType nextNextFallingBlockOrbit;

    private int xOrigin;
    private int yOrigin;
    private int xOrbit;
    private int yOrbit;

    public Player() {
        board = new TileType[Const.Rows][Const.Cols];
        resetBoard();
    }

    public void resetBoard() {
        for (int row = 0; row < Const.Rows; row++)
            for (int col = 0; col < Const.Cols; col++)
                board[row][col] = TileType.Empty;
    }

    public void onHardDrop() {
        nextFallingBlock();
    }
    
    public void nextFallingBlock() {
        fallingBlockOrigin = nextFallingBlockOrigin;
        fallingBlockOrbit = nextFallingBlockOrbit;

        xOrigin = Const.FallingBlockSpawnX;
        yOrigin = Const.FallingOriginSpawnY;
        xOrbit = Const.FallingBlockSpawnX;
        yOrbit = Const.FallingOrbitSpawnY;

        nextFallingBlockOrigin = nextNextFallingBlockOrigin;
        nextFallingBlockOrbit = nextNextFallingBlockOrbit;

        nextNextFallingBlockOrigin = Const.QueueTileTypes[RNG.next(0, Const.QueueTileTypes.length)];
        nextNextFallingBlockOrbit = Const.QueueTileTypes[RNG.next(0, Const.QueueTileTypes.length)];
    }

}
