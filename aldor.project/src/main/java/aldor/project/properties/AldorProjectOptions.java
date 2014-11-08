package aldor.project.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.project.builder.AldorNature;

public class AldorProjectOptions {
	final private static AldorPreferenceModel preferenceModel = new AldorPreferenceModel();
	private AldorPreferenceUIFields uiFields;
	final private Map<AldorPreference<?>, Object> valueForPreference;
	
	public AldorProjectOptions() {
		valueForPreference = new HashMap<>();
	}

	public void load(IProject project) {
		IScopeContext context = new ProjectScope(project);
		try {
			AldorNature nature = (AldorNature) project.getNature(AldorNature.NATURE_ID);
			uiFields = nature.uiFields();
		} catch (CoreException e) {
		}
		Preferences projectPreferences = context.getNode(AldorNature.NATURE_ID);
		load(projectPreferences);
	}

	public void load(Preferences projectPreferences) {
		for (AldorPreference<?> preference: preferenceModel.all()) {
			String preferenceValue = projectPreferences.get(preference.name(), null);
		
			valueForPreference.put(preference, preference.decode(preferenceValue));
		}
	}
	
	public void save(IProject project) throws BackingStoreException {
		IScopeContext context = new ProjectScope(project);

		Preferences projectPreferences = context.getNode(AldorNature.NATURE_ID);

		save(projectPreferences);
	}

	public void save(Preferences projectPreferences) throws BackingStoreException {
		for (AldorPreference<?> preference: preferenceModel.all()) {
			storePreference(projectPreferences, preference);
		}
		projectPreferences.flush();
	}
	
	private <T> void storePreference(Preferences projectPreferences,
			AldorPreference<T> preference) {
		T value = preference.clss().cast(valueForPreference.get(preference));
		if (value == null) {
			projectPreferences.remove(preference.name());
		}
		else {
			projectPreferences.put(preference.name(), preference.encode(value));
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
