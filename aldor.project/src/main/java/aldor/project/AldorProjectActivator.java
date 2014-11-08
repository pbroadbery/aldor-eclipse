package aldor.project;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class AldorProjectActivator extends AbstractUIPlugin implements IAldorProjectActivator {
	public static IAldorProjectActivator activator;
	
	public AldorProjectActivator() {
		activator = this;
	}
	
	public final static IAldorProjectActivator getDefault() {
		return activator;
	}
	
	// For testing
	public final static void setDefault(IAldorProjectActivator activator1) {
		activator = activator1;
	}
	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}
	
}
