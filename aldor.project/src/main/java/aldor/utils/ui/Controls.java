package aldor.utils.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import aldor.util.event.EventAdapter;
import aldor.util.event.EventSource;

public class Controls {

	
	public static OptionalStringControl createOptionalString(String currentValue) {
		return new OptionalStringControl(currentValue);
	}

	public static class OptionalStringControl {
		Text text;
		Button checkbox;
		String currentValue;
		EventSource<String> valueSource = new EventSource<>();
		
		public EventSource<String> valueSource() {
			return valueSource;
		}

		OptionalStringControl(String currentValue) {
			valueSource.addListener(new EventAdapter<String>() {

				@Override
				public void onEvent(String event) {
					OptionalStringControl.this.currentValue = event;
				}});
			valueSource.event(currentValue);
		}
		
		
		public Control createControl(Composite parent) {
			final Composite composite = new Composite(parent, SWT.NULL);
			
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);

			///GridData data = new GridData();
			//data.verticalAlignment = GridData.FILL;
			//data.horizontalAlignment = GridData.FILL;
			//composite.setLayoutData(data);
			
			checkbox = new Button(composite, SWT.CHECK);
			checkbox.setText("Use default");
			
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
						raiseEvent(text.getText());
				}
			});
			
			checkbox.addSelectionListener(new SelectionAdapter() {
		
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkbox.getSelection()) {
						text.setEnabled(false);
						raiseEvent(null);
					} else {
						text.setEnabled(true);
						raiseEvent(text.getText());
					}
				}
		
			});
			setValue(currentValue);
			return composite;
		}
		
		protected void raiseEvent(String text) {
			valueSource.event(text);
		}
		
		public void setValue(String value) {
			if (value == null) {
				checkbox.setSelection(true);
				text.setText("");
				text.setEnabled(false);
			}
			else {
				text.setText(value);
				text.setEnabled(true);
				checkbox.setSelection(false);
			}
			
		}

		public String value() {
			return this.currentValue;
		}


	}
	
}

