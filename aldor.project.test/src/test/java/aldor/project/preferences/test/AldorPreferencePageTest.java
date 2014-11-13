package aldor.project.preferences.test;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Assert;
import org.junit.Test;

import aldor.project.preferences.AldorPreferencePage;
import aldor.project.testutils.AldorTestUtils;

public class AldorPreferencePageTest {
	@Test
	public void testProjectPreferencePage()
	{
		final IPreferenceStore localPreferenceStore = new PreferenceStore();
		AldorTestUtils.createAldorActivator(localPreferenceStore);

		AldorPreferencePage prefPage = new AldorPreferencePage();
		IPreferenceStore store = prefPage.getPreferenceStore();
		Assert.assertSame(localPreferenceStore, store);
	}

}

