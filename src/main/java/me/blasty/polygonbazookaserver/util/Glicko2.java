package me.blasty.polygonbazookaserver.util;

public class Glicko2 {

    public static final double SYSTEM_CONSTANT = 0.5;

    public static final double UNRANKED_VOLATILITY = 0.06;
    public static final double UNRANKED_GLICKO = 1500;
    public static final double UNRANKED_RD = 350;

    public static final double MAX_RD = 350;

    public static final double MAX_RATING = 25000;

    public static double glickoToGlicko2(double glicko) {
        return (glicko - 1500) / 173.7178;
    }

    public static double rdToRD2(double rd) {
        return rd / 173.7178;
    }

    public static double glicko2toGlicko(double glicko2) {
        return 173.7178 * glicko2 + 1500;
    }

    public static double rd2toRD(double rd2) {
        return 173.7178 * rd2;
    }

    public static double g(double rd) {
        return 1 / Math.sqrt(1 + 3 * Math.pow(rd, 2) / Math.pow(Math.PI, 2));
    }

    public static double E(double glicko2, double glicko2j, double rdj) {
        return 1 / (1 + Math.exp(-g(rdj) * (glicko2 - glicko2j)));
    }

    public static double calculateVariance(double glicko2, double[] enemyGlicko2, double[] enemyRd2) {
        int m = enemyGlicko2.length;
        double sum = 0;

        for (int i = 0; i < m; i++)
            sum += Math.pow(g(enemyRd2[i]), 2) * E(glicko2, enemyGlicko2[i], enemyRd2[i]) * (1 - E(glicko2, enemyGlicko2[i], enemyRd2[i]));

        return Math.pow(sum, -1);
    }

    public static double calculateImprovement(double variance, double glicko2, double[] enemyGlicko2, double[] enemyRd2, double[] results) {
        int m = enemyGlicko2.length;
        double sum = 0;

        for (int i = 0; i < m; i++)
            sum += g(enemyRd2[i]) * (results[i] - E(glicko2, enemyGlicko2[i], enemyRd2[i]));

        return variance * sum;
    }

    public static double f(double x, double variance, double improvement, double rd2, double a) {
        return (Math.exp(x) * (Math.pow(improvement, 2) - Math.pow(rd2, 2) - variance - Math.exp(x)) / 2
                * Math.pow(Math.pow(rd2, 2) + variance + Math.exp(x), 2)) - ((x - a) / Math.pow(SYSTEM_CONSTANT, 2));
    }

    public static double calculateNewVolatility(double volatility, double variance, double improvement, double rd2) {
        double a = Math.log(Math.pow(volatility, 2));
        double b;
        double c;
        double k;

        if (Math.pow(improvement, 2) > Math.pow(rd2, 2) + variance)
            b = Math.log(Math.pow(improvement, 2) - Math.pow(rd2, 2) - variance);
        else {
            k = 1;

            while (f(a - k * SYSTEM_CONSTANT, variance, improvement, rd2, a) < 0)
                k++;

            b = a - k * SYSTEM_CONSTANT;
        }

        double Fa = f(a, variance, improvement, rd2, a);
        double Fb = f(b, variance, improvement, rd2, a);
        double Fc;

        while (Math.abs(b - a) > 0.000001) {
            c = a + (a - b) * Fa / (Fb - Fa);
            Fc = f(c, variance, improvement, rd2, a);
            if (Fc * Fb <= 0) {
                a = b;
                Fa = Fb;
            } else Fa = Fa / 2;
            b = c;
            Fb = Fc;
        }

        return Math.exp(a / 2);
    }

    public static double updateRDtoPreRating(double rd2, double newVolatility) {
        return Math.sqrt(Math.pow(rd2, 2) + Math.pow(newVolatility, 2));
    }

    public static double updateRDtoNew(double prRd2, double variance) {
        return 1 / Math.sqrt((1 / Math.pow(prRd2, 2)) + (1 / variance));
    }

    public static double updateGlickoToNew(double newRd2, double glicko2, double enemyGlicko2, double enemyRd2, double result) {
        return glicko2 + Math.pow(newRd2, 2) * (g(enemyRd2) * (result - E(glicko2, enemyGlicko2, enemyRd2)));
    }

    public static double calculateGlixareRating(double glicko, double rd) {
        double glixare = 10000 / (1 + Math.pow(10, (((1500 - glicko) * Math.PI / Math.sqrt(3 * Math.pow(Math.log(10), 2)
                * Math.pow(rd, 2) + 2500 * (64 * Math.pow(Math.PI, 2) + 147 * Math.pow(Math.log(10), 2))))))) / 10000;
        return glixare * MAX_RATING;
    }

}
