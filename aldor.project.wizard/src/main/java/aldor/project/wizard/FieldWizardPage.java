package aldor.project.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

abstract class FieldWizardPage extends WizardPage {
	private Composite fieldEditorParent;
	private List<FieldEditor> fieldEditors;
	private final IPreferenceStore preferences;

	protected FieldWizardPage(String pageName) {
		super(pageName);
		fieldEditors = new ArrayList<FieldEditor>();
		preferences = new PreferenceStore();
	}

	IPreferenceStore getPreferences() {
		return preferences;
	}

	@Override
	public final void createControl(Composite parent) {
        fieldEditorParent = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fieldEditorParent.setLayout(layout);
        fieldEditorParent.setFont(parent.getFont());

        createFields(fieldEditorParent);

        setControl(fieldEditorParent);

        loadDefaults();
	}

	protected final void loadDefaults() {
		for (FieldEditor fe: fieldEditors) {
			fe.loadDefault();
		}

	}

	abstract protected void createFields(Composite fieldEditorParent);

	void addFieldEditor(FieldEditor editor)  {
		this.fieldEditors.add(editor);
		editor.setPreferenceStore(preferences);
	}

	Composite getFieldEditorParent() {
		return fieldEditorParent;
	}
}