package aldor.project.test.properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Assert;
import org.junit.Test;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.AldorProjectActivator;
import aldor.project.IAldorProjectActivator;
import aldor.project.properties.AldorPreferenceUIField;
import aldor.project.properties.AldorPreferenceUIFields;

public class AldorPreferenceUIFieldsTest {
	static final private AldorPreferenceModel prefModel = AldorPreferenceModel.instance();
	
	@Test
	public void testOne() {
		final IPreferenceStore localPreferenceStore = new PreferenceStore();
		IAldorProjectActivator activator = new IAldorProjectActivator() {

			@Override
			public IPreferenceStore getPreferenceStore() {
				return localPreferenceStore;
			}};
		AldorProjectActivator.setDefault(activator);
		localPreferenceStore.setDefault(prefModel.executableLocation.name(), "/aldor-v1");
		Assert.assertNotNull(activator.getPreferenceStore());
		AldorPreferenceUIFields uiFields = new AldorPreferenceUIFields("bob");
		AldorPreferenceUIField<IPath> location = uiFields.uiFieldForPreference(prefModel.executableLocation);

		Assert.assertEquals("/aldor-v1", location.defaultStringValue());
	
		localPreferenceStore.setValue(prefModel.executableLocation.name(), "/aldor-v2");
		Assert.assertEquals("/aldor-v2", location.defaultStringValue());
	}
}
