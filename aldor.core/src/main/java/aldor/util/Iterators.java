package aldor.util;

import java.util.Iterator;
import java.util.ListIterator;

public class Iterators {

	public final static <T> ListIterator<T> listIterator(final Iterator<T> iterator) {
		return new ListIterator<T>() {
			int index = 0;
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				index++;
				return iterator.next();
			}

			@Override
			public boolean hasPrevious() {
				throw new UnsupportedOperationException();
			}

			@Override
			public T previous() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int nextIndex() {
				return index + 1;
			}

			@Override
			public int previousIndex() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void remove() {
				iterator.remove();
			}

			@Override
			public void set(T e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(T e) {
				throw new UnsupportedOperationException();
			}};

	}
}
