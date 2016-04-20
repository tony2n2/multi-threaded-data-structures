package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {

	private Node tail = new Node(null);
	private Node head = new Node(tail);
	private int innerWorkTime;
	private boolean doInnerWork;
	
	public FineGrainedList(int innerWorkTime) {
		this.innerWorkTime = innerWorkTime;
		this.doInnerWork = innerWorkTime > 0;
	}

	public void add(T t) {
		head.lock.lock();
		Node pred = head;
		try {
			Node curr = pred.next;
			curr.lock.lock();
			try {
				if (curr == tail) {
					head.next = new Node(tail);
					head.next.next = tail;
					head.next.key = t;
				} else {
					while (curr != tail && t.compareTo(curr.key) > 0) {
						pred.lock.unlock();
						pred = curr;
						curr = curr.next;
						curr.lock.lock();
					}
					doWork();
					Node newNode = new Node(curr);
					newNode.key = t;
					pred.next = newNode;
				}
			} finally {
				curr.lock.unlock();
			}
		} finally {
			pred.lock.unlock();
		}

	}
	
	public void remove(T t) {
		Node pred, curr;
		head.lock.lock();
		pred = head;
		try {
			curr = pred.next;
			curr.lock.lock();
			try {
				while (curr != null && t.compareTo(curr.key) > 0) {
					pred.lock.unlock();
					pred = curr;
					curr = curr.next;
					curr.lock.lock();
				}
				doWork();
				if (t.compareTo(curr.key) == 0) {
					pred.next = curr.next;
				}
			} finally {
				curr.lock.unlock();
			}
		} finally {
			pred.lock.unlock();
		}
	}

	public String toString() {
		String str = "[";
		Node curr;
		if (head.next == tail) {
			return "[]";
		}
		curr = head.next;
		while (curr.next != null) {
			str += curr.key; // add element to string
			str += ", ";
			curr = curr.next;
		}
		if (str.length() > 2) {
			String str2 = str.substring(0, str.length() - 2);
			str = str2;
		}
		str += "]";
		return str;
	}
	
	private void doWork() {
		if (doInnerWork) {
			long end = System.nanoTime() + innerWorkTime * 1000;
			while (System.nanoTime() < end); // busy wait
		}
	}

	private class Node {
		private Lock lock = new ReentrantLock();

		public Node next;
		public T key;

		public Node(Node n) {
			this.next = n;
		}
	}
}