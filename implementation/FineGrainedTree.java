package data_structures.implementation;

import data_structures.Sorted;

import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	private Node guard;
	private int innerWorkTime;
	private boolean doInnerWork;

	public FineGrainedTree(int innerWorkTime) {
		guard = new Node(null, null, null);
		this.innerWorkTime = innerWorkTime;
		this.doInnerWork = innerWorkTime > 0;
	}

	public void add(T t) {
		Node pred, curr;
		boolean goLeft;
		guard.lock();
		pred = guard;
		try {
			if (guard.left == null) { // root always left child of guard
				guard.left = new Node(t, null, null);
			} else {
				curr = pred.left;
				curr.lock();
				try {
					while (null != ((goLeft = t.compareTo(curr.value) < 0) ? curr.left
							: curr.right)) {
						pred.unlock();
						pred = curr;
						curr = goLeft ? curr.left : curr.right;
						curr.lock();
					}
					doWork();
					if (goLeft) {
						curr.left = new Node(t, null, null);
					} else {
						curr.right = new Node(t, null, null);
					}
				} finally {
					curr.unlock();
				}
			}
		} finally {
			pred.unlock();
		}
	}
	
	public void remove(T t) {
		Node pred, curr;
		Boolean goLeft = true;
		guard.lock();
		pred = guard;
		curr = guard.left;
		try {
			curr.lock();
			try {
				while (t.compareTo(curr.value) != 0
						&& null != ((goLeft = t.compareTo(curr.value) < 0) ? curr.left
								: curr.right)) {
					pred.unlock();
					pred = curr;
					curr = goLeft ? curr.left : curr.right;
					curr.lock();
				}
				doWork();
				removeNode(curr, pred, goLeft);
			} finally {
				curr.unlock();
			}
		} finally {
			pred.unlock();
		}
	}

	public String toString() {
		String result = "[";
		Stack<Node> toProcess;
		Node currNode;
		if (guard.left == null) {
			// nothing
		} else if (guard.left.left == null && guard.left.right == null) {
			result += guard.left.value;
		} else {
			toProcess = new Stack<Node>();
			currNode = guard.left;
			toProcess.push(currNode);
			while (currNode.left != null) {
				currNode = currNode.left;
				toProcess.push(currNode);
			}
			while (!toProcess.isEmpty()) {
				currNode = toProcess.pop();
				result += currNode.value + ", ";
				if (currNode.right != null) {
					currNode = currNode.right;
					toProcess.push(currNode);
					while (currNode.left != null) {
						currNode = currNode.left;
						toProcess.push(currNode);
					}
				}
			}
		}
		return (result == "[" ? result : result.substring(0,
				result.length() - 2)) + "]";
	}

	private void removeNode(Node toRemove, Node parent, boolean isLeft) {
		Node pred;
		Node maxOfLeft;
		Node t;
		if (toRemove.left != null && toRemove.right != null) {
			pred = toRemove.left;
			pred.lock();
			try {
				if (pred.right == null) {
					maxOfLeft = pred;
					removeNode(maxOfLeft, toRemove, true);
					maxOfLeft.right = toRemove.right;
				} else {
					maxOfLeft = pred.right;
					maxOfLeft.lock();
					try {
						while (maxOfLeft.right != null) {
							pred.unlock();
							pred = maxOfLeft;
							maxOfLeft = maxOfLeft.right;
							maxOfLeft.lock();
						}
						removeNode(maxOfLeft, pred, false);
						maxOfLeft.left = toRemove.left;
						maxOfLeft.right = toRemove.right;
					} finally {
						maxOfLeft.unlock();
					}
				}
			} finally {
				pred.unlock();
			}
			t = maxOfLeft;
		} else if (toRemove.left != null) {
			t = toRemove.left;
		} else if (toRemove.right != null) {
			t = toRemove.right;
		} else {
			t = null;
		}
		if (isLeft) {
			parent.left = t;
		} else {
			parent.right = t;
		}
	}
	
	private void doWork() {
		if (doInnerWork) {
			long end = System.nanoTime() + innerWorkTime * 1000;
			while (System.nanoTime() < end); // busy wait
		}
	}

	private class Node {
		public T value;
		public Node left;
		private Node right;
		private Lock lock;

		Node(T v, Node l, Node r) {
			value = v;
			left = l;
			right = r;
			lock = new ReentrantLock();
		}

		void lock() {
			lock.lock();
		}

		void unlock() {
			lock.unlock();
		}
	}
}