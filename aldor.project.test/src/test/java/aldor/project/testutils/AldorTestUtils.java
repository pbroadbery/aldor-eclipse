package aldor.project.testutils;

import org.eclipse.jface.preference.IPreferenceStore;

import aldor.project.AldorProjectActivator;
import aldor.project.IAldorProjectActivator;

public class AldorTestUtils {

	/** Set the Project activator's default preference store to the supplied value.
	 * Note that it is assumed that the Activator is only wanted for its preferences.
	 * @param localPreferenceStore
	 * @return
	 */
	public static IAldorProjectActivator createAldorActivator(final IPreferenceStore localPreferenceStore) {
		IAldorProjectActivator activator = new IAldorProjectActivator() {

			@Override
			public IPreferenceStore getPreferenceStore() {
				return localPreferenceStore;
			}};
		AldorProjectActivator.setDefault(activator);
		return activator;
	}
}

