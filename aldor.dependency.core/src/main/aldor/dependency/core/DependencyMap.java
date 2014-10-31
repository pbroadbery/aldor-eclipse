package aldor.dependency.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DependencyMap {
	Multimap<String, String> dependencyMap = HashMultimap.create();
	Set<String> knownFiles = new HashSet<String>();
	Map<String, List<String>> dependencies = new HashMap<String, List<String>>();
	Map<String, Set<String>> cliques;
	Multimap<String, String> reverseMap = HashMultimap.create();

	boolean needsRebuild = true;

	public String toString() {
		return "{DepMap: " + dependencyMap +" files: " + knownFiles +" dependencies: " + dependencies + "clq: " + cliques + "rev: " + reverseMap + "}";
	}
	
	
	public void dependsOn(String dependent, String dependency) {
		dependencyMap.put(dependent, dependency);
		knownFiles.add(dependency);
		knownFiles.add(dependent);
		needsRebuild = true;
	}

	public void clearDependencies(String name) {
		dependencyMap.removeAll(name);
		knownFiles.remove(name);
		needsRebuild = true;
	}

	public boolean isDependency(String from, String to) {
		return dependencyMap.get(from).contains(to);
	}
	

	public boolean inCycle(String name) {
		return cliques.containsKey(name);
	}
	
	public Set<String> cycles() {
		rebuild();
		return cliques.keySet();
	}

	public Collection<String> dependencies(String name) {
		Collection<String> deps = dependencyMap.get(name);
		if (deps == null) {
			return Collections.emptyList();
		}
		return deps;
	}

	
	public void rebuild() {
		if (!needsRebuild)
			return;
		needsRebuild = false;
		buildReverseMap();
		buildCliques();
	}

	
	private void buildReverseMap() {
		reverseMap.clear();
		for (String key: dependencyMap.keySet()) {
			Collection<String> dependents = dependencyMap.get(key);
			for (String dependent: dependents) {
				reverseMap.put(dependent, key);
			}
		}
	}

	private void buildCliques() {
		cliques = new HashMap<>();
		List<Set<String>> cliqueList = findCliques();
		for (Set<String> clq: cliqueList) {
			for (String elt: clq) {
				cliques.put(elt, clq);
			}
		}
	}

	private List<Set<String>> findCliques() {
		return new Cliques().findCliques();
	}
	
	
	class Cliques {
		private List<String> visitOrder;
		private Set<String> visitTable;
		
		Cliques() {
			this.visitOrder = new ArrayList<String>();
			this.visitTable = new HashSet<String>();
		}
		
		public List<Set<String>> findCliques() {
			for (String node: knownFiles) {
				determineFwdOrder(node);
			}
			visitTable.clear();
			
			List<Set<String>> components = new LinkedList<Set<String>>();
			for (String node: Lists.reverse(visitOrder)) {
				if (!visitTable.contains(node)) {
					Set<String> component = reverseOrder(node);
					if (component.size() > 1)
						components.add(component);
				}
			}
			return components;
		}

		public void determineFwdOrder(String node) {
			if (visitTable.contains(node))
				return;
			visitTable.add(node);
			for (String dependency: dependencyMap.get(node)) {
				if (visitTable.contains(dependency)) {
					continue;
				}
				determineFwdOrder(dependency);
			}
			visitOrder.add(node);
		}
		
		public Set<String> reverseOrder(String node) {
			visitTable.add(node);
			HashSet<String> members = new HashSet<>();
			members.add(node);
			for (String dependent: reverseMap.get(node)) {
				if (!visitTable.contains(dependent)) {
					members.addAll(reverseOrder(dependent));
				}
			}
			return members;
		}
	}

}
