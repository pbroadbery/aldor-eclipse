package aldor.dependency.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class DependencyState<Named extends INamed> implements IDependencyState<Named> {
	DependencyMap dependencyMap = new DependencyMap();
	KnownFileState<Named> fileState = new KnownFileState<Named>();
	Set<String> needsDependencyUpdate = new TreeSet<>();

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
			updateDependencies(file.getName());
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
			updateDependencies(file.getName());
		assert fileState.validate();
	}

	private void updateDependencies(String name) {
		dependencyMap.clearDependencies(name);
		needsDependencyUpdate.add(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see aldor.dependency.core.IDependencyState#aldorFileChanged(Named)
	 */
	@Override
	public void aldorFileChanged(Named file) {
		assert fileState.validate();
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
		if (name == null) {
			return;
		}
		for (String dependencyName : scan) {
			if (fileState.isKnownName(dependencyName))
				dependencyMap.dependsOn(name, dependencyName);
		}
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

	static class KnownFileState<NamedObj extends INamed> {
		BiMap<String, NamedObj> namedObjForString;
		Multimap<String, NamedObj> duplicateNamedObj;

		KnownFileState() {
			this.namedObjForString = HashBiMap.create();
			this.duplicateNamedObj = HashMultimap.create();
		}

		public boolean isKnownName(String name) {
			return namedObjForString.containsKey(name);
		}

		public Set<String> knownFiles() {
			return namedObjForString.keySet();
		}

		boolean add(NamedObj file) {
			String name = file.getName();
			if (namedObjForString.containsKey(name)) {
				NamedObj oldFile = namedObjForString.get(name);
				namedObjForString.remove(name);
				duplicateNamedObj.put(name, file);
				duplicateNamedObj.put(name, oldFile);

				return true;
			} else if (duplicateNamedObj.containsKey(name)) {
				duplicateNamedObj.put(name, file);
				return false;
			} else {
				namedObjForString.put(name, file);
				return true;
			}
		}

		public String stringForName(NamedObj file) {
			return namedObjForString.inverse().get(file);
		}

		boolean remove(NamedObj file) {
			String name = file.getName();
			if (duplicateNamedObj.containsKey(name)) {
				duplicateNamedObj.remove(name, file);
				Collection<NamedObj> remainingFiles = duplicateNamedObj.get(name);
				if (remainingFiles.size() == 1) {
					duplicateNamedObj.removeAll(name);
					namedObjForString.put(name, Iterables.getFirst(remainingFiles, null));
					return true;
				}
				return false;
			} else {
				namedObjForString.remove(name);
				return true;
			}
		}

		boolean isDuplicate(NamedObj file) {
			return duplicateNamedObj.containsValue(file);
		}

		boolean validate() {
			for (String name : namedObjForString.keySet()) {
				if (duplicateNamedObj.containsKey(name)) {
					return false;
				}
			}
			for (String name : duplicateNamedObj.keys()) {
				if (namedObjForString.containsKey(name)) {
					return false;
				}
			}
			return true;

		}

		public NamedObj fileForString(String name) {
			assert namedObjForString.containsKey(name);
			return namedObjForString.get(name);
		}

	}
}
