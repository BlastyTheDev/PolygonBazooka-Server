package me.blasty.polygonbazookaserver.api.multiplayer;

import lombok.Getter;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.multiplayer.util.RNG;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Vector2;

import java.util.ArrayList;
import java.util.List;

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

    private long lastClear;

    private volatile boolean toppedOut;

    public Player() {
        board = new TileType[Const.Rows][Const.Cols];
        resetBoard();
    }

    public void resetBoard() {
        for (int row = 0; row < Const.Rows; row++)
            for (int col = 0; col < Const.Cols; col++)
                board[row][col] = TileType.Empty;
    }

    public synchronized void dropPiece(boolean origin, int destY) {
        if (destY < 0)
            toppedOut = true;
        else if (origin) {
            board[destY][xOrigin] = fallingBlockOrigin;
            fallingBlockOrigin = TileType.Empty;
        } else {
            board[destY][xOrbit] = fallingBlockOrbit;
            fallingBlockOrbit = TileType.Empty;
        }
    }

    public synchronized int getLowestEmptyCell(int col) {
        for (int row = Const.Rows - 1; row >= 0; row--)
            if (board[row][col] == TileType.Empty)
                return row;

        return -1;
    }

    public synchronized void hardDrop() {
        if (isClearing())
            return;

        // drop origin first if lower
        // they are flipped and i have no idea why but it works so..
        if (yOrigin <= yOrbit) {
            if (fallingBlockOrbit != TileType.Empty)
                dropPiece(false, getLowestEmptyCell(xOrbit));
            if (fallingBlockOrigin != TileType.Empty)
                dropPiece(true, getLowestEmptyCell(xOrigin));
        } else {
            if (fallingBlockOrigin != TileType.Empty)
                dropPiece(true, getLowestEmptyCell(xOrigin));
            if (fallingBlockOrbit != TileType.Empty)
                dropPiece(false, getLowestEmptyCell(xOrigin));
        }

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
        if (isClearing())
            return;

        if (xOrigin - 1 < 0 || xOrbit - 1 < 0)
            return;

        if (isCellToLeftNotEmpty())
            return;

        xOrigin--;
        xOrbit--;
    }

    public synchronized void moveRight() {
        if (isClearing())
            return;

        if (xOrigin + 1 > Const.Cols - 1 || xOrbit + 1 > Const.Cols - 1)
            return;

        if (isCellToRightNotEmpty())
            return;

        xOrigin++;
        xOrbit++;
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

    private synchronized boolean isClearing() {
        return System.currentTimeMillis() - lastClear < Const.ClearTime;
    }

    public synchronized void moveLeftFully() {
        while (xOrigin - 1 >= 0 && xOrbit - 1 >= 0 && !isCellToLeftNotEmpty() && !isClearing()) {
            xOrigin--;
            xOrbit--;
        }
    }

    public synchronized void moveRightFully() {
        while (xOrigin + 1 <= Const.Cols - 1 && xOrbit + 1 <= Const.Cols - 1 && !isCellToRightNotEmpty() && !isClearing()) {
            xOrigin++;
            xOrbit++;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public synchronized void clear() {
        List<Vector2> clearedTiles = new ArrayList<>();

        // clear horizontally
        for (int row = 0; row < Const.Rows; row++) {
            for (int col = 0; col < Const.Cols; col++) {
                if (board[row][col] == TileType.Empty || board[row][col] == TileType.Bonus ||
                        board[row][col] == TileType.Garbage)
                    continue;

                int matchLength = 1;
                int lastColourMatch = col;
                TileType current = board[row][col];

                for (int nextCol = col + 1; nextCol < Const.Cols; nextCol++) {
                    if (board[row][nextCol] == current || board[row][nextCol] == TileType.Bonus) {
                        matchLength++;
                        if (board[row][nextCol] == current)
                            lastColourMatch = nextCol;
                    } else
                        break;
                }

                if (board[row][col + matchLength - 1] == TileType.Bonus)
                    matchLength--;

                if (matchLength >= 3 && lastColourMatch > col + 1) {
                    for (int i = 0; i < matchLength; i++) {
                        if (col + i <= lastColourMatch)
                            clearedTiles.add(new Vector2(col + i, row));
                    }
                }
            }
        }

        // clear vertically
        for (int col = 0; col < Const.Cols; col++) {
            for (int row = 0; row < Const.Rows; row++) {
                if (board[row][col] == TileType.Empty || board[row][col] == TileType.Bonus ||
                        board[row][col] == TileType.Garbage)
                    continue;

                int matchLength = 1;
                int lastColourMatch = row;
                TileType current = board[row][col];

                for (int nextRow = row + 1; nextRow < Const.Rows; nextRow++) {
                    if (board[nextRow][col] == current || board[nextRow][col] == TileType.Bonus) {
                        matchLength++;
                        if (board[nextRow][col] == current)
                            lastColourMatch = nextRow;
                    } else
                        break;
                }

                if (board[row + matchLength - 1][col] == TileType.Bonus)
                    matchLength--;

                if (matchLength >= 3 && lastColourMatch > row + 1) {
                    for (int i = 0; i < matchLength; i++) {
                        if (row + i <= lastColourMatch)
                            clearedTiles.add(new Vector2(col, row + i));
                    }
                }
            }
        }

//        if (clearedTiles.Count != 0) {
//            clearingTiles = new ArrayList<>();
//            lastClear = System.currentTimeMillis();
//        }

        // clear tiles and adjacent garbage
        for (Vector2 tile : clearedTiles) {
            lastClear = System.currentTimeMillis();

//            clearingTiles.Add(tile);
            board[tile.Y][tile.X] = TileType.Empty;

            // if tile below is garbage
            if (tile.Y - 1 >= 0 && board[tile.Y - 1][tile.X] == TileType.Garbage)
                board[tile.Y - 1][tile.X] = TileType.Empty;

            // if tile above is garbage
            if (tile.Y + 1 < Const.Rows && board[tile.Y + 1][tile.X] == TileType.Garbage)
                board[tile.Y + 1][tile.X] = TileType.Empty;

            // if tile to the left is garbage
            if (tile.X - 1 >= 0 && board[tile.Y][tile.X - 1] == TileType.Garbage)
                board[tile.Y][tile.X - 1] = TileType.Empty;

            // if tile to the right is garbage
            if (tile.X + 1 < Const.Cols && board[tile.Y][tile.X + 1] == TileType.Garbage)
                board[tile.Y][tile.X + 1] = TileType.Empty;
        }
    }

    public synchronized void gravity() {
        for (int col = 0; col < Const.Cols; col++) {
            int bottomEmptyRow = Const.Rows - 1;

            for (int row = Const.Rows - 1; row >= 0; row--)
                if (board[row][col] != TileType.Empty)
                    board[bottomEmptyRow--][col] = board[row][col];

            for (int row = bottomEmptyRow; row >= 0; row--)
                board[row][col] = TileType.Empty;
        }
    }

}
