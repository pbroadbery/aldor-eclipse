package aldor.utils.ui;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import aldor.utils.ui.Controls.OptionalStringControl;

public class OptionalFieldEditor extends FieldEditor {

	OptionalStringControl optionalStringControl = Controls.createOptionalString(null);
	private String preferenceName;
	private Control control;

	public OptionalFieldEditor(String labelText, String preferenceName, Composite parent) {
		super();
		super.setPreferenceName(preferenceName);
		super.setLabelText(labelText);
		
		this.preferenceName = preferenceName;
		this.createControl(parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		System.out.println("Number of columns: " + numColumns);
        GridData gd = (GridData) control.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		System.out.println("Filling in grid: " + numColumns);
		getLabelControl(parent);

		control = optionalStringControl.createControl(parent);
		optionalStringControl.setValue(null);

        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        control.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		String value = this.getPreferenceStore().getString(preferenceName);
		optionalStringControl.setValue(value);
	}

	@Override
	protected void doLoadDefault() {
		optionalStringControl.setValue(null);
	}

	@Override
	protected void doStore() {
		this.getPreferenceStore().setValue(preferenceName, optionalStringControl.value());
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}

}