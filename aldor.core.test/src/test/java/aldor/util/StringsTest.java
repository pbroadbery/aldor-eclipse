package aldor.util;


import org.junit.Assert;
import org.junit.Test;

public class StringsTest {
	static enum Colour {R, G, B; }
	@Test
	public void testBoolean() {
		Assert.assertEquals(true, (boolean) Strings.instance().decode(Boolean.class, "true"));
		Assert.assertEquals("true", Strings.instance().encode(Boolean.class, true));

		Assert.assertEquals(Colour.R, Strings.instance().decode(Colour.class, "R"));
		Assert.assertEquals(Colour.G, Strings.instance().decode(Colour.class, "G"));
		Assert.assertEquals(Colour.B, Strings.instance().decode(Colour.class, "B"));

		Assert.assertNull(Strings.instance().decode(Colour.class, null));
		try {
			Assert.assertEquals(Colour.B, Strings.instance().decode(Colour.class, "X"));
			Assert.fail();
		}
		catch (RuntimeException e) {

		}
	}
}
