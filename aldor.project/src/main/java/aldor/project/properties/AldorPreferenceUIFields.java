package aldor.project.properties;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;

public class AldorPreferenceUIFields {
	private Set<AldorPreferenceUIField<?>> uiFields;
	private AldorPreferenceModel prefModel = AldorPreferenceModel.instance();

	@SuppressWarnings("unchecked")
	public <T> AldorPreferenceUIField<T> uiFieldForPreference(AldorPreference<T> preference) {
		for (AldorPreferenceUIField<?> uiField: uiFields) {
			if (uiField.item.equals(preference)) {
				return (AldorPreferenceUIField<T>) uiField;
			}
		}
		throw new RuntimeException(""+preference.name());
	}
	
	public AldorPreferenceUIFields(IProject project) {
		uiFields = new HashSet<>();
		uiFields.add(new AldorPreferenceUIField<>(prefModel.aldorOptions, "", "Aldor Options"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.executableLocation, defaultExecutablePath(), "Aldor Location"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.intermediateFileLocation, defaultIntermediateFileLocation(), "Intermediate File Location"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.javaFileLocation, defaultJavaFileLocation(), "Java File Location"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.generateJava, false, "Generate Java"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.targetLibraryName, project.getName(), "Target library name"));
	}

	private IPath defaultExecutablePath() {
		return null;
	}

	private IPath defaultIntermediateFileLocation() {
		return Path.fromPortableString("aldor-generated/ao");
	}


	private IPath defaultJavaFileLocation() {
		return Path.fromPortableString("aldor-generated/java");
	}

	public Iterable<AldorPreferenceUIField<?>> all() {
		return uiFields;
	}

}
