package me.blasty.polygonbazookaserver.api.multiplayer.util;

import me.blasty.polygonbazookaserver.api.multiplayer.TileType;

public class Const {

    public static final int Rows = 12;
    public static final int Cols = 7;

    public static final int FallingBlockSpawnX = 3;
    public static final int FallingOriginSpawnY = -2;
    public static final int FallingOrbitSpawnY = -3;

    public static final TileType[] QueueTileTypes = {
            TileType.Blue,
            TileType.Green,
            TileType.Red,
            TileType.Yellow,
            TileType.Bonus,
    };

    // Ranked Websocket Messages
    public static final String JoinQueue = "<JOINQUEUE>";
    public static final String LeaveQueue = "<LEAVEQUEUE>";

    // Multiplayer Websocket Messages
    public static final String ForfeitGame = "<FORFEIT>";
    public static final String ChatPrefix = "MESSAGE:";

}
