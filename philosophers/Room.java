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

// the room class provides chairs, forks, and all shared resources as random objact and logger
// it also manages threads
// philosophers are a subclass of Room to avoid the need for boilerplate getters
public class Room {
    private Integer n;
    private List<Chair> chairs = new ArrayList<>();
    private List<Fork> forks = new ArrayList<>();
    private SeparateChair separateChair = new SeparateChair();
    private List<Philosopher> philosophers = new ArrayList<>();
    private List<Thread> threads = new ArrayList<>();
    private AtomicInteger philosophersSitingAtTable = new AtomicInteger(0);
    private ReentrantLock moveChairLock = new ReentrantLock(true);
    private long startTime;
    private List<String> meals = Collections.synchronizedList(new ArrayList<>());
    private static Random random = new Random();
    private static Logger logger = Logger.getLogger("philosophers");

    public static void main(String[] args) {
        new Room(args.length==0 ? 5 : Integer.parseInt(args[0]));
    }

    public Room(Integer n) {
        this.n = n;
        this.startTime = System.currentTimeMillis();
        try {
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
            Philosopher philosopher = new Philosopher();
            philosophers.add(philosopher);
            Thread thread = new Thread(philosopher, "philosopher "+i);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All philosophers left, main process returning");
        waitRandom();
        logMessage("Main process listing all meals");
        for (String meal : meals) {
            System.out.println(meal);
        }
    }
    public void logMessage(String message) {
        logger.info(message);
    }

    public void waitRandom() {
        try {
            Thread.sleep(random.nextInt(500)+500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    

    public class Philosopher implements Runnable {
        private Hand preferredHand;
        private Chair chair;
        private int nrOfTimesEaten = 0;

        @Override
        public void run() {
            logEvent("enters room");
            sitOnSeparateChair();
            while (nrOfTimesEaten < n) {
                waitRandom();
                switch (random.nextInt(3)) {
                    case 0 :
                        logEvent("decides to eat");
                        eat();
                        break;
                    case 1:
                        logEvent("decides to sit on separate chair");
                        sitOnSeparateChair();
                        break;
                    case 2:
                        logEvent("decides to move chairs");
                        sitOnChair();
                        break;
                }
            }
            standUpIfneeded();
            logEvent("leaves room");
        }
        private void sitOnSeparateChair() {
            standUpIfneeded();
            waitRandom();
            separateChair.sitOn(this);
            try {
                logEvent("sits on separate chair");
                waitRandom();
                setHandPreference();
                waitRandom();
                // we would want to log in the finally section before we stand up,
                // but unlocking locked resource takes preference there
                logEvent("left separate chair");
            } finally {
                separateChair.standUpFrom(this);
            }
            sitOnChair();
        }

        private void sitOnChair() {
            if (philosophersSitingAtTable.get() < n) {
                int i = (chairs.indexOf(chair) + 1) % n;
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
                moveChairLock.lock();
                try {
                    while (!chairs.get(i).trySitOn(this)) {
                        i = ++i % n;
                    }
                    logEvent("sits on chair nr "+chairs.indexOf(chair));
                } finally {
                    moveChairLock.unlock();
                }
            }
        }

        private void eat() {
            waitRandom();
            synchronized (chair.getFork(preferredHand)) {
                waitRandom();
                synchronized (chair.getFork(Hand.otherHand(preferredHand))) {
                    waitRandom();
                    nrOfTimesEaten++;
                    logMeal();
                }
            }
        }

        private void standUpIfneeded() {
            if (chair!=null){
                waitRandom();
                logEvent("leaves chair nr "+chairs.indexOf(chair));
                chair.standUpFrom(this);
            }
        }

        private void setHandPreference() {
            preferredHand=null;
            // get preferences of other philosophers
            // no need for a lock here, as philosophers only change preference when siting on the separate chair,
            // which is occupied by our philosopher now
            SortedSet<Hand> preferences = new TreeSet<>();
            for (Philosopher philosopher : philosophers) {
                preferences.add(philosopher.preferredHand);
            }
            preferences.remove(null);
            // if only one preference exists, we choose the other, otherwise we choose randomly
            if (preferences.size() == 1) { preferredHand = Hand.otherHand(preferences.first()); }
                else { preferredHand = Hand.randomHand(); }
            logEvent("prefers "+preferredHand.toString().toLowerCase()+" hand");
        }

        private String name() { return Thread.currentThread().getName(); }

        private void logEvent(String event) {
            Room.this.logMessage(name()+" "+event);
        }

        private void logMeal(){
            meals.add(name()+" ate meal at "+(System.currentTimeMillis()-startTime));
            logEvent("eats meal");
        }

        // This method is called by the Chair object when succesfully siting down/standing up to/from it
        public void setChair(Chair chair) {
            int i = 0;
            if (chairs.indexOf(chair)>=0) i++ ;
            if (chairs.indexOf(this.chair)>=0) i-- ;
            if (i!=0) philosophersSitingAtTable.addAndGet(i);
            this.chair = chair;
        }
    }
}
