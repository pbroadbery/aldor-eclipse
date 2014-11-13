package aldor.dependency.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

class KnownFileState<NamedObj extends INamed> {
	private final BiMap<String, NamedObj> namedObjForString;
	private final Multimap<String, NamedObj> duplicateNamedObj;

	KnownFileState() {
		this.namedObjForString = HashBiMap.create();
		this.duplicateNamedObj = HashMultimap.create();
		this.notForBuildSet = new HashSet<>();
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
			return false;
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