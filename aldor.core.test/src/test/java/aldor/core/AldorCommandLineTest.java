package aldor.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import aldor.core.commandline.AldorCommandLine;
import aldor.core.commandline.AldorCommandLine.FileType;

public class AldorCommandLineTest {

	@Test
	public void test() {
		AldorCommandLine commandLine = new AldorCommandLine(new Path("aldor"));
		commandLine.addOutput(FileType.Intermediate, new Path("foo/bar"));
		commandLine.inputFilePath(new Path("abc.def"));
		assertEquals("aldor -Fao=foo/bar abc.def", commandLine.toCommandString());
	}
}
