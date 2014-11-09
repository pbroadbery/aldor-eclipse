package aldor.utils.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import aldor.utils.ui.Controls.OptionalStringControl;

import com.google.common.base.Optional;

public class OptionalFieldEditor extends FieldEditor {

	private final OptionalStringControl optionalStringControl;
	private final String preferenceName;
	private Control control;
	private final String tooltip;

	public OptionalFieldEditor(String preferenceName, String labelText, String labelTooltip, String currentDefaultValue, Composite parent) {
		super();
		super.setPreferenceName(preferenceName);
		super.setLabelText(labelText);
		this.tooltip = labelTooltip;
		this.preferenceName = preferenceName;
		optionalStringControl = Controls.createOptionalString(null, currentDefaultValue);
		this.createControl(parent);
	}

	@Override
	protected final void adjustForNumColumns(int numColumns) {
		System.out.println("Number of columns: " + numColumns);
        GridData gd = (GridData) control.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
	}

	@Override
	protected final void doFillIntoGrid(Composite parent, int numColumns) {
		Label label = getLabelControl(parent);
		if (this.tooltip != null)
			label.setToolTipText(this.tooltip);
		control = optionalStringControl.createControl(parent);
		optionalStringControl.setValue(Optional.<String>absent());

        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        control.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		boolean isSet = this.getPreferenceStore().getBoolean(isSetPreferenceName());
		if (isSet) {
			String value = this.getPreferenceStore().getString(preferenceName);
			Assert.isTrue(value.length() > 0);
			optionalStringControl.setValue(Optional.of(value));
		}
	}

	@Override
	protected void doLoadDefault() {
		optionalStringControl.setValue(Optional.<String>absent());
	}

	@Override
	protected void doStore() {
		if (optionalStringControl.value() == null) {
			throw new RuntimeException("Nope");
		}
		Optional<String> value = optionalStringControl.value();
		this.getPreferenceStore().setValue(isSetPreferenceName(), value.isPresent());
		if (value.isPresent())
			this.getPreferenceStore().setValue(preferenceName, value.get());
		else
			this.getPreferenceStore().setToDefault(preferenceName);
	}

	private String isSetPreferenceName() {
		return preferenceName+"IsSet";
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}

}