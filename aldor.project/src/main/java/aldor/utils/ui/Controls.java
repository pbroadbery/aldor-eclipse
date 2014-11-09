package aldor.utils.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import aldor.util.event.EventAdapter;
import aldor.util.event.EventSource;

import com.google.common.base.Optional;

public class Controls {


	public static OptionalStringControl createOptionalString(String currentValue, String defaultValue) {
		return new OptionalStringControl(currentValue, defaultValue);
	}

	public static class OptionalStringControl {
		private Text text;
		private Button checkbox;
		private Optional<String> currentValue;
		final private EventSource<Optional<String>> valueSource = new EventSource<>();
		final private String defaultValue;

		public EventSource<Optional<String>> valueSource() {
			return valueSource;
		}

		OptionalStringControl(String currentValue, String defaultValue) {
			valueSource.addListener(new EventAdapter<Optional<String>>() {
				@Override
				public void onEvent(Optional<String> event) {
					OptionalStringControl.this.currentValue = event;
				}});
			this.valueSource.event(Optional.fromNullable(currentValue));
			this.defaultValue = defaultValue;
		}


		public Control createControl(Composite parent) {
			final Composite composite = new Composite(parent, SWT.NULL);

			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.numColumns = 1;
			composite.setLayout(layout);

			Composite topBox = new Composite(composite, SWT.NONE);
			RowLayout rowLayout = new RowLayout();
			rowLayout.center = true;
			topBox.setLayout(rowLayout);

			checkbox = new Button(topBox, SWT.CHECK);
			checkbox.setText("Use default");

			Label defaultLabel = new Label(topBox, SWT.NONE);
			if (defaultValue != null)
				defaultLabel.setText("("+defaultValue + ")");
			text = new Text(composite, SWT.SINGLE);
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			text.setLayoutData(data);

			text.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!checkbox.getSelection())
						raiseEvent(Optional.fromNullable(text.getText()));
				}
			});

			checkbox.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkbox.getSelection()) {
						text.setEnabled(false);
						raiseEvent(Optional.<String>absent());
					} else {
						text.setEnabled(true);
						raiseEvent(Optional.fromNullable(text.getText()));
					}
				}

			});
			setValue(currentValue);
			return composite;
		}

		protected void raiseEvent(Optional<String> text) {
			valueSource.event(text);
		}

		public void setValue(Optional<String> value) {
			if (!value.isPresent()) {
				checkbox.setSelection(true);
				text.setText("");
				text.setEnabled(false);
			}
			else {
				text.setText(value.get());
				text.setEnabled(true);
				checkbox.setSelection(false);
			}

		}

		public Optional<String> value() {
			Assert.isNotNull(currentValue);
			return this.currentValue;
		}


	}

}

