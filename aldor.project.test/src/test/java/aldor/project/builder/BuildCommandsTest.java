package aldor.project.builder;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import aldor.core.commandline.AldorCommandLine;
import aldor.project.testutils.AldorTestUtils;
import aldor.project.testutils.Asserts;

public class BuildCommandsTest {

	@Test
	public void testObjectFileNameForIntermediate() throws CoreException {
		IProject project = createSimpleProject();
		BuildCommands buildCommands = new BuildCommands(project);

		IPath objFile = buildCommands.objectFileForIntermediate(Path.fromPortableString("bob/foo.ao"));
		Assert.assertEquals(Path.fromPortableString("bin/foo.o"), objFile);
	}

	@Test
	public void testArchiveFileName() throws CoreException {
		IProject project = createSimpleProject();
		BuildCommands buildCommands = new BuildCommands(project);

		IPath objFile = buildCommands.archiveFileName(Path.fromPortableString("bob/src/foo.ao"));
		Assert.assertEquals(Path.fromPortableString("aldor-generated/ao/libproject_foo.al"), objFile);
	}

	@Test
	public void testIntermediateFileName() throws CoreException {
		IProject project = createSimpleProject();
		BuildCommands buildCommands = new BuildCommands(project);

		IPath objFile = buildCommands.intermediateFileName(Path.fromPortableString("bob/src/foo.as"));
		Assert.assertEquals(Path.fromPortableString("aldor-generated/ao/foo.ao"), objFile);
	}

	@Test
	public void testPrepareFoo() throws CoreException {
		IProject project = createSimpleProject();
		BuildCommands buildCommands = new BuildCommands(project);
		IFile aFile = Mockito.mock(IFile.class);
		Mockito.when(aFile.getFullPath()).thenReturn(Path.fromPortableString("foo.as"));
		Mockito.when(aFile.getLocation()).thenReturn(Path.fromPortableString("foo.as"));
		AldorCommandLine cmd = buildCommands.prepareBuildIntermediateCommandLine(aFile);
		System.out.println("" + cmd.toCommandString());
		List<String> arguments = Arrays.asList(cmd.arguments());
		Asserts.assertContains("-Fao=aldor-generated/ao/foo.ao", arguments);
		Asserts.assertContains("-DBUILD_project", arguments);
	}

	private IProject createSimpleProject() throws CoreException {
		final IPreferenceStore localPreferenceStore = new PreferenceStore();
		AldorTestUtils.createAldorActivator(localPreferenceStore);

		IProject project = Mockito.mock(IProject.class);
		IProjectNature theAldorNature = new AldorNature();

		Mockito.when(project.getName()).thenReturn("project");
		Mockito.when(project.getNature(Mockito.eq(AldorNature.NATURE_ID))).thenReturn(theAldorNature);
		Mockito.when(project.getLocation()).thenReturn(Path.fromPortableString("."));
		theAldorNature.setProject(project);
		return project;
	}

}
