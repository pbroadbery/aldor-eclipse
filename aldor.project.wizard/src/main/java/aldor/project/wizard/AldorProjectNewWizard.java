package aldor.project.wizard;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import aldor.project.builder.AldorProjectSupport;

public class AldorProjectNewWizard extends Wizard implements INewWizard, IExecutableExtension {
	private static final String PAGE_NAME = "Aldor Project Wizard";
	private static final String WIZARD_NAME = "Aldor Project";
	private WizardNewProjectCreationPage _pageOne;
	private IConfigurationElement _configurationElement;
	public AldorProjectNewWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
	    String name = _pageOne.getProjectName();
	    URI location = null;
	    if (!_pageOne.useDefaults()) {
	        location = _pageOne.getLocationURI();
	    } // else location == null
	 
	    AldorProjectSupport.createProject(name, location);
	    BasicNewProjectResourceWizard.updatePerspective(_configurationElement);

	    return true;
	}
	
	@Override
	public void addPages() {
	    super.addPages();
	 
	    _pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
	    _pageOne.setTitle("Aldor Project Settings");
	    _pageOne.setDescription("Create an Aldor project.  You'll love it.");
	 
	    addPage(_pageOne);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}

}
