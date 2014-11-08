package aldor.core.project;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

import aldor.util.Strings;

public class AldorPreferenceModel {	
	static final private AldorPreferenceModel instance = new AldorPreferenceModel();
	
	List<AldorPreference<?>> all = new LinkedList<AldorPreference<?>>();

	public final AldorPreference<IPath> executableLocation = new AldorPreference<>(IPath.class, "executableLocation");
	public final AldorPreference<String> aldorOptions = new AldorPreference<String>(String.class, "aldorOptions");
	public final AldorPreference<IPath> intermediateFileLocation = new AldorPreference<>(IPath.class, "intermediateFileLocation");
	public final AldorPreference<Boolean> generateJava= new AldorPreference<>(Boolean.class, "generateJava");
	public final AldorPreference<IPath> javaFileLocation = new AldorPreference<>(IPath.class, "javaFileLocation");
	public final AldorPreference<DependencyStyle> dependencyStyle = new AldorPreference<>(DependencyStyle.class, "dependencyStyle");
	public final AldorPreference<String> targetLibraryName = new AldorPreference<String>(String.class, "targetLibraryName");
	public final AldorPreference<String> includeFileName = new AldorPreference<String>(String.class, "includeFileName");

	public AldorPreference<IPath> aldorSourceFilePath = new AldorPreference<IPath>(IPath.class, "aldorSourceFilePath");

	public AldorPreferenceModel() {
		
	}
	
	public static AldorPreferenceModel instance() {
		return instance;
	}
	
	private void add(AldorPreference<?> aldorPreference) {
		all.add(aldorPreference);
	}

	public class AldorPreference<T> {
		Class<T> clss;
		String name;
		
		AldorPreference(Class<T> clss, String name) {
			this.name = name;
			this.clss = clss;
			add(this);
		}
		
		public String name() {
			return name;
		}
		
		public String encode(T value) {
			return Strings.instance().encode(clss, value);
		}
		
		public T decode(String text) {
			return Strings.instance().decode(clss, text);
		}

		public Class<T> clss() {
			return clss;
		}
		
		@Override
		public boolean equals(Object other) {
			return this.name().equals(((AldorPreference<?>) other).name());
		}
		
		@Override
		public int hashCode() {
			return this.name().hashCode();
		}

		public String preference(IPreferenceStore preferences) {
			return preferences.getString(this.name());
		}

		public T preferenceValue(IPreferenceStore preferences) {
			return decode(preferences.getString(this.name()));
		}

	}
	
	enum DependencyStyle {
		InFile, DependecyFile;
	}

	public List<AldorPreference<?>> all() {
		return all;
	}
	
}
