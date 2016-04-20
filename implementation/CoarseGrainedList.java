package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data_structures.Sorted;

public class CoarseGrainedList<T extends Comparable<T>> implements Sorted<T> {

	private Node head;
	private Lock lock = new ReentrantLock();
	private int innerWorkTime;
	private boolean doInnerWork;
	
	public CoarseGrainedList(int innerWorkTime) {
		this.innerWorkTime = innerWorkTime;
		this.doInnerWork = innerWorkTime > 0;
	}

	public void add(T t) {
		Node pred, curr;
		lock.lock();
		try {
			if (head == null) {
				head = new Node(t, null);
			} else if (t.compareTo(head.key) < 0) {
				curr = head;
				head = new Node(t, curr);
			} else {
				pred = head;
				curr = head;
				while (curr != null && t.compareTo(curr.key) >= 0) {
					pred = curr;
					curr = curr.next;
				}
				doWork();
				pred.next = new Node(t, curr);
			}
		} finally {
			lock.unlock();
		}
	}

	public void remove(T t) {
		if (head == null) return;
		Node pred, curr;
		lock.lock();
		try {
			pred = curr = head;
			while (curr != null && t.compareTo(curr.key) > 0) {
				pred = curr;
				curr = curr.next;
			}
			doWork();
			if (curr == null) return;
			if (t.compareTo(curr.key) == 0) {
				pred.next = curr.next;
			}
		} finally {
			lock.unlock();
		}
	}

	public String toString() {

		String str = "[";
		Node curr;
		if (head == null) {
			return "[]";
		}
		curr = head;
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
		public T key;
		public Node next;
		
		Node(T k, Node n) {
			key = k;
			next = n;
		}
	}
}
