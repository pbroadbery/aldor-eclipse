package aldor.project.test;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;
import org.junit.Test;

import com.google.common.base.Optional;

import aldor.util.event.EventAdapter;
import aldor.utils.ui.Controls;
import aldor.utils.ui.Controls.OptionalStringControl;

public class PreferencePageTest {
	@Test
	public void testFoo() {
		Display display = new Display();
		final Shell shell = new Shell(display);
	
		final PrefPage prefs = new PrefPage();
		
		prefs.createControl(shell);
		prefs.getControl().pack();
		shell.pack();
		
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.close();
			}});
		
		//shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();
	}
	

	static class PrefPage extends PropertyPage {
		OptionalStringControl optionalString;
		
		@Override
		protected Control createContents(Composite parent) {
			optionalString = Controls.createOptionalString("");
			optionalString.valueSource().addListener(new EventAdapter<Optional<String>>() {
				@Override
				public void onEvent(Optional<String> event) {
					System.out.println("Event [" + event + "]");
				}
			});
			return optionalString.createControl(parent);
		}
		
	}
}
