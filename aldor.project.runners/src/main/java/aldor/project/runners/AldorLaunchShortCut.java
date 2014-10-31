package aldor.project.runners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import aldor.core.AldorCore;

public abstract class AldorLaunchShortCut implements ILaunchShortcut2 {

	@Override
	public void launch(ISelection selection, String mode) {
		launch(getLaunchableResource(selection), mode);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(getLaunchableResource(editor), mode);
	}

	@Override
	public final IResource getLaunchableResource(final IEditorPart editorpart) {
		final IEditorInput input = editorpart.getEditorInput();
		if (input instanceof FileEditorInput)
			return ((FileEditorInput) input).getFile();

		return null;
	}

	@Override
	public final IResource getLaunchableResource(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (final Object element : ((IStructuredSelection) selection)
					.toArray()) {
				if (element instanceof IFile)
					return (IResource) element;
			}
		}

		return null;
	}

	private void launch(IResource file, String mode) {
		if (file instanceof IFile) {
			launch((IFile) file, mode);
		}
	}

	private void launch(IFile file, String mode) {
		ILaunchConfiguration[] configurations = getLaunchConfigurations(file);
		try {
			ILaunchConfiguration configuration;
			configuration = configurations.length > 0 ? configurations[0]
					: createConfigurationForFile(file);
			configuration.launch(mode, new NullProgressMonitor());
		} catch (CoreException e) {
			AldorCore.log("Failed to launch " + file.getFullPath() + " " + mode, e);
		}
	}

	private ILaunchConfigurationWorkingCopy createConfigurationForFile(
			IFile file) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(launchType());

		ILaunchConfigurationWorkingCopy configuration = type.newInstance(null,
				file.getName());
		configuration.setAttribute(
				AldorRunnerMetaModel.ConfigAttribute.RUNNER_Project.text(),
				file.getProject().getName());
		configuration.setAttribute(
				AldorRunnerMetaModel.ConfigAttribute.RUNNER_File.text(), file
						.getProjectRelativePath().toPortableString());

		// save and return new configuration
		configuration.doSave();
		return configuration;
	}

	protected abstract String launchType();

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return getLaunchConfigurations(getLaunchableResource(selection));
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getLaunchConfigurations(getLaunchableResource(editorpart));
	}

	private ILaunchConfiguration[] getLaunchConfigurations(IResource resource) {
		List<ILaunchConfiguration> configurations = new ArrayList<ILaunchConfiguration>();

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager
				.getLaunchConfigurationType(launchType());

		// try to find existing configurations using the same file
		for (ILaunchConfiguration configuration : safeLaunchConfigurations(
				manager, type)) {
			try {
				IFile file = AldorRunnerConfigAccessor.file(configuration);
				if (resource.equals(file))
					configurations.add(configuration);
			} catch (CoreException e) {
			}

		}
		return configurations.toArray(new ILaunchConfiguration[configurations
				.size()]);
	}

	private Iterable<ILaunchConfiguration> safeLaunchConfigurations(
			ILaunchManager manager, ILaunchConfigurationType type) {
		try {
			return Arrays.asList(manager.getLaunchConfigurations(type));
		} catch (CoreException e) {
			AldorCore.log("Failed to discover launch configs " + type, e);
		}
		return Collections.emptyList();
	}

}
