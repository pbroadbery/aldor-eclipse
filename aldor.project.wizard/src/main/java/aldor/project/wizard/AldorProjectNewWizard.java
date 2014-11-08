package aldor.project.wizard;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.builder.AldorProjectSupport;
import aldor.project.properties.AldorPreferenceUIField;
import aldor.project.properties.AldorPreferenceUIFields;

public class AldorProjectNewWizard extends Wizard implements INewWizard, IExecutableExtension {
	private static final String PAGE_NAME = "Aldor Project Wizard";
	private static final String WIZARD_NAME = "Aldor Project";
	private WizardNewProjectCreationPage _pageOne;
	private WizardAldorProjectSettingsPage _pageTwo;
	private IConfigurationElement _configurationElement;

	private IPageChangedListener pageChangeListener = new IPageChangedListener() {

		@Override
		public void pageChanged(PageChangedEvent event) {
			_pageTwo.updateFieldValues();
		}};
	public AldorProjectNewWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	@Override
	public boolean performFinish() {
	    String name = _pageOne.getProjectName();
	    URI location = null;
	    if (!_pageOne.useDefaults()) {
	        location = _pageOne.getLocationURI();
	    } // else location == null
	    _pageTwo.updateFieldValues();
	    IPreferenceStore preferences = _pageTwo.getPreferences();
	    AldorProjectSupport.createProject(name, location, preferences);
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
	    _pageTwo = new WizardAldorProjectSettingsPage();
		addPage(_pageTwo);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		if (wizardContainer instanceof WizardDialog) {
			WizardDialog dialog = (WizardDialog) wizardContainer;
			dialog.addPageChangedListener(pageChangeListener);
		}
	}

	class WizardAldorProjectSettingsPage extends FieldWizardPage {
		private AldorPreferenceUIFields uiFields;

		protected WizardAldorProjectSettingsPage() {
			super("Aldor Details");
			uiFields = new AldorPreferenceUIFields(_pageOne.getProjectName());
		}

		public void updateFieldValues() {
			uiFields = new AldorPreferenceUIFields(_pageOne.getProjectName());
			for (AldorPreferenceUIField<?> field: uiFields.all() ) {
				if (field.defaultStringValue() != null)
					getPreferences().setDefault(field.name(), field.defaultStringValue());
			}
			super.loadDefaults();
		}

		@Override
		protected void createFields(Composite parent) {
			AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();
			AldorPreferenceUIField<?> aldorPathUI;
			aldorPathUI = uiFields.uiFieldForPreference(preferenceModel.executableLocation);
			final FieldEditor aldorPath = aldorPathUI.fieldEditor(parent);
			addFieldEditor(aldorPath);

			AldorPreferenceUIField<?> aldorOptionsUI = uiFields.uiFieldForPreference(preferenceModel.aldorOptions);
			final StringFieldEditor aldorOptions= new StringFieldEditor(aldorOptionsUI.name(), aldorOptionsUI.name(), getFieldEditorParent());
			addFieldEditor(aldorOptions);


			AldorPreferenceUIField<?> intermediateFileLocationUI = uiFields.uiFieldForPreference(preferenceModel.intermediateFileLocation);
			final StringFieldEditor intermediateFileLocation= new StringFieldEditor(intermediateFileLocationUI.name(), intermediateFileLocationUI.name(), getFieldEditorParent());
			addFieldEditor(intermediateFileLocation);

			AldorPreferenceUIField<?> libraryNameUI = uiFields.uiFieldForPreference(preferenceModel.targetLibraryName);
			final StringFieldEditor libraryName = new StringFieldEditor(libraryNameUI.name(), libraryNameUI.name(), getFieldEditorParent());
			addFieldEditor(libraryName);

			AldorPreferenceUIField<?> generateJavaUI = uiFields.uiFieldForPreference(preferenceModel.generateJava);
			final BooleanFieldEditor generateJava = new BooleanFieldEditor(generateJavaUI.name(), generateJavaUI.name(), BooleanFieldEditor.SEPARATE_LABEL,
					getFieldEditorParent());
			addFieldEditor(generateJava);

			AldorPreferenceUIField<?> javaLocationUI = uiFields.uiFieldForPreference(preferenceModel.javaFileLocation);
			final StringFieldEditor javaLocation = new StringFieldEditor(javaLocationUI.name(), javaLocationUI.name(), getFieldEditorParent());
			addFieldEditor(javaLocation);

			AldorPreferenceUIField<?> includeFileNameUI = uiFields.uiFieldForPreference(preferenceModel.includeFileName);
			final StringFieldEditor includeFileName = new StringFieldEditor(includeFileNameUI.name(), includeFileNameUI.name(), getFieldEditorParent());
			addFieldEditor(includeFileName);
		}
	}

}
