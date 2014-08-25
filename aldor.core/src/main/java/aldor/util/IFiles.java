package aldor.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class IFiles {

	public static void createDirectory(IContainer parent, IProgressMonitor monitor) throws CoreException {
		if (!parent.exists()) {
			createDirectory(parent.getParent(), monitor);
			IFolder folder = (IFolder) parent;
			folder.create(false, false, monitor);
		}
	}

	public static boolean isAldorSourceFile(IFile file) {
		return file.getName().endsWith(".as");
	}
	
	public static String idForAldorFile(IFile file) {
		assert isAldorSourceFile(file);
		
		return file.getName().substring(0, file.getName().length()-3);
	}
	
}
