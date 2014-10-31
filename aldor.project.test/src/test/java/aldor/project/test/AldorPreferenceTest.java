package aldor.project.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.properties.AldorPreferenceUIField;
import aldor.project.properties.AldorPreferenceUIFields;
import aldor.project.properties.AldorProjectOptions;

public class AldorPreferenceTest {
	private static final AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();

	@Test
	public void test0() {
		AldorPreferenceUIFields fields = new AldorPreferenceUIFields(Mockito.mock(IProject.class));
		AldorPreferenceUIField<IPath> field = fields.uiFieldForPreference(preferenceModel.executableLocation);
		Assert.assertNotNull(field);
		Assert.assertNull(field.defaultValue());

		AldorPreferenceUIField<Boolean> field2 = fields.uiFieldForPreference(preferenceModel.generateJava);
		Assert.assertNotNull(field2);
		Assert.assertNotNull(field2.defaultStringValue());
	}
	
	@Test
	public void test1() {
		AldorProjectOptions options = new AldorProjectOptions();
		options.put(preferenceModel.executableLocation, new Path("/tmp/foo"));
		Preferences prefs = Mockito.mock(Preferences.class);
		try {
			options.save(prefs);
			Mockito.verify(prefs).put(preferenceModel.executableLocation.name(), "/tmp/foo");
			Mockito.verify(prefs).flush();
		} catch (BackingStoreException e) {
			fail();
		}
	}
	
	@Test
	public void test2() {
		Preferences prefs = Mockito.mock(Preferences.class);
		final Map<String, String> map = new HashMap<>();
		Mockito.when(prefs.get(Matchers.anyString(), Matchers.anyString())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock arg0) throws Throwable {
				return map.get(arg0.getArguments()[0]);
			}});
		map.put(preferenceModel.executableLocation.name(), "/tmp/foo");
		
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(prefs);
		assertEquals((IPath) new Path("/tmp/foo"), options.get(preferenceModel.executableLocation));
	}
	
}
