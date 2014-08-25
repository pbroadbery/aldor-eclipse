package aldor.util.event;

public interface EventListener<T> {
	void onEvent(T event);
	void onError(RuntimeException e);
	void completed();
}