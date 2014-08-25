package aldor.dependency.core;

class StringDependencyState extends DelegatedDependencyState<String> {

	@Override
	public String getName(String obj) {
		return obj;
	}
}