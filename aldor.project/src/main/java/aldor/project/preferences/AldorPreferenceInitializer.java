package aldor.project.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import aldor.project.AldorProjectActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		AldorProjectActivator.getDefault().getPreferenceStore();
	}

}
