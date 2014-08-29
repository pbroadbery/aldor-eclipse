package aldor.project.runners.interp;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.model.RuntimeProcess;

import aldor.project.builder.BuildCommands;
import aldor.project.runners.interp.AldorRunnerMetaModel.ConfigAttribute;

public class AldorLaunchInterpConfigurationDelegate extends LaunchConfigurationDelegate {

	private static final String MISSING = "<missing>";
	static public final String LNCH_ALDOR_INTERP_Project = ConfigAttribute.INTERP_Project.text();
	static public final String LNCH_ALDOR_INTERP_File = ConfigAttribute.INTERP_File.text();
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProject project = AldorInterpConfigAccessor.project(configuration);
		
		BuildCommands buildCommands = new BuildCommands(project);
		IFile file = AldorInterpConfigAccessor.file(configuration);
				
		Process process = buildCommands.runInterpOnIntermediate(file, configuration.getName(), monitor);

		launch.addProcess(new RuntimeProcess(launch, process, configuration.getName(), null));
	}

	@Override
	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {
		IProject project = AldorInterpConfigAccessor.project(configuration);
		if (project == null) {
			return null;
		}
		
		return new IProject[] { project};
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		String projectName = configuration.getAttribute(LNCH_ALDOR_INTERP_Project, MISSING);
		String file = configuration.getAttribute(LNCH_ALDOR_INTERP_File, MISSING);
		
		if (MISSING.equals(projectName) || MISSING.equals(file)) {
			return false;
		}
		return super.preLaunchCheck(configuration, mode, monitor);
	}
}
