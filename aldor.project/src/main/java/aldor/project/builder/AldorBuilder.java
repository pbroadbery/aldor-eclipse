package aldor.project.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import aldor.core.AldorCore;
import aldor.dependency.core.DelegatedDependencyState;
import aldor.util.IFiles;
import aldor.util.IPaths;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AldorBuilder extends IncrementalProjectBuilder {

	static class IFileDependencyState extends DelegatedDependencyState<IFile> {
		IFileDependencyState() {
			super();
		}

		@Override
		public String getName(IFile obj) {
			return IFiles.idForAldorFile(obj);
		}

		public List<IFile> prerequisites(final IFile file) {
			final List<IFile> namesInOrder = new ArrayList<IFile>();
			super.visitInBuildOrder(new Function<IFile, Boolean>() {

				@Override
				public Boolean apply(IFile arg0) {
					if (arg0.equals(file))
						return true;
					namesInOrder.add(arg0);
					return true;
				}}, file);
			return namesInOrder;
		}

	}

	public static final String BUILDER_ID = "aldor.project.AldorBuilder";
	static public final String MARKER_TYPE = "aldor.project.aldorProblem";
	private IFileDependencyState dependencyState;

	public AldorBuilder() {
		this.dependencyState = new IFileDependencyState();
	}

	class AldorResourceVisitor implements IResourceVisitor {
		List<IFile> toBeChecked = new LinkedList<>();

		@Override
		public boolean visit(IResource resource) {
			if (isAldorResource(resource))
				toBeChecked.add((IFile) resource);
			return true;
		}

		public List<IFile> toBeChecked() {
			return toBeChecked;
		}
	}

	public boolean isAldorResource(IResource resource) {
		if (!(resource instanceof IFile))
			return false;
		if (resource.getName().endsWith(".as"))
			return true;
		else if (resource.getName().endsWith(".ao"))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor) throws CoreException {

		if (!(new BuildCommands(getProject())).confirmCanBuild())
			return null;

		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			AldorCore.log(IStatus.OK, "build - Delta " + getProject().getName() + " " + delta);
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void checkAldor(IResource resource, IProgressMonitor monitor) {
		if (resource instanceof IFile && (resource.getName().endsWith(".as") || resource.getName().endsWith(".ao"))) {
			build(resource, monitor);
		}
	}

	private void build(IResource resource, IProgressMonitor monitor) {
		BuildCommands buildCommands = new BuildCommands(getProject());
		String fileName = resource.getName();
		if (fileName.endsWith(".as")) {
			assert resource instanceof IFile;
			IFile file = (IFile) resource;
			deleteMarkers(file);
			IPath destPath = buildCommands.intermediateFileName(file);
			try {
				createTemporaryArchiveFile(monitor, buildCommands, file, destPath);
				buildIntermediateFile(monitor, buildCommands, file, destPath);
				removeTemporaryArchiveFile(monitor, buildCommands, file, destPath);
			} catch (CoreException e) {
				e.printStackTrace();
				return;
			}
		}
		if (fileName.endsWith(".ao")) {
			assert resource instanceof IFile;
			try {
				if (buildCommands.isJavaWanted())
					buildJavaFile(resource, monitor, buildCommands);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	private void createTemporaryArchiveFile(IProgressMonitor monitor, final BuildCommands buildCommands, IFile file, IPath destPath) {		
		if (!needsTemporaryArchiveFile(buildCommands, file)) {
			return;
		}
		List<IFile> prerequisites = dependencyState.prerequisites(file);
		List<IPath> prerequisitePaths = Lists.transform(prerequisites, new Function<IFile, IPath>() {

			@Override
			public IPath apply(IFile arg0) {
				return buildCommands.intermediateFileName(arg0);
			}});
		
		try {
			buildCommands.createTemporaryArchiveFile(monitor, file, prerequisitePaths);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean needsTemporaryArchiveFile(BuildCommands buildCommands, IFile file) {
		if (buildCommands.targetLibraryName() == null)
			return false;
		
		List<IFile> prerequisites = dependencyState.prerequisites(file);
		if (prerequisites.isEmpty())
			return false;

		return true;
	}

	private void removeTemporaryArchiveFile(IProgressMonitor monitor, final BuildCommands buildCommands, IFile file, IPath destPath) {
		if (!needsTemporaryArchiveFile(buildCommands, file)) {
			return;
		}

		IPath fileName = buildCommands.archiveFileName(file);
		fileName.toFile().delete();
	}
	
	private void buildIntermediateFile(IProgressMonitor monitor, BuildCommands buildCommands, IFile file, IPath destPath) throws CoreException {
		IPaths.createDirectoryForPath(destPath);
		buildCommands.buildIntermediateFile(file, monitor);
		IFile destFile = getProject().getFile(destPath);
		if (destFile.exists()) {
			destFile.setDerived(true, monitor);
			destFile.refreshLocal(1, monitor);
			build(destFile, monitor); // .. and create things dependent on the .ao
			// file
		}
	}

	private void buildJavaFile(IResource resource, IProgressMonitor monitor, BuildCommands buildCommands) throws CoreException {
		IFile file = (IFile) resource;
		IPath destPath = buildCommands.javaFileName(file);
		IPaths.createDirectoryForPath(destPath);
		buildCommands.buildJavaFile((IFile) resource, monitor);
		IFile destFile = getProject().getFile(destPath);
		if (destFile.exists()) {
			destFile.setDerived(true, monitor);
			destFile.refreshLocal(1, monitor);
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			dependencyState.release();
			dependencyState = new IFileDependencyState();
			AldorResourceVisitor visitor = new AldorResourceVisitor();
			getProject().accept(visitor);
			for (IFile file : visitor.toBeChecked()) {
				if (IFiles.isAldorSourceFile(file)) {
					dependencyState.aldorFileAdded(file);
				}
			}
			DependencyScanner scanner = new DependencyScanner();
			for (IFile file : dependencyState.needsDependencyUpdate()) {
				List<String> scan = scanner.scan(file);
				Iterable<String> filtered = Iterables.filter(scan, new Predicate<String>() {

					@Override
					public boolean apply(String arg0) {
						return dependencyState.isKnownName(arg0);
					}
				});
				dependencyState.updateDependencies(file, filtered);
			}
			dependencyState.visitInBuildOrder(new Function<IFile, Boolean>() {

				@Override
				public Boolean apply(IFile file) {
					build(file, monitor);
					return true;
				}
			});
		} catch (CoreException e) {
			AldorCore.log(e);
			throw e;
		} catch (IOException e) {
			AldorCore.log(e);
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.

		// AldorDeltaVisitor visitor = new AldorDeltaVisitor();
		// delta.accept(visitor);
		// for (IFile file: visitor.removed()) {
		// dependencyState.aldorFileRemoved((IFile) file);
		// }
		// for (IFile file : visitor.toBeChecked()) {
		// dependencyState.aldorFileChanged((IFile) file);
		// }
		fullBuild(monitor);
	}

}
