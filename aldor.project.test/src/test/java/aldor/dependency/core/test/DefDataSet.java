package aldor.dependency.core.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import aldor.dependency.core.DependencyMap;
import aldor.dependency.core.IDependencyState;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

class DefDataSet {
	List<String> files = Lists.newArrayList("1", "2", "3", "a", "b", "c");
	@SuppressWarnings("unchecked")
	List<Map.Entry<String, String>> deps = Lists.newArrayList(
			Maps.immutableEntry("a", "1"),
			Maps.immutableEntry("b", "1"),
			Maps.immutableEntry("c", "a"),
			Maps.immutableEntry("c", "b"),
			Maps.immutableEntry("c", "3"));

	IDependencyState<String> depGraph() {
		IDependencyState<String> depState = new StringDependencyState();
		for (String file: files) {
			depState.aldorFileAdded(file);
		}
		Multimap<String, String> dd = multimap();
		for (String name: dd.keys()) {
			depState.updateDependencies(name, new ArrayList<>(dd.get(name)));
		}
		return depState;
	}

	private Multimap<String, String> multimap() {
		Multimap<String, String> dd = HashMultimap.create();
		for (Map.Entry<String, String> ent: deps) {
			dd.put(ent.getKey(), ent.getValue());
		}
		return dd;
	}
	
	int graphSize() {
		return files.size();
	}
	
	DependencyMap defMap() {
		DependencyMap map = new DependencyMap();
		Multimap<String, String> mmap = multimap();
		for (Entry<String, String> name: mmap.entries()) {
			map.dependsOn(name.getKey(), name.getValue());
		}
		return map;
			
	}

	public boolean isValidOrder(List<String> built) {
		for (int i=0; i<built.size(); i++) {
			List<String> rhs = built.subList(i+1, built.size());
			for (String dep: rhs) {
				if (deps.contains(Maps.immutableEntry(built.get(i), dep))) {
					System.out.println("Check failed: " + built.get(i) + " " + dep);
					return false;
				}
			}
		}
		return true;
	}

	public List<String> dependents(String exclude) {
		List<String> list = new ArrayList<>();
		for (Map.Entry<String, String> dep: deps) {
			if (dep.getValue().equals(exclude))
				list.add(dep.getKey());
		}
		return list;
	}

	public int cycleCount() {
		return 0;
	}

}