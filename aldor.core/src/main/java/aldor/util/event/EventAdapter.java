package aldor.util.event;

public abstract class EventAdapter<T> implements EventListener<T> {

	@Override
	public void onEvent(T event) {
	}

	@Override
	public void onError(RuntimeException e) {
	}

	@Override
	public void completed() {
	}

}
