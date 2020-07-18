package philosophers;

import philosophers.chairs.Chair;

import java.util.Random;
import java.util.SortedSet;

public class Philosopher implements Runnable {
    private final Room room;
    private Hand preferredHand;
    private Chair chair;
    private int nrOfTimesEaten = 0;
    private Thread thread;
    private static Random random = new Random();

    public Philosopher(Room room) {
        this.room = room;
    }

    public static void waitRandom() {
        try {
            Thread.sleep(random.nextInt(500)+500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        room.logEvent("enters room", this);
        sitOnSeparateChair();
        while (nrOfTimesEaten < room.size()) {
            waitRandom();
            switch (random.nextInt(3)) {
                case 0 :
                    room.logEvent("decides to eat", this);
                    eat();
                    break;
                case 1:
                    room.logEvent("decides to sit on separate chair", this);
                    sitOnSeparateChair();
                    break;
                case 2:
                    room.logEvent("decides to move chairs", this);
                    sitOnChair();
                    break;
            }
        }
        standUpIfneeded();
        room.logEvent("leaves room", this);
    }
    private void sitOnSeparateChair() {
        standUpIfneeded();
        waitRandom();
        room.getSeparateChair().sitOn(this);
        try {
            room.logEvent("sits on separate chair", this);
            waitRandom();
            setHandPreference();
            waitRandom();
            // we would want to log just before we stand up,
            // but unlocking locked resource takes preference in the finally section
            room.logEvent("leaves separate chair", this);
        } finally {
            room.getSeparateChair().standUp();
        }
        sitOnChair();
    }

    private void sitOnChair() {
        if (room.getPhilosophersSitingAtTable() < room.size()) {
            int i = (room.chairIndex(chair) + 1) % room.size();
            standUpIfneeded();
            waitRandom();
            // Philosopher goes around the table and tries to sit on each chair until they find an empty one.
            // This lock isn't strictly necessary here as
            //      - existence of an empty chair is guaranteed (our philosopher is standing)
            //      - chairs are protected from multiple occupancy by only being occupied by the trySitOn method
            //      - syncroneous movement is limited by low number and high waiting time of other philosophers,
            //          so in a hypothetical worst case scenario, where only one chair is free,
            //          and it is always occupied by someone else just before we arrive,
            //          we'll still find an empty chair in <= n rounds, after all other philosopers have moved
            //          to occupy a chair before us and have started waiting before their next decision
            // Only a much higher number of philosophers or a close to zero waiting time would make this lock
            //  necessary, to avoid a philosopher continously running around the table and not finding an empty chair
            room.lockMoveChair();
            try {
                while (!room.getChair(i).trySitOn(this)) {
                    i = ++i % room.size();
                }
                room.logEvent("sits on chair nr "+ room.chairIndex(chair), this);
            } finally {
                room.unlockMoveChair();
            }
        } else {
            room.logEvent("found all chairs occupied", this);
        }
    }

    private void eat() {
        waitRandom();
        synchronized (chair.getFork(preferredHand)) {
            waitRandom();
            synchronized (chair.getFork(Hand.otherHand(preferredHand))) {
                waitRandom();
                nrOfTimesEaten++;
                room.logMeal(this);
            }
        }
    }

    private void standUpIfneeded() {
        if (chair!=null){
            waitRandom();
            room.logEvent("leaves chair nr "+ room.chairIndex(chair), this);
            chair.standUp();
        }
    }

    private void setHandPreference() {
        preferredHand=null;
        // get preferences of other philosophers
        // no need for a lock here, as philosophers only change preference when siting on the separate chair,
        // which is occupied by our philosopher now
        SortedSet<Hand> preferences = room.philosopherPreferences();
        // if only one preference exists, we choose the other, otherwise we choose randomly
        if (preferences.size() == 1) { preferredHand = Hand.otherHand(preferences.first()); }
            else { preferredHand = Hand.randomHand(); }
        room.logEvent("prefers "+preferredHand.toString().toLowerCase()+" hand", this);
    }

    public String name() { return Thread.currentThread().getName(); }

    Hand getPreferredHand() { return preferredHand; }

    Thread getThread() { return thread; }

    // This method is called by the Chair object when succesfully siting down/standing up to/from it
    public void setChair(Chair chair) {
        int i = 0;
        if (room.chairIndex(chair)>=0) i++ ;
        if (room.chairIndex(this.chair)>=0) i-- ;
        if (i!=0) room.addPhilosophersSitingAtTable(i);
        this.chair = chair;
    }
}
