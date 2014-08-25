package aldor.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.builder.AldorNature;
import aldor.utils.ui.OptionalFieldEditor;

public class AldorProjectPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	IAdaptable element;
	private AldorPreferenceUIFields uiFields;

	public AldorProjectPropertyPage() {
		super(GRID);
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		IProject project = getProject();
		try {
			AldorPreferenceUIFields uiFields = uiFields();
			ProjectScope scope = new ProjectScope(project);
			ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(scope, AldorNature.NATURE_ID);

			for (AldorPreferenceUIField<?> field : uiFields.all()) {
				String defaultStringValue = field.defaultStringValue();
				if (defaultStringValue != null) {
					preferenceStore.setDefault(field.name(), defaultStringValue);
				}
			}

			return preferenceStore;
		} catch (CoreException e) {
			return null;
		}

	}

	private AldorPreferenceUIFields uiFields() throws CoreException {
		if (this.uiFields == null) {
			this.uiFields = getAldorNature().uiFields();
		}
		return uiFields;
	}

	@Override
	protected void createFieldEditors() {
		AldorPreferenceModel preferences = AldorPreferenceModel.instance();
		AldorPreferenceUIField<?> aldorPathUI;
		try {
			aldorPathUI = uiFields().uiFieldForPreference(preferences.executableLocation);
		} catch (CoreException e) {
			throw new RuntimeException("Failed to create preferences");
		}
		final OptionalFieldEditor aldorPath = new OptionalFieldEditor(aldorPathUI.title, aldorPathUI.name(), getFieldEditorParent());
		addField(aldorPath);

		AldorPreferenceUIField<?> libraryNameUI = uiFields.uiFieldForPreference(preferences.targetLibraryName);
		final StringFieldEditor libraryName = new StringFieldEditor(libraryNameUI.name(), libraryNameUI.name(), getFieldEditorParent());
		addField(libraryName);

		AldorPreferenceUIField<?> generateJavaUI = uiFields.uiFieldForPreference(preferences.generateJava);
		final BooleanFieldEditor generateJava = new BooleanFieldEditor(generateJavaUI.name(), generateJavaUI.name(), BooleanFieldEditor.SEPARATE_LABEL,
				getFieldEditorParent());
		addField(generateJava);

		AldorPreferenceUIField<?> javaLocationUI = uiFields.uiFieldForPreference(preferences.javaFileLocation);
		final StringFieldEditor javaLocation = new StringFieldEditor(javaLocationUI.name(), javaLocationUI.name(), getFieldEditorParent());
		addField(javaLocation);

	}

	// Might be handy for some extra layout
	@SuppressWarnings("unused")
	private void createSpace() {
		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
	}

	private IProject getProject() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		return project;
	}

	private AldorNature getAldorNature() throws CoreException {
		return (AldorNature) getProject().getNature(AldorNature.NATURE_ID);
	}

}