package philosophers.chairs;

import philosophers.Room;

import java.util.concurrent.locks.ReentrantLock;

public class SeparateChair extends Chair {
    private ReentrantLock lock = new ReentrantLock(true);

    // once a philosopher decides to sit on the separate chair, they'll wait until it is free.
    //  There is no need for a trySitOn method here.
    @Override
    public boolean trySitOn(Room.Philosopher philosopher) {
        return false;
    }

    // the separate chair is protected through a lock which makes other prospective occupants wait for their turn
    // the lock is claimed when a philosopher sits down on a chair, and is freed when they stand up
    @Override
    public void sitOn(Room.Philosopher philosopher) {
        lock.lock();
        super.sitOn(philosopher);
    }

    @Override
    public void standUpFrom(Room.Philosopher philosopher) {
        super.standUpFrom(philosopher);
        lock.unlock();
    }
}
