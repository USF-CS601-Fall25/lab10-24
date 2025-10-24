package blockingqueue;

import java.util.LinkedList;
import java.util.List;

/** BlockingQueue exercise - demonstrates the use of wait() and notify().
 * From http://tutorials.jenkov.com/java-concurrency/blocking-queues.html
 */
public class BlockingQueue {
	private List<Integer> queue = new LinkedList<>();
	private int capacity;

	public BlockingQueue(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Waits until there is space in the queue, then adds an element to the queue
	 * @param element the element to be added to the queue
	 * @throws InterruptedException
	 */
	public synchronized void enqueue(Integer element) throws InterruptedException {
		// FILL IN CODE


		queue.add(element);
		System.out.println("Added an element: " + element);

	}

	/**
	 * Waits until the queue has something to dequeue, then dequeues it and returns the element.
	 * @return the first element from the queue
	 * @throws InterruptedException
	 */
	public synchronized Integer dequeue() throws InterruptedException {
		// FILL IN CODE

		int elem = queue.remove(0);
		System.out.println("Removed an element " + elem);

		return elem;
	}
}
