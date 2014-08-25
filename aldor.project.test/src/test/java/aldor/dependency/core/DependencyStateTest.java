package aldor.dependency.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DependencyStateTest {

	@Test
	public void testOne() {
		IDependencyState<String> depState = new StringDependencyState();
		depState.aldorFileAdded("fred");

		assertFalse(depState.needsDependencyUpdate().isEmpty());
	}

	@Test
	public void testTwo() {
		IDependencyState<String> depState = new StringDependencyState();
		depState.aldorFileAdded("fred");
		depState.aldorFileAdded("bob");

		depState.updateDependencies("fred", Collections.singletonList("bob"));
		final List<String> built = new ArrayList<>();
		buildToList(depState, built);
		assertEquals(Lists.newArrayList("bob", "fred"), built);
	}

	@Test
	public void testClique() {
		IDependencyState<String> depState = new StringDependencyState();
		depState.aldorFileAdded("fred");
		depState.aldorFileAdded("bob");

		depState.updateDependencies("fred", Collections.singletonList("bob"));
		depState.updateDependencies("bob", Collections.singletonList("fred"));
		final List<String> built = new ArrayList<>();
		buildToList(depState, built);
		assertEquals(Lists.newArrayList(), built);
	}

	@Test
	public void testThree() {
		IDependencyState<String> depState = new StringDependencyState();
		depState.aldorFileAdded("fred");
		depState.aldorFileAdded("jim");
		depState.aldorFileAdded("bob");

		depState.updateDependencies("fred", Collections.singletonList("bob"));
		depState.updateDependencies("bob", Collections.singletonList("jim"));
		final List<String> built = new ArrayList<>();
		buildToList(depState, built);
		assertEquals(Lists.newArrayList("jim", "bob", "fred"), built);
	}

	@Test
	public void testFour() {
		DefDataSet ds = new DefDataSet();
		IDependencyState<String> depState = ds.depGraph();
		final List<String> built = new ArrayList<>();
		buildToList(depState, built);
		assertEquals(ds.graphSize(), built.size());
		assertTrue(ds.isValidOrder(built));
	}

	@Test
	public void testFive() {
		DefDataSet ds = new DefDataSet();
		IDependencyState<String> depState = ds.depGraph();
		for (String exclude : ds.files) {
			final List<String> built = new ArrayList<>();
			buildToListWithFail(depState, built, Sets.newHashSet(exclude));
			// assertEquals(ds.graphSize(), built.size());
			assertTrue(built.contains(exclude));
			System.out.println("Excl: " + exclude + " " + built + " " + ds.dependents(exclude));
			assertTrue(ds.isValidOrder(built));
			for (String name : ds.dependents(exclude)) {
				assertFalse(built.contains(name));
			}
		}

	}

	private void buildToList(IDependencyState<String> depState,
			final List<String> built) {
		depState.visitInBuildOrder(new Function<String, Boolean>() {

			@Override
			public Boolean apply(String arg0) {
				built.add(arg0);
				return true;
			}
		});
	}

	private void buildToListWithFail(IDependencyState<String> depState,
			final List<String> built, final Set<String> failures) {
		depState.visitInBuildOrder(new Function<String, Boolean>() {

			@Override
			public Boolean apply(String arg0) {
				built.add(arg0);
				boolean b = !failures.contains(arg0);
				System.out.println("BUILD: " + arg0 + " --> " + b);
				return b;
			}
		});
	}

}
