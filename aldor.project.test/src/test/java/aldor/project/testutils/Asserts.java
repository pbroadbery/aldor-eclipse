package aldor.project.testutils;

import java.util.Collection;

import org.junit.Assert;


public class Asserts {
	public static <T> void assertContains(Collection<T> collection, T wanted) {
		Assert.assertTrue("Expected "  + collection + " to contain " + wanted, collection.contains(wanted));
	}
}
