package aldor.dependency.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class DependencyMapTest {

	@Test
	public void test() {
		DependencyMap map = new DependencyMap();
		Collection<String> clqs;
		
		map.dependsOn("a", "b");
		assertTrue(map.cycles().isEmpty());
		
		map.dependsOn("b", "a");
		clqs = map.cycles();
		System.out.println("clqs " + clqs);
		assertEquals(2, clqs.size());

		map.dependsOn("a", "c");
		clqs = map.cycles();
		System.out.println("clqs " + clqs);
		assertEquals(2, clqs.size());
		
		map.dependsOn("c", "d");
		map.dependsOn("d", "c");
		
		clqs = map.cycles();
		System.out.println("clqs " + clqs);
		assertEquals(4, clqs.size());
		
		map.clearDependencies("a");
		System.out.println("clqs " + clqs);
		clqs = map.cycles();
		assertEquals(2, clqs.size());
	}

	@Test
	public void test2() {
		DependencyMap map = new DependencyMap();
		map.dependsOn("fred", "bob");
		map.dependsOn("bob", "jim");
		assertEquals(0, map.cycles().size());
	}
	
	@Test
	public void test3() {
		DefDataSet ds = new DefDataSet();
		DependencyMap map = ds.defMap();
		assertEquals(ds.cycleCount(), map.cycles().size());
	}
}
