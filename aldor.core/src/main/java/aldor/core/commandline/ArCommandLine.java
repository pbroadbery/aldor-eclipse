package aldor.core.commandline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

public class ArCommandLine {
	private final IPath executablePath;
	private IPath archiveFile;
	private final List<IPath> pathsToArchive;
	
	public ArCommandLine(IPath executablePath) {
		this.executablePath = executablePath;
		pathsToArchive = new LinkedList<IPath>();
		
	}
	
	public void addFile(IPath path) {
		pathsToArchive.add(path);
	}

	public void addFiles(Collection<? extends IPath> path) {
		pathsToArchive.addAll(path);
	}
	
	public String toCommandString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(executablePath.toOSString());
		sb.append(" cr ");
		sb.append(archiveFile.toOSString());
		for (IPath path: pathsToArchive) {
			sb.append(" " + path.toOSString());
		}
		
		return sb.toString();
	}
	
	public String[] arguments() {
		List<String> args = new ArrayList<String>(pathsToArchive.size()+2);
		
		args.add("cr");
		args.add(archiveFile.toOSString());
		for (IPath path: pathsToArchive) {
			args.add(path.toOSString());
		}
		return args.toArray(new String[0]);
	}

	public void archiveName(IPath archiveFileName) {
		this.archiveFile = archiveFileName;
	}

	public IPath executablePath() {
		return this.executablePath;
	}
}
