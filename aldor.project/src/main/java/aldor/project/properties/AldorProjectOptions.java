package aldor.project.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.project.builder.AldorProjectSupport;

public class AldorProjectOptions {
	final private static AldorPreferenceModel preferenceModel = new AldorPreferenceModel();
	private AldorPreferenceUIFields uiFields;
	final private Map<AldorPreference<?>, Object> valueForPreference;

	public AldorProjectOptions() {
		valueForPreference = new HashMap<>();
	}

	public void load(IProject project) {
		IPreferenceStore projectPreferences = AldorProjectSupport.getPreferenceStore(project);
		load(projectPreferences);
	}

	public void load(IPreferenceStore projectPreferences) {
		for (AldorPreference<?> preference: preferenceModel.all()) {
			String preferenceValue = preference.preference(projectPreferences);

			try {
				valueForPreference.put(preference, preference.decode(preferenceValue));
			}
			catch (RuntimeException e) {

			}
		}
	}

	public <T> T get(AldorPreference<T> preference) {
		return preference.clss().cast(valueForPreference.get(preference));
	}

	public <T> T getOrDefault(AldorPreference<T> preference) {
		T value = get(preference);
		if (value == null) {
			return uiFields.<T>uiFieldForPreference(preference).defaultValue();
		}
		return value;
	}
}
