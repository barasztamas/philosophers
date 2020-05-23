package philosophers.chairs;

import philosophers.Fork;
import philosophers.Hand;
import philosophers.Philosopher;

public class Chair {
    private Fork left;
    private Fork right;
    private Philosopher philosopher;

    public Chair(Fork left, Fork right) {
        this.left = left;
        this.right = right;
    }
    public Chair() {}

    public Fork getFork(Hand hand) {
        if (hand==Hand.LEFT) return left;
        if (hand==Hand.RIGHT) return right;
        throw new IllegalArgumentException("");
    }

    // this method is syncronised to avoid someone else occupying the chair between our philosoph checking if
    //  it is free and siting down on it
    public synchronized boolean trySitOn(Philosopher philosopher) {
        if (this.isFree()) {
            this.sitOn(philosopher);
            return true;
        }
        return false;
    }
    // regular chairs are protected from multiple occuppants by not exposing the sitOn method, only through the syncronised trySitOn.
    //  This is enough here, as philosophers don't need to wait for a regular chair, they only sit on them when they are free
    protected void sitOn(Philosopher philosopher) {
        if(this.philosopher != null) throw new IllegalStateException("Chair not empty!");
        this.philosopher = philosopher;
        philosopher.setChair(this);
    }

    public boolean isFree() {return philosopher == null;}

    public void standUpFrom(Philosopher philosopher) {
        if(philosopher!=this.philosopher) throw new IllegalArgumentException("Only someone sitting on a chair can stand up from it!");

        this.philosopher = null;
        philosopher.setChair(null);
    }
}

