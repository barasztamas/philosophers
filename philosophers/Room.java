package philosophers;

import philosophers.chairs.Chair;
import philosophers.chairs.SeparateChair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

// the room class provides chairs, forks and communication between philosophers
// it also provides shared resources as random objact and logger, and manages threads
public class Room {
    private List<Chair> chairs = new ArrayList<>();
    private List<Fork> forks = new ArrayList<>();
    private SeparateChair separateChair = new SeparateChair();
    private List<Philosopher> philosophers = new ArrayList<>();
    private AtomicInteger philosophersSitingAtTable = new AtomicInteger(0);
    private ReentrantLock moveChairLock = new ReentrantLock(true);
    private long startTime;
    private List<String> meals = Collections.synchronizedList(new ArrayList<>());
    private Logger logger;

    public static void main(String[] args) {
        new Room(args.length==0 ? 5 : Integer.parseInt(args[0]));
    }

    public Room(Integer n) {
        this.startTime = System.currentTimeMillis();
        try {
            logger = Logger.getLogger("philosophers");
            FileHandler fh = new FileHandler("./log.txt");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < n; i++) {
            forks.add(new Fork());
        }
        for (int i = 0; i < n; i++) {
            chairs.add(new Chair(forks.get(i), forks.get((i+1)%n)));
        }
        for (int i = 0; i < n; i++) {
            Philosopher philosopher = new Philosopher(this);
            philosophers.add(philosopher);
            new Thread(philosopher, "philosopher "+i).start();
        }
        for (Philosopher philosopher : philosophers) {
            try {
                philosopher.getThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All philosophers left, main process returning");
        Philosopher.waitRandom();
        logMessage("Main process listing all meals");
        for (String meal : meals) {
            System.out.println(meal);
        }
    }

    //logging tools
    private void logMessage(String message) { logger.info(message); }

    void logEvent(String event, Philosopher philosopher) { logMessage(philosopher.name()+" "+event); }

    void logMeal(Philosopher philosopher){
        meals.add(philosopher.name()+" ate meal at "+(System.currentTimeMillis()- startTime));
        logEvent("eats meal", philosopher);
    }

    // getters (and setters)

    public SortedSet<Hand> philosopherPreferences() {
        SortedSet<Hand> preferences = new TreeSet<>();
        for (Philosopher philosopher : philosophers) {
            if (philosopher.getPreferredHand() != null) {
                preferences.add(philosopher.getPreferredHand());
            }
        }
        return preferences;
    }

    public int size() { return chairs.size(); }

    Chair getChair(int i) { return chairs.get(i); }

    int chairIndex(Chair chair) { return chairs.indexOf(chair); }

    SeparateChair getSeparateChair() { return separateChair; }

    void lockMoveChair() { moveChairLock.lock(); }
    void unlockMoveChair() { moveChairLock.unlock(); }

    public int getPhilosophersSitingAtTable() { return philosophersSitingAtTable.get(); }
    public void addPhilosophersSitingAtTable(int philosophersToAdd) { this.philosophersSitingAtTable.addAndGet(philosophersToAdd); }

}
