package aldor.project.properties.test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Assert;
import org.junit.Test;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.IAldorProjectActivator;
import aldor.project.preferences.AldorPreferenceInitializer;
import aldor.project.properties.AldorPreferenceUIField;
import aldor.project.properties.AldorPreferenceUIFields;
import aldor.project.testutils.AldorTestUtils;

public class AldorPreferenceUIFieldsTest {
	static final private AldorPreferenceModel prefModel = AldorPreferenceModel.instance();

	@Test
	public void testOne() {
		final IPreferenceStore localPreferenceStore = new PreferenceStore();
		IAldorProjectActivator activator = AldorTestUtils.createAldorActivator(localPreferenceStore);

		localPreferenceStore.setDefault(prefModel.executableLocation.name(), "/aldor-v1");
		Assert.assertNotNull(activator.getPreferenceStore());
		AldorPreferenceUIFields uiFields = new AldorPreferenceUIFields("bob");
		AldorPreferenceUIField<IPath> location = uiFields.uiFieldForPreference(prefModel.executableLocation);

		Assert.assertEquals("/aldor-v1", location.defaultStringValue());

		localPreferenceStore.setValue(prefModel.executableLocation.name(), "/aldor-v2");
		Assert.assertEquals("/aldor-v2", location.defaultStringValue());
	}

	@Test
	public void testAldorExecutableIsTheDefault() {
		final IPreferenceStore localPreferenceStore = new PreferenceStore();
		AldorTestUtils.createAldorActivator(localPreferenceStore);

		AldorPreferenceInitializer init = new AldorPreferenceInitializer();
		init.initializeDefaultPreferences();

		AldorPreferenceUIFields uiFields = new AldorPreferenceUIFields("bob");

		Assert.assertEquals("aldor", uiFields.uiFieldForPreference(prefModel.executableLocation).defaultStringValue());
	}


}
