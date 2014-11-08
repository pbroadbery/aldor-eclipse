package aldor.project.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.AldorProjectActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	final static private AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = AldorProjectActivator.getDefault().getPreferenceStore();
	}

}
