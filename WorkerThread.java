package data_structures;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class WorkerThread extends Thread {
	private int id;
	private int nrIterations;
	private Sorted<Integer> sorted;
	private int[] itemsToAdd;
	private int[] itemsToRemove;
	private int workTime;
	private boolean doWork;
	private boolean doDebug;
	private CyclicBarrier barrier;
	
	WorkerThread(int id, Sorted<Integer> list, int nrIterations, int[] itemsToAdd, int[] itemsToRemove, int workTime, CyclicBarrier barrier, boolean debug) {
		this.sorted = list;
		this.id = id;
		this.nrIterations = nrIterations;
		this.itemsToAdd = itemsToAdd;
		this.itemsToRemove = itemsToRemove;
		this.workTime = workTime;
		this.doWork = workTime > 0;
		this.barrier = barrier;
		this.doDebug = debug;
	}

	public void run() {
		int startIndex = nrIterations * id;
		add(sorted, startIndex, nrIterations, itemsToAdd);

		try {
		    barrier.await();
		    if (this.doDebug) {
		    	    if (this.id == 0) {
		    	    	    System.out.printf("Output after adding, before removing:\n%s\n", sorted.toString());
		    	    }
		    	    barrier.await();
		    }
		}
		catch (InterruptedException e) {
		    e.printStackTrace();
		}
		catch (BrokenBarrierException e) {
		    e.printStackTrace();
		}

		remove(sorted, startIndex, nrIterations, itemsToRemove);
	}

	private void remove(Sorted<Integer> sorted, int startIndex, int nrIterations, int[] itemsToRemove) {
		for (int i = startIndex; i < startIndex + nrIterations; i++) {
			doWork();
			sorted.remove(itemsToRemove[i]);
		}
	}

	private void add(Sorted<Integer> sorted, int startIndex, int nrIterations, int[] itemsToAdd) {
		for (int i = startIndex; i < startIndex + nrIterations; i++) {
			doWork();
			sorted.add(itemsToAdd[i]);
		}
	}

	private void doWork() {
		if (doWork) {
			long end = System.nanoTime() + workTime * 1000;
			while (System.nanoTime() < end); // busy wait
		}
	}
}
