package aldor.project.test;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import aldor.project.builder.AldorNature;
import aldor.project.properties.AldorProjectPropertyPage;

public class PreferencePage2Test {

	@Test
	public void testFoo() throws CoreException {
		IProject project = EasyMock.createNiceMock(IProject.class);
		AldorNature nature = new AldorNature();
		EasyMock.expect(project.getAdapter(EasyMock.<Class<?>> anyObject())).andReturn(project).anyTimes();
		EasyMock.expect(project.getNature(EasyMock.eq(AldorNature.NATURE_ID))).andReturn(nature).anyTimes();
		final IPreferenceStore prefStore = EasyMock.createMock(IPreferenceStore.class);
		EasyMock.expect(prefStore.getString(EasyMock.<String> anyObject())).andReturn("").anyTimes();
		EasyMock.expect(prefStore.getBoolean(EasyMock.<String> anyObject())).andReturn(false).anyTimes();
		EasyMock.replay(project, prefStore);

		nature.setProject(project);
		Display display = new Display();
		final Shell shell = new Shell(display);

		final AldorProjectPropertyPage prefs = new AldorProjectPropertyPage();
		prefs.setElement(project);
		prefs.setPreferenceStore(prefStore);
		prefs.createControl(shell);
		prefs.getControl().pack();
		shell.pack();

		shell.open();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				shell.close();
			}
		});
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	@Test
	public void test2() {
		IProject p = EasyMock.createNiceMock(IProject.class);
		EasyMock.expect(p.getAdapter(EasyMock.<Class<?>> anyObject())).andReturn(p).anyTimes();
		EasyMock.replay(p);
		Assert.assertEquals(p, p.getAdapter(IProject.class));
	}

}
