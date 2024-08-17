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

    public synchronized void hardDrop() {
        nextFallingBlock();
    }

    public synchronized void nextFallingBlock() {
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

    public synchronized void moveLeft() {
    }

    public synchronized void moveRight() {
    }

    private synchronized boolean isCellToLeftNotEmpty() {
        if (xOrigin > 0 && xOrbit > 0) {
            if (yOrigin < 0 && yOrbit < 0)
                return false;

            if (yOrigin >= 0)
                return board[yOrigin][xOrigin - 1] != TileType.Empty;

            return board[yOrbit][xOrbit - 1] != TileType.Empty;
        }

        return true;
    }

    private synchronized boolean isCellToRightNotEmpty() {
        if (xOrigin < Const.Cols - 1 && xOrbit < Const.Cols - 1) {
            if (yOrigin < 0 && yOrbit < 0)
                return false;

            if (yOrigin >= 0)
                return board[yOrigin][xOrigin + 1] != TileType.Empty;

            return board[yOrbit][xOrbit + 1] != TileType.Empty;
        }

        return true;
    }

    private synchronized boolean isSideways() {
        return yOrigin == yOrbit;
    }

    private synchronized void rotate(int direction) {
        if (isCellToLeftNotEmpty() && isCellToRightNotEmpty() && !isSideways())
            return;

        // origin above orbit
        if (yOrigin > yOrbit) {
            if (isCellToLeftNotEmpty() && direction == Const.Ccw)
                moveRight();

            if (isCellToRightNotEmpty() && direction == Const.Cw)
                moveLeft();

            xOrbit = xOrigin - direction;
            yOrbit = yOrigin;
        }
        // origin below orbit
        else if (yOrigin < yOrbit) {
            if (isCellToLeftNotEmpty() && direction == Const.Cw)
                moveRight();

            if (isCellToRightNotEmpty() && direction == Const.Ccw)
                moveLeft();

            xOrbit = xOrigin + direction;
            yOrbit = yOrigin;
        }
        // origin left of orbit
        else if (xOrigin < xOrbit) {
            xOrbit = xOrigin;
            yOrbit = yOrigin - direction;
        }
        // origin right of orbit
        else if (xOrigin > xOrbit) {
            xOrbit = xOrigin;
            yOrbit = yOrigin + direction;
        }
    }

    private synchronized void moveUp() {
        yOrigin--;
        yOrbit--;
    }

    public synchronized void rotateCw() {
        if (isSideways() && xOrigin < xOrbit && yOrigin == Const.Rows - 1)
            moveUp();

        rotate(Const.Cw);
    }

    public synchronized void rotateCcw() {
        if (isSideways() && xOrbit < xOrigin && yOrigin == Const.Rows - 1)
            moveUp();

        rotate(Const.Ccw);
    }

    public synchronized void flip() {
        if (isSideways()) {
            // orbit left of origin (to move to right)
            if (xOrigin > xOrbit) {
                if (isCellToLeftNotEmpty() && isCellToRightNotEmpty()) {
                    int temp = xOrigin;
                    xOrigin = xOrbit;
                    xOrbit = temp;
                } else if (isCellToRightNotEmpty())
                    moveLeft();

                xOrbit = xOrigin + 1;
            }
            // orbit right of origin (to move to left)
            else if (xOrigin < xOrbit) {
                if (isCellToLeftNotEmpty() && isCellToRightNotEmpty()) {
                    int temp = xOrigin;
                    xOrigin = xOrbit;
                    xOrbit = temp;
                } else if (isCellToLeftNotEmpty())
                    moveRight();

                xOrbit = xOrigin - 1;
            }
        } else {
            // for some reason these are flipped and i have no idea why but it works
            // orbit above origin
            if (yOrbit > yOrigin)
                yOrbit = yOrigin - 1;
                // orbit below origin
            else {
                boolean kick = yOrigin >= Const.Rows - 1;

                if (yOrigin >= 0 && !kick) {
                    if (board[yOrigin + 1][xOrigin] != TileType.Empty)
                        kick = true;
                }

                if (kick) {
                    yOrigin--;
                    yOrbit--;
                }

                yOrbit = yOrigin + 1;
            }
        }
    }

    public synchronized void moveFullyLeft() {
    }

    public synchronized void moveFullyRight() {
    }
    
    public synchronized void clear() {
    }
    
    public synchronized void gravity() {
    }

}
