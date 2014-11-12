package aldor.project.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.AldorProjectActivator;

/**
 * Class used to initialize default preference values.
 */
public class AldorPreferenceInitializer extends AbstractPreferenceInitializer {
	static final AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore ps = AldorProjectActivator.getDefault().getPreferenceStore();
		ps.setDefault(preferenceModel.executableLocation.name(), "aldor");
	}

}
