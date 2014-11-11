package aldor.project.properties;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.project.AldorProjectActivator;
import aldor.util.SafeCallable;
import aldor.utils.ui.OptionalFieldEditor;

import com.google.common.base.Function;

public class AldorPreferenceUIFields {
	private Set<AldorPreferenceUIField<?>> uiFields;
	private AldorPreferenceModel prefModel = AldorPreferenceModel.instance();

	@SuppressWarnings("unchecked")
	public <T> AldorPreferenceUIField<T> uiFieldForPreference(AldorPreference<T> preference) {
		for (AldorPreferenceUIField<?> uiField : uiFields) {
			if (uiField.item().equals(preference)) {
				return (AldorPreferenceUIField<T>) uiField;
			}
		}
		throw new RuntimeException("" + preference.name());
	}

	public AldorPreferenceUIFields(String projectName) {
		uiFields = new HashSet<>();
		uiFields.add(stringUIField(prefModel.aldorOptions, "Aldor Options", ""));
		uiFields.add(optionalStringUIField(prefModel.executableLocation, "Aldor Location", defaultExecutablePath()));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.intermediateFileLocation, "Intermediate File Location",
				defaultIntermediateFileLocation()));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.javaFileLocation, "Java File Location", defaultJavaFileLocation()));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.generateJava, "Generate Java", false));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.targetLibraryName, "Target library name", projectName));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.includeFileName, "Include File Name", projectName + ".as"));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.aldorSourceFilePath, "Source file path", sourceFilePath()));
		uiFields.add(new AldorPreferenceUIField<>(prefModel.binaryFileLocation, "Binary file path", binaryFilePath()));
	}

	private AldorPreferenceUIField<?> stringUIField(AldorPreference<String> item, String title, String defaultValue) {
		AldorPreferenceUIField<String> field = new AldorPreferenceUIField<>(item, title, defaultValue);
		field.fieldEditor(stringEditor(field));
		return field;
	}

	private <T> AldorPreferenceUIField<T> optionalStringUIField(AldorPreference<T> item, String title, SafeCallable<T> defaultValue) {
		AldorPreferenceUIField<T> field = new AldorPreferenceUIField<>(item, title, defaultValue);
		field.fieldEditor(optionalStringEditor(field));
		return field;
	}


	private IPath sourceFilePath() {
		return Path.fromPortableString("src/aldor");
	}


	private IPath binaryFilePath() {
		return Path.fromPortableString("bin");
	}

	private SafeCallable<IPath> defaultExecutablePath() {
		return new SafeCallable<IPath>() {

			@Override
			public IPath call() {
				IPreferenceStore preferences = AldorProjectActivator.getDefault().getPreferenceStore();
				return prefModel.executableLocation.preferenceValue(preferences);
			}
		};
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

	private <T> Function<Composite, FieldEditor> stringEditor(final AldorPreferenceUIField<T> field) {
		return new Function<Composite, FieldEditor>() {
			@Override
			public FieldEditor apply(Composite parent) {
				return new StringFieldEditor(field.name(), field.title(), parent);
			}
		};

	}

	private <T> Function<Composite, FieldEditor> optionalStringEditor(final AldorPreferenceUIField<T> field) {
		return new Function<Composite, FieldEditor>() {
			@Override
			public FieldEditor apply(Composite parent) {
				return new OptionalFieldEditor(field.name(), field.title(), null, field.defaultStringValue(), parent);
			}
		};

	}


}
