package aldor.project;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.mockito.Mockito;

import aldor.project.properties.AldorProjectPropertyPage;

public class PreferencePage2Test {

	@Test
	public void testFoo() {
		Display display = new Display();
		final Shell shell = new Shell(display);

		final AldorProjectPropertyPage prefs = new AldorProjectPropertyPage();
		prefs.setPreferenceStore(Mockito.mock(IPreferenceStore.class));
		prefs.createControl(shell);
		prefs.getControl().pack();
		shell.pack();

		shell.open();
		/*
		 * display.asyncExec(new Runnable() {
		 * 
		 * @Override public void run() { shell.close(); }});
		 */
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

}
