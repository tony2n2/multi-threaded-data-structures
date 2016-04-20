package data_structures.implementation;

import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	private Node guard;
	private Lock lock = new ReentrantLock();
	private int innerWorkTime;
	private boolean doInnerWork;

	public CoarseGrainedTree(int innerWorkTime) {
		guard = new Node(null, null, null);
		this.innerWorkTime = innerWorkTime;
		this.doInnerWork = innerWorkTime > 0;
	}

	public void add(T t) {
		Node curr, next;
		lock.lock();
		try {
			if (guard.left == null) {
				guard.left = new Node(t, null, null);
			} else {
				curr = guard;
				next = guard.left;
				while (next != null) {
					curr = next;
					next = t.compareTo(curr.value) < 0 ? curr.left : curr.right;
				}
				doWork();
				if (t.compareTo(curr.value) < 0) {
					curr.left = new Node(t, null, null);
				} else {
					curr.right = new Node(t, null, null);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void remove(T t) {
		Node pred, curr;
		Boolean goLeft = true;
		lock.lock();
		try {
			pred = guard;
			curr = guard.left;
			if (guard.left.left == null && guard.left.right == null) {
				doWork();
			}
			while (curr.value.compareTo(t) != 0
					&& null != ((goLeft = t.compareTo(curr.value) < 0) ? curr.left
							: curr.right)) {
				pred = curr;
				curr = goLeft ? curr.left : curr.right;
			}
			doWork();
			removeNode(curr, pred, goLeft);
		} finally {
			lock.unlock();
		}
	}

	private void removeNode(Node toRemove, Node parent, boolean isLeft) {
		Node pred;
		Node maxOfLeft;
		Node t;
		if (toRemove.left != null && toRemove.right != null) {
			pred = toRemove.left;
			if (pred.right == null) {
				maxOfLeft = pred;
				removeNode(maxOfLeft, toRemove, true);
				maxOfLeft.right = toRemove.right;
			} else {
				maxOfLeft = pred.right;
				while (maxOfLeft.right != null) {
					pred = maxOfLeft;
					maxOfLeft = maxOfLeft.right;
				}
				removeNode(maxOfLeft, pred, false);
				maxOfLeft.left = toRemove.left;
				maxOfLeft.right = toRemove.right;
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

	public String toString() {
		String result = "[";
		Stack<Node> toProcess;
		Node currNode;
		if (guard.left == null) {
			// nothing
		} else if (guard.left.left == null && guard.left.right == null) {
			result += guard.left.value + ", ";
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

	private void doWork() {
		if (doInnerWork) {
			long end = System.nanoTime() + innerWorkTime * 1000;
			while (System.nanoTime() < end)
				; // busy wait
		}
	}

	private class Node {
		public T value;
		public Node left;
		public Node right;

		Node(T v, Node l, Node r) {
			value = v;
			left = l;
			right = r;
		}
	}

}
