package aldor.util;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class Lists2Test {
	@Test
	public void testContainsSubList() {
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList(), Lists.newArrayList()));
		Assert.assertFalse(Lists2.containsSubList(Lists.newArrayList(), Lists.newArrayList("a")));
	
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a"), Lists.<String>newArrayList()));

		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b"), Lists.<String>newArrayList("a")));
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b"), Lists.<String>newArrayList("b")));
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b"), Lists.<String>newArrayList("a")));
		
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b"), Lists.<String>newArrayList("a", "b")));

		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b", "c"), Lists.<String>newArrayList("a", "b")));
		Assert.assertTrue(Lists2.containsSubList(Lists.newArrayList("a", "b", "c"), Lists.<String>newArrayList("b", "c")));

		
	}
}
