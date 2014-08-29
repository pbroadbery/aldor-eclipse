package aldor.dependency.core.test;

import aldor.dependency.core.DelegatedDependencyState;

class StringDependencyState extends DelegatedDependencyState<String> {

	@Override
	public String getName(String obj) {
		return obj;
	}
}