package aldor.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.builder.AldorProjectSupport;

public class AldorProjectPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	IAdaptable element;

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
		return AldorProjectSupport.getPreferenceStore(project);

	}

	@Override
	protected void createFieldEditors() {
		AldorPreferenceModel preferences = AldorPreferenceModel.instance();
		AldorPreferenceUIField<?> aldorPathUI;
		AldorPreferenceUIFields uiFields = null;
		try {
			uiFields = AldorProjectSupport.uiFields(getProject());
			aldorPathUI = AldorProjectSupport.uiFields(getProject()).uiFieldForPreference(preferences.executableLocation);
		} catch (CoreException e) {
			throw new RuntimeException("Failed to create preferences");
		}
		final FieldEditor aldorPath = aldorPathUI.fieldEditor(getFieldEditorParent());
		addField(aldorPath);

		AldorPreferenceUIField<?> aldorOptionsUI = uiFields.uiFieldForPreference(preferences.aldorOptions);
		final StringFieldEditor aldorOptions= new StringFieldEditor(aldorOptionsUI.name(), aldorOptionsUI.name(), getFieldEditorParent());
		addField(aldorOptions);


		AldorPreferenceUIField<?> intermediateFileLocationUI = uiFields.uiFieldForPreference(preferences.intermediateFileLocation);
		final StringFieldEditor intermediateFileLocation= new StringFieldEditor(intermediateFileLocationUI.name(), intermediateFileLocationUI.name(), getFieldEditorParent());
		addField(intermediateFileLocation);

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
		System.out.println("Adapter: " + getElement() +"  " + getElement().getAdapter(IProject.class));
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		return project;
	}

}