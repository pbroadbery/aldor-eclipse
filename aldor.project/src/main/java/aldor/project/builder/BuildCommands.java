package aldor.project.builder;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

import aldor.command.output.AldorError;
import aldor.command.output.AldorErrorMessageParser;
import aldor.command.output.ParsingOutputStream;
import aldor.core.AldorCore;
import aldor.core.commandline.AldorCommandLine;
import aldor.core.commandline.AldorCommandLine.FileType;
import aldor.core.commandline.ArCommandLine;
import aldor.core.project.AldorPreferenceModel;
import aldor.project.properties.AldorProjectOptions;
import aldor.util.IPaths;
import aldor.util.OutputStreams;
import aldor.util.event.EventAdapter;

public class BuildCommands {

	private static final AldorPreferenceModel preferences = AldorPreferenceModel.instance();
	private IProject project;
	private AldorProjectOptions options = new AldorProjectOptions();
	
	public BuildCommands(IProject project) {
		this.project = project;
		this.options.load(project);
	}

	private IProject project() {
		return project;
	}

	public void emitBuildPlan(final IFile file, final String status) {
		MessageConsole console = findConsole("AldorCommand");
		final IOConsoleOutputStream outStream = console.newOutputStream();
		
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				outStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
				try {
					outStream.write(">>> "  + file  + " " + status + "\n");
				} catch (IOException e) {
					AldorCore.log(e);
				}
				finally {
					outStream.setColor(null);
				}
			}});	
	}
	
	public void buildIntermediateFile(final IFile file, IProgressMonitor monitor) throws CoreException {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findConsole("AldorCommand");
		IOConsoleOutputStream outputStream = messageConsole.newOutputStream();
		ParsingOutputStream output = new ParsingOutputStream(outputStream);
		IOConsoleOutputStream errorStream = messageConsole.newOutputStream();

		AldorCommandLine commandLine = prepareBuildIntermediateCommandLine(file);		
		OutputStreams.writeSafely(outputStream, "[AO] " + commandLine.toCommandString() + "\n");
		
		Process p = launcher.execute(commandLine.executablePath(), commandLine.arguments(), new String[0]/* env */, project().getLocation() /* cwd */,
				monitor);
		if (p == null) {
			OutputStreams.forceClose(output);
			return;
		}
		OutputStreams.forceClose(p.getOutputStream());

		ParsingOutputStream err = new ParsingOutputStream(errorStream);
		final AldorErrorMessageParser errorParser = new AldorErrorMessageParser();
		output.addListener(errorParser);
		errorParser.addListener(new EventAdapter<AldorError>() {

			@Override
			public void onEvent(AldorError event) {
				addMarker(file, AldorErrorMapper.detailText(event), event.lineNumber(), AldorErrorMapper.severityCode(event.severity()));
			}
		});
		launcher.waitAndRead(output, err, monitor);
	}

	private void addMarker(IResource resource, String message, int severity) {
		try {
			IMarker marker = resource.createMarker(AldorBuilder.MARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.MESSAGE, message);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(AldorBuilder.MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public void buildJavaFile(IFile file, IProgressMonitor monitor) throws CoreException {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findConsole("AldorCommand");
		IOConsoleOutputStream outputStream = messageConsole.newOutputStream();
		IOConsoleOutputStream errorStream = messageConsole.newOutputStream();

		AldorCommandLine commandLine = prepareBuildJavaCommandLine(file);
		OutputStreams.writeSafely(outputStream, "[Java] " + commandLine.toCommandString() + "\n");
		
		Process p = launcher.execute(commandLine.executablePath(), commandLine.arguments(), new String[0]/* env */, project().getLocation() /* cwd */,
				monitor);
		if (p == null) {
			return;
		}
		OutputStreams.forceClose(p.getOutputStream());

		ParsingOutputStream err = new ParsingOutputStream(errorStream);
		launcher.waitAndRead(outputStream, err, monitor);
	}
	
	
	public Process runInterpOnIntermediate(final IFile file, String name, String[] env, IProgressMonitor monitor) throws CoreException {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);
	
		AldorCommandLine commandLine = prepareRunInterpOnIntermediateCommandLine(file);		
		
		Process process = launcher.execute(commandLine.executablePath(), commandLine.arguments(), env, project().getLocation() /* cwd */,
				monitor);
		if (process == null) {
			return null;
		}
		OutputStreams.forceClose(process.getOutputStream());

		return process;
	}

	public Process runAsBinaryOnIntermediate(IFile file, String name, String[] env, IProgressMonitor monitor) throws CoreException {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);
	
		AldorCommandLine commandLine = prepareRunBinaryOnIntermediateCommandLine(file);		
		
		Process process = launcher.execute(commandLine.executablePath(), commandLine.arguments(), env, project().getLocation() /* cwd */,
				monitor);
		if (process == null) {
			return null;
		}
		OutputStreams.forceClose(process.getOutputStream());

		return process;
	}

	

	AldorCommandLine prepareBuildIntermediateCommandLine(IFile file) {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		IPath aldorExecutablePath = options.getOrDefault(preferences.executableLocation);
		AldorCommandLine commandLine = new AldorCommandLine(aldorExecutablePath);
		commandLine.inputFilePath(file.getLocation());
		if (this.targetLibraryName() != null) {
			commandLine.addLibrary(this.targetLibraryName(), this.archiveFileName(file));
		}
		IPath aoFile = intermediateFileName(file);
		commandLine.addOutput(AldorCommandLine.FileType.Intermediate, aoFile);
		return commandLine;
	}

	AldorCommandLine prepareBuildJavaCommandLine(IFile file) {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		IPath aldorExecutablePath = options.getOrDefault(preferences.executableLocation);
		AldorCommandLine commandLine = new AldorCommandLine(aldorExecutablePath);
		commandLine.inputFilePath(file.getLocation());
		IPath javaFileLocation = options.getOrDefault(preferences.javaFileLocation);
		IPath javaFile = AldorCommandLine.outputNameForName(FileType.Java, javaFileLocation, file.getName());
		commandLine.addOutput(AldorCommandLine.FileType.Java, javaFile);
		return commandLine;
	}
	

	AldorCommandLine prepareRunInterpOnIntermediateCommandLine(IFile file) {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		IPath aldorExecutablePath = options.getOrDefault(preferences.executableLocation);
		AldorCommandLine commandLine = new AldorCommandLine(aldorExecutablePath);
		commandLine.inputFilePath(file.getLocation());
		commandLine.addRunType(AldorCommandLine.RunType.Interp);

		return commandLine;
	}


	AldorCommandLine prepareRunBinaryOnIntermediateCommandLine(IFile file) {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		IPath aldorExecutablePath = options.getOrDefault(preferences.executableLocation);
		AldorCommandLine commandLine = new AldorCommandLine(aldorExecutablePath);
		commandLine.inputFilePath(file.getLocation());
		commandLine.addRunType(AldorCommandLine.RunType.Run);
		commandLine.addLibrary("aldor");
		return commandLine;
	}
	
	
	public boolean confirmCanBuild() {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		if (options.get(preferences.executableLocation) == null) {
			addMarker(project, "Missing aldor executable (to set, go to project preferences)", IMarker.SEVERITY_ERROR);
			return false;
		}
		return true;
	}


	public IPath intermediateFileName(IFile file) {
		IPath intermediateFileLocation = options.getOrDefault(preferences.intermediateFileLocation);
		IPath aoFile = AldorCommandLine.outputNameForName(FileType.Intermediate, intermediateFileLocation, file.getName());
		return aoFile;
	}

	public IPath archiveFileName(IFile file) {
		IPath intermediateFileLocation = options.getOrDefault(preferences.intermediateFileLocation);
		String libName = options.getOrDefault(preferences.targetLibraryName);
		IPath aoFile = intermediateFileLocation.append("lib" + libName +"_" + AldorCommandLine.nameForFileName(file.getName()) + ".al");
		return aoFile;
	}

	public IPath javaFileName(IFile file) {
		IPath javaFileLocation = options.getOrDefault(preferences.javaFileLocation);
		IPath javaFile = AldorCommandLine.outputNameForName(FileType.Java, javaFileLocation, file.getName());
		return javaFile;
	}

	public String targetLibraryName() {
		return this.options.getOrDefault(preferences.targetLibraryName);
	}

	public boolean isJavaWanted() {
		return options.getOrDefault(preferences.generateJava);
	}

	public void createTemporaryArchiveFile(IProgressMonitor monitor, IFile file, List<IPath> prerequisitePaths) throws CoreException {
		ArCommandLine commandLine = new ArCommandLine(new Path("ar"));
		IPath archiveFileName = archiveFileName(file);
		if (archiveFileName.toFile().exists())
			archiveFileName.toFile().delete();
		IPaths.createDirectoryForPath(project.getFile(archiveFileName).getLocation().removeLastSegments(1));
		commandLine.archiveName(archiveFileName);
		commandLine.addFiles(prerequisitePaths);
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findConsole("AldorCommand");
		IOConsoleOutputStream outputStream = messageConsole.newOutputStream();
		IOConsoleOutputStream errorStream = messageConsole.newOutputStream();

		OutputStreams.writeSafely(outputStream, "[AR] " + commandLine.toCommandString() + "\n");
		
		Process p = launcher.execute(commandLine.executablePath(), commandLine.arguments(), new String[0]/* env */, project().getLocation() /* cwd */,
				monitor);
		if (p == null) {
			return;
		}
		OutputStreams.forceClose(p.getOutputStream());

		launcher.waitAndRead(outputStream, errorStream, monitor);
	}

	
}
