package aldor.util.event;

import java.util.LinkedList;
import java.util.List;

public class EventSource<T> {
	private List<EventListener<T>> listeners = new LinkedList<>();
	
	public EventListener<T> addListener(EventListener<T> listener) {
		listeners.add(listener);
		return listener;
	}
	
	public void removeListener(EventListener<T> listener) {
		listeners.remove(listener);
	}

	public void event(T event) {
		for (EventListener<T> listener: listeners) {
			listener.onEvent(event);
		}
	}
	
	public void completed() {
		for (EventListener<T> listener: listeners) {
			listener.completed();
		}
		listeners.clear();
	}
	public void error(RuntimeException e) {
		for (EventListener<T> listener: listeners) {
			listener.onError(e);
		}
		listeners.clear();
	}
}
