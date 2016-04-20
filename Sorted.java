package data_structures;

public interface Sorted<T extends Comparable<T>> {
	public void add(T t);
	public void remove(T t);
}
