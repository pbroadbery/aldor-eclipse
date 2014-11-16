package aldor.util;

import java.io.File;

import com.google.common.base.Optional;

public class Files {

	public static Optional<File> lookupExecutableByPath(String executableName, String path) {
		String[] pathSplit = path.split(File.pathSeparator);
		for (String pathElement: pathSplit) {
			File dir = new File(pathElement);
			if (dir.isDirectory()) {
				File executable = new File(dir, executableName);
				if (executable.canExecute())
					return Optional.of(executable);
			}
		}
		return Optional.absent();
	}
}

