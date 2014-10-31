package aldor.dependency.core;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public abstract class DelegatedDependencyState<T> implements
		IDependencyState<T> {
	private class Wrapper implements INamed {
		private T value;

		Wrapper(T obj) {
			assert obj != null;
			this.value = obj;
		}

		@Override
		public String getName() {
			return DelegatedDependencyState.this.getName(value);
		}

		public T value() {
			return value;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object other) {
			if (!(other instanceof DelegatedDependencyState.Wrapper))
				return false;
			return value.equals(((Wrapper) other).value());
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}

	private DependencyState<DelegatedDependencyState<T>.Wrapper> delegate;

	public DelegatedDependencyState() {
		this.delegate = new DependencyState<Wrapper>();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}


	@Override
	public void release() {
		delegate.release();
	}

	public abstract String getName(T obj);

	@Override
	public void aldorFileAdded(T file) {
		delegate.aldorFileAdded(new Wrapper(file));
	}

	@Override
	public void aldorFileRemoved(T file) {
		delegate.aldorFileRemoved(new Wrapper(file));
	}

	@Override
	public void aldorFileChanged(T file) {
		delegate.aldorFileChanged(new Wrapper(file));
	}

	@Override
	public boolean isKnownName(String name) {
		return delegate.isKnownName(name);
	}

	@Override
	public Collection<T> needsDependencyUpdate() {
		return Collections2.transform(delegate.needsDependencyUpdate(),
				new Function<Wrapper, T>() {
					@Override
					public T apply(DelegatedDependencyState<T>.Wrapper arg0) {
						return arg0.value();
					}
				});
	}

	@Override
	public void updateDependencies(T file, Iterable<String> filtered) {
		delegate.updateDependencies(new Wrapper(file), filtered);
	}

	@Override
	public boolean visitInBuildOrderForBuild(final Function<T, Boolean> function) {
		return delegate.visitInBuildOrderForBuild(new Function<Wrapper, Boolean>() {

			@Override
			public Boolean apply(DelegatedDependencyState<T>.Wrapper arg0) {
				assert arg0 != null;
				return function.apply(arg0.value());
			}
		});
	}

	
	@Override
	public void visitInBuildOrder(final Function<T, Boolean> function) {
		delegate.visitInBuildOrder(new Function<Wrapper, Boolean>() {

			@Override
			public Boolean apply(DelegatedDependencyState<T>.Wrapper arg0) {
				assert arg0 != null;
				return function.apply(arg0.value());
			}
		});
	}

	@Override
	public void visitInBuildOrder(final Function<T, Boolean> function, T from) {
		delegate.visitInBuildOrder(new Function<Wrapper, Boolean>() {

			@Override
			public Boolean apply(DelegatedDependencyState<T>.Wrapper arg0) {
				assert arg0 != null;
				return function.apply(arg0.value());
			}
		}, new Wrapper(from));
	}


	@Override
	public boolean needsBuild(String name) {
		return delegate.needsBuild(name);
	}

	@Override
	public void built(String name) {
		delegate.built(name);
	}

	
}
