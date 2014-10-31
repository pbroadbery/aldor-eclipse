package aldor.project.runners.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import aldor.project.runners.AldorRunnerMetaModel.ConfigAttribute;

public class AldorLaunchInterpMainTab extends AbstractLaunchConfigurationTab {
	Text projectName;
	Text fileName;

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			projectName.setText(configuration.getAttribute(ConfigAttribute.RUNNER_Project.text(), ""));
			fileName.setText(configuration.getAttribute(ConfigAttribute.RUNNER_File.text(), ""));
		} catch (CoreException e) {
			throw new RuntimeException("Creating configuration from " + configuration.getName(), e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ConfigAttribute.RUNNER_Project.text(), projectName.getText());
		configuration.setAttribute(ConfigAttribute.RUNNER_File.text(), fileName.getText());
	}

	@Override
	public String getName() {
		return "Interpreter Options";
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(2, true));
		fillParent(comp);
		setControl(comp);
		
	}

	private ModifyListener fBasicModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent evt) {
			scheduleUpdateJob();
		}
	};
	
	private void fillParent(Composite comp) {
		Label lbl;
		lbl = new Label(comp, SWT.NONE);
		lbl.setText("Project");
		projectName = new Text(comp, SWT.NONE);
		projectName.addModifyListener(fBasicModifyListener);

		lbl = new Label(comp, SWT.NONE);
		lbl.setText("File");
		fileName = new Text(comp, SWT.NONE);
		projectName.addModifyListener(fBasicModifyListener);
	}

}
