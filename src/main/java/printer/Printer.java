package printer;

public class Printer {
    private int paperCount = 3;
    private final int capacity = 5;

    public synchronized void print(String doc) throws InterruptedException {
        // TODO:
        // Check if there is any paper left
        // If  no paper, we should wait.
        // Once out of the loop, pretend  print a one-page document (just print something to the console)
        // Since we used one piece of paper, decrement paperCount

        Thread.sleep(200); // simulate print time
    }

    public synchronized void refill() {
        // TODO:
        // Refill the paper by setting the paper count to capacity
        // Print a message saying you refilled the paper
        // Notify all waiting threads

    }
}

