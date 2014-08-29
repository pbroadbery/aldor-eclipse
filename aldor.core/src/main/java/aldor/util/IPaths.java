package aldor.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class IPaths {

	public static void createDirectoryForPath(IPath destPath) throws CoreException {
		IPath path = destPath.removeLastSegments(1);
		if (path.toFile().exists())
			return;
		if (!path.toFile().mkdirs()) {
			throw new RuntimeException("Failed to create directory: " + path);
		}
	}

	
}
