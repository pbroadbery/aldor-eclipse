package aldor.util;

import java.util.concurrent.Callable;

public interface SafeCallable<T> extends Callable<T>{
	T call();
}
