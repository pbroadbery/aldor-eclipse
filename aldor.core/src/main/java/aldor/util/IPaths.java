package aldor.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.base.Optional;

public class IPaths {

	public static void createDirectoryForPath(IPath destPath) throws CoreException {
		IPath path = destPath.removeLastSegments(1);
		if (path.toFile().exists())
			return;
		if (!path.toFile().mkdirs()) {
			throw new RuntimeException("Failed to create directory: " + path);
		}
	}

	public static IPath executablePath(IPath originalPath, String path) {
		if (path == null) {
			return originalPath;
		}
		if (!originalPath.isUNC() && originalPath.segmentCount() == 1) {
			Optional<File> pathToExecutable = Files.lookupExecutableByPath(originalPath.lastSegment(), path);
			if (pathToExecutable.isPresent())
				return Path.fromOSString(pathToExecutable.get().toString());
			else
				return originalPath;
		}
		return originalPath;
	}

}
