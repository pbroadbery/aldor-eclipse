package aldor.project.testutils;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;


public class Asserts {
	public static <T> void assertContains(T wanted, Collection<T> collection) {
		Assert.assertTrue("Expected "  + collection + " to contain " + wanted, collection.contains(wanted));
	}

	public static void assertDoesNotContain(String string, List<String> collection) {
		Assert.assertFalse("Expected " + collection + " to not contain " + string, collection.contains(string));
	}
}
