package philosophers;

import java.util.Random;

public enum Hand {
    LEFT, RIGHT;

    private static Random rand = new Random();
    public static Hand randomHand() {
        return values()[rand.nextInt(2)];
    }

    public static Hand otherHand(Hand hand){
        if (hand==LEFT) return RIGHT;
        if (hand==RIGHT) return LEFT;
        throw new IllegalArgumentException();
    }
}
