package me.blasty.polygonbazookaserver.api.multiplayer.util;

import java.util.Random;

public class RNG {
    
    private static final Random random = new Random();
    
    public static int next() {
        return random.nextInt();
    }

    public static int next(int maxValue) {
        return random.nextInt(maxValue);
    }

    public static int next(int minValue, int maxValue) {
        return random.nextInt(minValue, maxValue);
    }
    
}
