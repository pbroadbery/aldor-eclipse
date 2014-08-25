package aldor.util.event;


public class AbstractEventSource<T> {
	private EventSource<T> eventHelper = new EventSource<>();
	
	public void addListener(EventListener<T> listener) {
		eventHelper.addListener(listener);
	}

	public void removeListener(EventListener<T> listener) {
		eventHelper.removeListener(listener);
	}

}
