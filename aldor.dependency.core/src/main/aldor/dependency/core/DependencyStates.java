package aldor.dependency.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;

public class DependencyStates {


	public static <T> void clearBuildOrder(final IDependencyState<T> depState) {
		depState.visitInBuildOrder(new Function<T, Boolean>() {

			@Override
			public Boolean apply(T input) {
				depState.built((String) input);
				return true;
			}});
		
	}

	public static <T> List<T> buildOrderForBuild(IDependencyState<T> depState) {
		final List<T> l = new ArrayList<T>();

		depState.visitInBuildOrderForBuild(new Function<T, Boolean>() {

			@Override
			public Boolean apply(T input) {
				l.add(input);
				return true;
			}});
		return l;
	}
}
