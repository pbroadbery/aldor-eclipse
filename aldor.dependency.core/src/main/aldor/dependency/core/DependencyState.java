package aldor.dependency.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class DependencyState<Named extends INamed> implements IDependencyState<Named> {
	private DependencyMap dependencyMap = new DependencyMap();
	private KnownFileState<Named> fileState = new KnownFileState<Named>();
	private Set<String> needsDependencyUpdate = new TreeSet<>();
	private Set<String> needsRebuild = new TreeSet<>();
	private Set<Named> doNotBuildSet = new HashSet<Named>();
	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#release()
	 */
	@Override
	public void release() {
		// Mark object as dead (these are not "releasing resources", just
		// ensuring a blow up).
		dependencyMap = null;
		fileState = null;
		needsRebuild = null;
		needsDependencyUpdate = null;
	}

	@Override
	public String toString() {
		return "{DepState " + dependencyMap + " Files: " + fileState
				+ " Do not build: " + doNotBuildSet
				+ " Rebuild: "+ needsRebuild + " depUpdate: " + needsDependencyUpdate + "}";
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#aldorFileAdded(Named)
	 */
	@Override
	public void aldorFileAdded(Named file) {
		assert fileState.validate();
		boolean needsUpdate = fileState.add(file);
		if (needsUpdate)
			clearBuildInformation(file.getName());
		this.needsRebuild.add(file.getName());
		assert fileState.validate();
		assert !this.needsDependencyUpdate().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#aldorFileRemoved(Named)
	 */
	@Override
	public void aldorFileRemoved(Named file) {
		assert fileState.validate();
		boolean needsUpdate = fileState.remove(file);
		if (needsUpdate)
			clearBuildInformation(file.getName());
		assert fileState.validate();
	}

	@Override
	public void doNotBuild(Named file) {
		this.doNotBuildSet.add(file);
	}

	private boolean isNotForBuild(Named file) {
		return this.doNotBuildSet.contains(file);
	}

	private void clearBuildInformation(String name) {
		dependencyMap.clearDependencies(name);
		needsDependencyUpdate.add(name);
		doNotBuildSet.remove(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#aldorFileChanged(Named)
	 */
	@Override
	public void aldorFileChanged(Named file) {
		assert fileState.validate();
		needsRebuild.add(file.getName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#needsDependencyUpdate()
	 */
	@Override
	public Collection<Named> needsDependencyUpdate() {
		return Collections2.transform(needsDependencyUpdate, new Function<String, Named>() {

			@Override
			public Named apply(String arg0) {
				assert fileState.isKnownName(arg0);
				return fileState.fileForString(arg0);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see aldor.dependency.core.IDependencyState#updateDependencies(Named,
	 * java.util.List)
	 */
	@Override
	public void updateDependencies(Named file, Iterable<String> scan) {
		String name = fileState.stringForName(file);
		boolean needsBuild = false;
		if (name == null) {
			return;
		}
		for (String dependencyName : scan) {
			if (fileState.isKnownName(dependencyName) && !dependencyMap.isDependency(name, dependencyName)) {
				dependencyMap.dependsOn(name, dependencyName);
				needsBuild = true;
			}
		}

		if (needsBuild)
			this.needsRebuild.add(name);
		this.needsDependencyUpdate.remove(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * aldor.dependency.core.IDependencyState#visitInBuildOrder(com.google.common
	 * .base.Function)
	 */
	@Override
	public void visitInBuildOrder(Function<Named, Boolean> function) {
		dependencyMap.rebuild();
		Map<String, Boolean> visited = new HashMap<String, Boolean>();
		for (String name : fileState.knownFiles()) {
			visitInBuildOrder0(function, name, visited);
		}
	}

	@Override
	public void visitInBuildOrder(Function<Named, Boolean> function, Named named) {
		dependencyMap.rebuild();
		Map<String, Boolean> visited = new HashMap<String, Boolean>();
		visitInBuildOrder0(function, named.getName(), visited);
	}

	private Boolean visitInBuildOrder0(Function<Named, Boolean> function, String name, Map<String, Boolean> visited) {
		if (visited.containsKey(name))
			return visited.get(name);

		visited.put(name, false);
		if (dependencyMap.inCycle(name)) {
			visited.put(name, false);
			return false;
		}
		boolean success = true;
		// We could do a parallel build here...
		for (String dependencyName : dependencyMap.dependencies(name)) {
			success = success && visitInBuildOrder0(function, dependencyName, visited);
		}
		if (success) {
			success = safeApply(function, name);
		}
		visited.put(name, success);
		return success;
	}

	enum Status {
		Updated, Failed, UpToDate;

		public Status and(Status status) {
			switch (this) {
			case Updated:
				if (status == Failed)
					return Failed;
				return Updated;
			case Failed:
				return Failed;
			case UpToDate:
				return status;
			default:
				throw new RuntimeException();
			}
		}
	}

	@Override
	public boolean visitInBuildOrderForBuild(Function<Named, Boolean> function) {
		dependencyMap.rebuild();
		Map<String, Status> visited = new HashMap<String, Status>();
		Status status = Status.UpToDate;
		for (String name : fileState.knownFiles()) {
			status = status.and(visitInBuildOrderForBuild0(function, name, visited));
		}

		if (status == Status.Failed)
			return false;
		return true;
	}

	private Status visitInBuildOrderForBuild0(Function<Named, Boolean> function, String name, Map<String, Status> visited) {
		if (visited.containsKey(name))
			return visited.get(name);
		if (this.isNotForBuild(fileState.fileForString(name))) {
			visited.put(name, Status.UpToDate);
			return Status.UpToDate;
		}
		visited.put(name, Status.Failed);

		if (dependencyMap.inCycle(name)) {
			visited.put(name, Status.Failed);
			return Status.Failed;
		}

		Status status = Status.UpToDate;
		// We could do a parallel build here...
		for (String dependencyName : dependencyMap.dependencies(name)) {
			status = status.and(visitInBuildOrderForBuild0(function, dependencyName, visited));
		}
		if (status.equals(Status.UpToDate) && !needsBuild(name)) {
			visited.put(name, status);
			return Status.UpToDate;
		}
		else {
			boolean result = safeApply(function, name);
			visited.put(name, status);
			return result ? Status.Updated : Status.Failed;
		}
	}

	private Boolean safeApply(Function<Named, Boolean> function, String name) {
		if (!fileState.isKnownName(name)) {
			return false;
		}
		Named file = fileState.fileForString(name);
		try {
			return function.apply(file);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isKnownName(String name) {
		return fileState.isKnownName(name);
	}

	@Override
	public boolean needsBuild(String name) {
		return needsRebuild.contains(name);
	}

	@Override
	public void built(String name) {
		needsRebuild.remove(name);
	}
}
