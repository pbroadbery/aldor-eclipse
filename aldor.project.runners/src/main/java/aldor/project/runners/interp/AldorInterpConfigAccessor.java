package aldor.project.runners.interp;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class AldorInterpConfigAccessor {
	
	public static IProject project(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(AldorRunnerMetaModel.ConfigAttribute.INTERP_Project.text(), "");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		return project;
	}

	public static IFile file(ILaunchConfiguration configuration) throws CoreException {
		IProject project = project(configuration);
		if (project == null)
			return null;
		String fileName = configuration.getAttribute(AldorRunnerMetaModel.ConfigAttribute.INTERP_File.text(), "");
		
		IFile file = project.getFile(fileName);
		if (!file.exists())
			return null;
		
		return file;
	}

}
