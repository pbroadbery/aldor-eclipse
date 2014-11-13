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
		MessageConsole console = findAldorConsole();
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
		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void buildIntermediateFile(final IFile file, IProgressMonitor monitor) throws CoreException {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findAldorConsole();
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

	private IMarker addMarker(IResource resource, String message, int severity) {
		try {
			IMarker marker = resource.createMarker(AldorBuilder.MARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.MESSAGE, message);
			return marker;
		} catch (CoreException e) {

			return null;
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

		MessageConsole messageConsole = findAldorConsole();
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
		commandLine.define("BUILD_" + project().getName());
		if (this.targetLibraryName() != null) {
			commandLine.addLibrary(this.targetLibraryName(), this.archiveFileName(file.getFullPath()));
		}
		IPath aoFile = intermediateFileName(file.getFullPath());
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


	AldorCommandLine prepareCompileIntermediateToObjectCommandLine(IPath path, IPath objectPath) {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		IPath aldorExecutablePath = options.getOrDefault(preferences.executableLocation);
		AldorCommandLine commandLine = new AldorCommandLine(aldorExecutablePath);
		commandLine.inputFilePath(path);
		commandLine.addOutput(FileType.Object, objectPath);

		return commandLine;
	}



	public boolean confirmCanBuild() {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		if (options.getOrDefault(preferences.executableLocation) == null) {
			addMarker(project, "Missing aldor executable (to set, go to project preferences)", IMarker.SEVERITY_ERROR);
			return false;
		}
		IPath path = options.getOrDefault(preferences.executableLocation);

		if (!path.toFile().isFile()) {
			return false;
		}

		return true;
	}

	public IMarker emitCannotBuildError() {
		return addMarker(project, "Missing aldor executable: " + aldorExecutable() + " (to set, go to Project Properties, or Workspace Preferences)", IMarker.SEVERITY_ERROR);
	}

	public IPath aldorExecutable() {
		AldorProjectOptions options = new AldorProjectOptions();
		options.load(project);
		return options.getOrDefault(preferences.executableLocation);
	}


	public IPath intermediateFileName(IPath path) {
		IPath intermediateFileLocation = options.getOrDefault(preferences.intermediateFileLocation);
		IPath aoFile = AldorCommandLine.outputNameForName(FileType.Intermediate, intermediateFileLocation, path.lastSegment());
		return aoFile;
	}

	public IPath archiveFileName(IPath path) {
		IPath intermediateFileLocation = options.getOrDefault(preferences.intermediateFileLocation);
		String libName = options.getOrDefault(preferences.targetLibraryName);
		IPath aoFile = intermediateFileLocation.append("lib" + libName +"_" + AldorCommandLine.nameForFileName(path.lastSegment()) + ".al");
		return aoFile;
	}

	public IPath javaFileName(IPath file) {
		IPath javaFileLocation = options.getOrDefault(preferences.javaFileLocation);
		IPath javaFile = AldorCommandLine.outputNameForName(FileType.Java, javaFileLocation, file.lastSegment());
		return javaFile;
	}


	public IPath resultIntermediateLibraryFileName() {
		IPath intermediateFileLocation = options.getOrDefault(preferences.intermediateFileLocation);
		return intermediateFileLocation.append("lib" + project().getName() + ".al");
	}

	public IPath objectLibraryName() {
		IPath intermediateFileLocation = options.getOrDefault(preferences.binaryFileLocation);
		return intermediateFileLocation.append("lib" + project().getName() + ".a");
	}


	public IPath objectFileForIntermediate(IPath intermediatePath) {
		IPath binaryFileLocation = options.getOrDefault(preferences.binaryFileLocation);
		return binaryFileLocation.append(intermediatePath.removeFileExtension().addFileExtension("o").lastSegment());
	}

	public String targetLibraryName() {
		return this.options.getOrDefault(preferences.targetLibraryName);
	}

	public boolean isJavaWanted() {
		return options.getOrDefault(preferences.generateJava);
	}

	public void createTemporaryArchiveFile(IProgressMonitor monitor, IFile file, List<IPath> prerequisitePaths) throws CoreException {
		ArCommandLine commandLine = new ArCommandLine(new Path("ar"));
		IPath archiveFileName = archiveFileName(file.getFullPath());
		if (archiveFileName.toFile().exists())
			archiveFileName.toFile().delete();
		IPaths.createDirectoryForPath(project.getFile(archiveFileName).getLocation().removeLastSegments(1));
		commandLine.archiveName(archiveFileName);
		commandLine.addFiles(prerequisitePaths);
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findAldorConsole();
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

	public void createIntermediateLibrary(IProgressMonitor monitor, IPath archiveFileName, List<IPath> intermediatePaths) throws CoreException {
		createLibrary(monitor, archiveFileName, intermediatePaths);
	}

	private void createLibrary(IProgressMonitor monitor, IPath archiveFileName, List<IPath> intermediatePaths) throws CoreException {
		if (archiveFileName.toFile().exists())
			archiveFileName.toFile().delete();
		IPaths.createDirectoryForPath(project.getFile(archiveFileName).getLocation().removeLastSegments(1));
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findAldorConsole();
		IOConsoleOutputStream outputStream = messageConsole.newOutputStream();
		IOConsoleOutputStream errorStream = messageConsole.newOutputStream();

		ArCommandLine commandLine = new ArCommandLine(new Path("ar"));
		commandLine.archiveName(archiveFileName);
		commandLine.addFiles(intermediatePaths);

		OutputStreams.writeSafely(outputStream, "[AR] " + commandLine.toCommandString() + "\n");

		Process p = launcher.execute(commandLine.executablePath(), commandLine.arguments(), new String[0]/* env */, project().getLocation() /* cwd */,
				monitor);
		if (p == null) {
			return;
		}

		OutputStreams.forceClose(p.getOutputStream());

		launcher.waitAndRead(outputStream, errorStream, monitor);
	}

	public void buildObjectFile(IPath intermediateFile, IProgressMonitor monitor) throws CoreException {
		IPath objectFile= this.objectFileForIntermediate(intermediateFile);
		AldorCommandLine commandLine = this.prepareCompileIntermediateToObjectCommandLine(intermediateFile, objectFile);

		IPaths.createDirectoryForPath(project.getFile(objectFile).getLocation().removeLastSegments(1));
		ICommandLauncher launcher = new CommandLauncher();
		launcher.setProject(project());
		launcher.showCommand(true);

		MessageConsole messageConsole = findAldorConsole();
		IOConsoleOutputStream outputStream = messageConsole.newOutputStream();
		IOConsoleOutputStream errorStream = messageConsole.newOutputStream();

		OutputStreams.writeSafely(outputStream, "[AO->O] " + commandLine.toCommandString() + "\n");

		Process p = launcher.execute(commandLine.executablePath(), commandLine.arguments(), new String[0]/* env */, project().getLocation() /* cwd */,
				monitor);
		if (p == null) {
			return;
		}

		OutputStreams.forceClose(p.getOutputStream());

		launcher.waitAndRead(outputStream, errorStream, monitor);
	}

	public static MessageConsole findAldorConsole() {
		return findConsole("AldorCommand");
	}

	public void createObjectLibrary(IProgressMonitor monitor, IPath archiveFileName, List<IPath> objectFiles) throws CoreException {
		createLibrary(monitor, archiveFileName, objectFiles);
	}

}
