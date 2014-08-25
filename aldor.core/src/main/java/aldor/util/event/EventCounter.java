package aldor.util.event;


public class EventCounter<T> implements EventListener<T> {
	private RuntimeException error;
	private int count;
	private int total;
	
	@Override
	public void onEvent(T event) {
		count++;
		assert total == 0 && error == null;
	}

	@Override
	public void onError(RuntimeException e) {
		this.error = e;
	}

	@Override
	public void completed() {
		total = count;
	}

	public int total() {
		return total;
	}

}
