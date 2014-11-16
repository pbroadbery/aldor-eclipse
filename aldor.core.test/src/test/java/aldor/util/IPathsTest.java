package aldor.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class IPathsTest {
	@Test
	public void testLookup() {
		IPath aPath = Path.fromPortableString("/foo");
		assertEquals(aPath, IPaths.executablePath(aPath, null));
	}
}
