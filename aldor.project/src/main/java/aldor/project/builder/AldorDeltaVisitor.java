package aldor.project.builder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import aldor.core.AldorCore;

class AldorDeltaVisitor implements IResourceDeltaVisitor {
	List<IFile> toBeChecked = new LinkedList<>();
	List<IFile> removed = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
	 * .core.resources.IResourceDelta)
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		AldorCore.log("Looking at resource " + resource.toString() + " " + resource.getClass().getName(), null);
		if (!(resource instanceof IFile))
			return true;
		IFile file = (IFile) resource;
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			check(file);
			break;
		case IResourceDelta.REMOVED:
			remove(file);
			break;
		case IResourceDelta.CHANGED:
			check(file);
			break;
		}
		// return true to continue visiting children.
		return true;
	}

	private void remove(IFile resource) {
		removed.add(resource);
	}

	private void check(IFile resource) {
		toBeChecked.add(resource);
	}

	public Collection<IFile> toBeChecked() {
		return toBeChecked;
	}

	public List<IFile> removed() {
		return removed;
	}

}