package aldor.project.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import aldor.core.project.AldorPreferenceModel;
import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.project.properties.AldorPreferenceUIField;
import aldor.project.properties.AldorPreferenceUIFields;

public class AldorProjectSupport {
	private static final AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();

	/**
	 * For this project we need to: - create the default Eclipse project - add
	 * the custom project nature - create the folder structure
	 *
	 * @param projectName
	 * @param location
	 * @param preferences
	 * @param natureId
	 * @return
	 */
	public static IProject createProject(String projectName, URI location, IPreferenceStore preferences) {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0);

        IProject project = createBaseProject(projectName, location);
        try {
            addNature(project);

            String[] paths = { "src/aldor",
            					"bin",
            					preferenceModel.intermediateFileLocation.preference(preferences),
            					preferenceModel.javaFileLocation.preference(preferences),
            					preferenceModel.aldorSourceFilePath.preference(preferences),
            					};
            addToProjectStructure(project, paths);
            createIncludeFile(project,
            					preferenceModel.aldorSourceFilePath.preference(preferences),
            					preferenceModel.includeFileName.preference(preferences));
            setProjectPreferences(project, preferences);
        } catch (CoreException e) {
            e.printStackTrace();
            project = null;
        }

        return project;
    }

	private static void setProjectPreferences(final IProject project, final IPreferenceStore preferences) {
		IPreferenceStore projectPreferences = AldorProjectSupport.getPreferenceStore(project);
		for (AldorPreference<?> preference: preferenceModel.all()) {
			projectPreferences.setValue(preference.name(), preference.preference(preferences));
		}
		savePreferenceStore(projectPreferences);
	}

	private static void createIncludeFile(IProject project, String path, String includeFileName) {
		if (includeFileName.length() == 0) {
			return;
		}
		IFolder folder = project.getFolder(Path.fromPortableString(path));
		IFile file = folder.getFile(includeFileName);
		String text = "-- Include file for the " + project.getName() +" project."
					+ "-- it is expected that it will be included in all source files that\n"
					+ "-- make up this project.\n"
					+ "--NOBUILD\n"
					+ "#if BUILD_" + project.getName() + "\n"
					+ "#else" + "\n"
					+ "#library " + project.getName() + " \"lib"+project.getName() + ".al\"" + "\n"
					+ "#endif" + "\n";

		InputStream source = new ByteArrayInputStream(text.getBytes());
		try {
			file.create(source, true, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Just do the basics: create a basic project.
	 *
	 * @param location
	 * @param projectName
	 */
	private static IProject createBaseProject(String projectName, URI location) {
        // it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

        if (!newProject.exists()) {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
                projectLocation = null;
            }

            desc.setLocationURI(projectLocation);
            try {
                newProject.create(desc, null);
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        return newProject;
    }

	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * Create a folder structure with a parent root, overlay, and a few child
	 * folders.
	 *
	 * @param newProject
	 * @param paths
	 * @throws CoreException
	 */
	private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
		for (String path : paths) {
			if (!path.isEmpty()) {
				IFolder folders = newProject.getFolder(path);
				createFolder(folders);
			}
		}
	}

	public static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(AldorNature.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = AldorNature.NATURE_ID;
			description.setNatureIds(newNatures);

			project.setDescription(description, null);
		}

	}

	public static AldorPreferenceUIFields uiFields(IProject project) throws CoreException {
		return aldorNature(project).uiFields();
	}

	public static AldorNature aldorNature(IProject project) throws CoreException {
		return (AldorNature) project.getNature(AldorNature.NATURE_ID);
	}

	public static IPreferenceStore getPreferenceStore(IProject project) {

		try {
			// This is slightly confusing.. obtain a preference store from disk, and add some default
			// values to it.
			AldorPreferenceUIFields uiFields = uiFields(project);
			IPath preferencesFile = project.getLocation().append(".aldorsettings");
			final PreferenceStore preferenceStore = new PreferenceStore(preferencesFile.toOSString());

			for (AldorPreferenceUIField<?> field : uiFields.all()) {
				String defaultStringValue = field.defaultStringValue();
				if (defaultStringValue != null) {
					preferenceStore.setDefault(field.name(), defaultStringValue);
				}
			}
			if (preferencesFile.toFile().canRead())
				preferenceStore.load();

			return preferenceStore;
		} catch (CoreException e) {
			return null;

		} catch (IOException e) {
			return null;
		}
	}

	public static void savePreferenceStore(IPreferenceStore preferenceStore) {
		System.out.println("Saving preferences: "  + preferenceStore);
		if (preferenceStore instanceof PreferenceStore) {
			try {
				((PreferenceStore) preferenceStore).save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}