package aldor.util.event;

import java.util.LinkedList;
import java.util.List;

public class EventStreamListBuilder<T> implements EventListener<T> {
	List<T> events = new LinkedList<T>();
	EventSource<List<T>> helper = new EventSource<>();
	RuntimeException error;
	private List<T> all;
	
	@Override
	public void onEvent(T event) {
		events.add(event);
	}

	@Override
	public void onError(RuntimeException e) {
		helper.error(e);
	}

	@Override
	public void completed() {
		this.all = events;
		events = null;
	}
	
	public List<T> all() {
		return all;
	}

}
