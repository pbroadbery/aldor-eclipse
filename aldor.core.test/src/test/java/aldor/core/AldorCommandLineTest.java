package aldor.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import aldor.core.commandline.AldorCommandLine;
import aldor.core.commandline.AldorCommandLine.FileType;
import aldor.core.commandline.AldorCommandLine.RunType;

public class AldorCommandLineTest {

	@Test
	public void test() {
		AldorCommandLine commandLine = new AldorCommandLine(Path.fromPortableString("aldor"));
		commandLine.addOutput(FileType.Intermediate, new Path("foo/bar"));
		commandLine.inputFilePath(new Path("abc.def"));
		assertEquals("aldor -Fao=foo/bar abc.def", commandLine.toCommandString());
	}

	@Test
	public void test2() {
		AldorCommandLine commandLine = new AldorCommandLine(new Path("aldor"));
		commandLine.addRunType(RunType.Interp);
		commandLine.inputFilePath(new Path("abc.def"));
		assertEquals("aldor -Ginterp abc.def", commandLine.toCommandString());
	}

}
