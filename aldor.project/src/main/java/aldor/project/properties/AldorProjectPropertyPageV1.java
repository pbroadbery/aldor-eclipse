package aldor.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.project.builder.AldorNature;
import aldor.util.event.EventAdapter;
import aldor.utils.ui.Controls;
import aldor.utils.ui.Controls.OptionalStringControl;

public class AldorProjectPropertyPageV1 extends PropertyPage {

	static final AldorPreferenceModel preferences = AldorPreferenceModel.instance();
	private AldorProjectOptions options = new AldorProjectOptions();

	public AldorProjectPropertyPageV1() {
		super();
		this.options = new AldorProjectOptions();
	}

	private void addFirstSection(Composite parent) throws CoreException {
		Composite composite = createDefaultComposite(parent);
		AldorPreferenceUIFields uiFields = getAldorNature().uiFields();
		AldorPreferenceUIField<IPath> executableLocationField = uiFields.uiFieldForPreference(preferences.executableLocation);
		Label executablePathLabel = new Label(composite, SWT.NONE);
		executablePathLabel.setText(executableLocationField.title());

		// Path text field
		String value = initialValueForPreference(preferences.executableLocation);
		OptionalStringControl control = Controls.createOptionalString(value);
		
		Control controlWidget = control.createControl(composite);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = super.convertWidthInCharsToPixels(50);
		controlWidget.setLayoutData(gd);
		control.valueSource().addListener(new EventAdapter<String>() {

			@Override
			public void onEvent(String event) {
				options.put(preferences.executableLocation, preferences.executableLocation.decode(event));
			}
		});
		
	}

	private <T> String initialValueForPreference(final AldorPreference<T> thePreference) throws CoreException {
		AldorPreferenceUIFields uiFields = getAldorNature().uiFields();
		AldorPreferenceUIField<T> uifield = uiFields.uiFieldForPreference(thePreference);
		String value = options.get(thePreference) == null ? uifield.defaultStringValue(): thePreference.encode(options.get(thePreference));
		return value;
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) throws CoreException {
		AldorPreferenceUIFields uiFields = getAldorNature().uiFields();
		Composite composite = createDefaultComposite(parent);
		
		// Label
		AldorPreferenceUIField<Boolean> generateJavaField = uiFields.uiFieldForPreference(preferences.generateJava);
		Label generateJavaPathLabel = new Label(composite, SWT.NONE);
		generateJavaPathLabel.setText(generateJavaField.title());

		// Path
		final Text generateJavaValueText = new Text(composite, SWT.SINGLE);
		generateJavaValueText.setText(initialValueForPreference(preferences.generateJava));
		generateJavaValueText.addModifyListener(modificationListenerForPreference(preferences.generateJava, generateJavaValueText));
		// Label for path field
		AldorPreferenceUIField<IPath> intermediateFileLocationField = uiFields.uiFieldForPreference(preferences.intermediateFileLocation);
		Label executablePathLabel = new Label(composite, SWT.NONE);
		executablePathLabel.setText(intermediateFileLocationField.title());

		// Path text field
		Text executablePathValueText = new Text(composite, SWT.SINGLE);
		executablePathValueText.setText(initialValueForPreference(preferences.intermediateFileLocation));
		executablePathValueText.addModifyListener(modificationListenerForPreference(preferences.intermediateFileLocation, executablePathValueText));

		// Label for path field
		AldorPreferenceUIField<IPath> javaFileLocationField = uiFields.uiFieldForPreference(preferences.javaFileLocation);
		Label javaPathLabel = new Label(composite, SWT.NONE);
		javaPathLabel.setText(javaFileLocationField.title());

		// Path text field
		Text javaPathValueText = new Text(composite, SWT.SINGLE);
		javaPathValueText.setText(initialValueForPreference(preferences.javaFileLocation));
		javaPathValueText.addModifyListener(modificationListenerForPreference(preferences.javaFileLocation, javaPathValueText));

		// Label for library name
		AldorPreferenceUIField<String> libraryName = uiFields.uiFieldForPreference(preferences.targetLibraryName);
		Label libraryNameLabel = new Label(composite, SWT.NONE);
		libraryNameLabel.setText(libraryName.title());

		// Path text field
		Text libraryNameValueText = new Text(composite, SWT.SINGLE);
		libraryNameValueText.setText(initialValueForPreference(preferences.targetLibraryName));
		libraryNameValueText.addModifyListener(modificationListenerForPreference(preferences.targetLibraryName, libraryNameValueText));
	}

	private <T> ModifyListener modificationListenerForPreference(final AldorPreference<T> thePreference, final Text generateJavaValueText) {
		return new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				options.put(thePreference, thePreference.decode(generateJavaValueText.getText()));
			}};
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		options.load(project);

		Composite composite = new Composite(parent, SWT.NONE);
		try {
			GridLayout layout = new GridLayout();
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL);
			data.grabExcessHorizontalSpace = true;
			composite.setLayoutData(data);

			addFirstSection(composite);
			addSeparator(composite);
			addSecondSection(composite);
			return composite;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return composite;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return composite;
		}
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}


	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IProject project = getProject();
		
		try {
			this.options.save(project);
		} catch (BackingStoreException e) {
			return false;
		}

		return true;
	}

	private IProject getProject() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		return project;
	}
	
	private AldorNature getAldorNature() throws CoreException {
		return (AldorNature) getProject().getNature(AldorNature.NATURE_ID);
	}

}