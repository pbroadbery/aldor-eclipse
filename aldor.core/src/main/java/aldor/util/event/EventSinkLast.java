package aldor.util.event;


public class EventSinkLast<T> implements EventListener<T> {
	T last;
	boolean isCompleted;
	RuntimeException exception;
	@Override
	public void onEvent(T event) {
		this.last = event;
	}

	@Override
	public void onError(RuntimeException e) {
		this.exception = e;
	}

	@Override
	public void completed() {
		this.isCompleted = false;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public boolean isInError() {
		return exception != null;
	}

	public T last() {
		return last;
	}
	
	public RuntimeException exception() {
		return exception;
	}

}
