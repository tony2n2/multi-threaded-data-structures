package data_structures;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import data_structures.implementation.CoarseGrainedList;
import data_structures.implementation.CoarseGrainedTree;
import data_structures.implementation.FineGrainedList;
import data_structures.implementation.FineGrainedTree;
import data_structures.implementation.LockFreeList;
import data_structures.implementation.LockFreeTree;

public class Main {

	private static final String CGL = "cgl";
	private static final String CGT = "cgt";
	private static final String FGL = "fgl";
	private static final String FGT = "fgt";
	private static final String LFL = "lfl";
	private static final String LFT = "lft";

  // Compute a unique number from the three parameters
	private static long computeSeed(int param1, int param2, int param3) {
	    long result = param2;
	    result <<= 16;
	    result |= (long) param1;
	    result <<= 16;
	    result |= (long) param3;
	    return result;
	}

  // Permute the array in a predictable manner (based on the seed)
	private static void permute(int[] array, long seed) {
		Random random = new Random(seed);

		for (int i = 0; i < array.length; i++) {
			int r = random.nextInt(array.length);
			int swapped = array[i];
			array[i] = array[r];
			array[r] = swapped;
		}
	}

  // Fills the itemsToAdd and itemsToRemove arrays with pseudo-random numbers (based on the seed)
  // There will be no double numbers.
	private static void createWorkDataWithoutDoubles(int[] itemsToAdd, int[] itemsToRemove, long seed) {
		for (int i = 0; i < itemsToAdd.length; i++) {
			itemsToAdd[i] = i;
			itemsToRemove[i] = i;
		}

		permute(itemsToAdd, seed);
		permute(itemsToRemove, seed + 1);
	}

  // Fills the itemsToAdd and itemsToRemove arrays with pseudo-random numbers (based on the seed)
  // There might be double numbers.
	private static void createWorkDataWithDoubles(int[] itemsToAdd, int[] itemsToRemove, long seed) {
		Random random = new Random(seed);

		for (int i = 0; i < itemsToAdd.length; i++) {
			int nextRandom = random.nextInt();
			itemsToAdd[i] = nextRandom;
			itemsToRemove[i] = nextRandom;
		}

		permute(itemsToRemove, seed + 1);
	}

	private static void createWorkData(int[] itemsToAdd, int[] itemsToRemove, long seed, boolean doubles) {
		if (doubles) {
			createWorkDataWithDoubles(itemsToAdd, itemsToRemove, seed);
		} else {
			createWorkDataWithoutDoubles(itemsToAdd, itemsToRemove, seed);
		}
	}

	private static void startThreads(Sorted<Integer> sorted, int nrThreads, int nrItems, int workTime, long seed, boolean doubles, boolean debug) throws InterruptedException {
		int[] itemsToAdd = new int[nrItems];
		int[] itemsToRemove = new int[nrItems];
		createWorkData(itemsToAdd, itemsToRemove, seed, doubles);

		WorkerThread[] workerThreads = new WorkerThread[nrThreads];
		CyclicBarrier barrier = new CyclicBarrier(nrThreads);

		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i] = new WorkerThread(i, sorted, nrItems / nrThreads, itemsToAdd, itemsToRemove, workTime, barrier, debug);
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i].start();
		}

		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i].join();
		}
		long end = System.currentTimeMillis();

		if (debug) {
		System.out.println("data structure after removal (should be empty):");
		String st = sorted.toString();
		if (st.length() >= 0) {
			System.out.println(sorted);
		}
		System.out.println();
		System.out.printf("time: %d ms\n\n", end - start);
		} else {
		System.out.println(end - start);
		}
	}

	private static void performWork(String dataStructure, int nrThreads, int nrItems, int workTime, long seed, boolean debug, int innerWorkTime) throws InterruptedException {
		Sorted<Integer> sorted = null;

		boolean doubles = true;

		if (dataStructure.equals(CGL)) {
			sorted = new CoarseGrainedList<Integer>(innerWorkTime);
		} else if (dataStructure.equals(CGT)) {
			sorted = new CoarseGrainedTree<Integer>(innerWorkTime);
		} else if (dataStructure.equals(FGL)) {
			sorted = new FineGrainedList<Integer>(innerWorkTime);
		} else if (dataStructure.equals(FGT)) {
			sorted = new FineGrainedTree<Integer>(innerWorkTime);
		} else if (dataStructure.equals(LFL)) {
			sorted = new LockFreeList<Integer>(innerWorkTime);
		} else if (dataStructure.equals(LFT)) {
			sorted = new LockFreeTree<Integer>(innerWorkTime);
			doubles = false;
		} else {
			exitWithError();
		}

		if (debug) {
    	    	    System.out.printf("Output before adding:\n%s\n", sorted.toString());
		}

		startThreads(sorted, nrThreads, nrItems, workTime, seed, doubles, debug);
	}

	private static void exitWithError() {
		System.out .println("test_data_structures <data_structure> <nrThreads> <nrItems> <workTime> [debug]");
		System.out.println("  where:");
		System.out.printf("    <data_structure> in {%s, %s, %s, %s, %s, %s}\n", CGL, CGT, FGL, FGT, LFL, LFT);
		System.out.println("    <nrThreads> is a number > 0");
		System.out.println("    <nrItems> is a number > 0");
		System.out.println("    <workTime> is a number >= 0 (micro seconds)");
		System.out.println("    <innerWorkTime> is a number >= 0 (micro seconds)");
		System.out.println("    [debug] can be omitted. If added as the last parameter,");
		System.out.println("            the output of Sorted.toString() will be printed ");
		System.out.println("            after adding and before removing the numbers.");
		System.exit(1);
	}

	public static void main(String[] args) throws InterruptedException {
		if (args.length < 5 || args.length > 6) {
			exitWithError();
		}

		String dataStructure = args[0];
		int nrThreads = Integer.parseInt(args[1]);
		if (nrThreads < 1) {
			exitWithError();
		}

		int nrItems = Integer.parseInt(args[2]);
		if (nrItems < 1) {
			exitWithError();
		}

		if (nrItems % nrThreads != 0) {
			System.out.println("undiv");
			System.exit(1);
		}

		int workTime = Integer.parseInt(args[3]);
		if (workTime < 0) {
			exitWithError();
		}
		
		int innerWorkTime = Integer.parseInt(args[4]);
		if (innerWorkTime < 0) {
			exitWithError();
		}
		
		boolean debug = false;
		if (args.length == 6) {
			System.out.println(args[5]);
			if (args[5].equals("debug")) {
				debug = true;
			} else {
				System.out.println("last argument should be 'debug', or be omitted\n");
				System.exit(1);
			}
		}

		long seed = computeSeed(nrThreads, nrItems, workTime);

		performWork(dataStructure, nrThreads, nrItems, workTime, seed, debug, innerWorkTime);
	}
}
