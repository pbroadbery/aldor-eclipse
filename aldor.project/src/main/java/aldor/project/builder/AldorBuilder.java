package aldor.project.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import aldor.core.AldorCore;
import aldor.dependency.core.DelegatedDependencyState;
import aldor.dependency.core.DependencyStates;
import aldor.project.builder.DependencyScanner.ScanResult;
import aldor.util.IFiles;
import aldor.util.IPaths;

import com.google.common.base.Function;
import com.google.common.base.Objects;
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
				}
			}, file);
			return namesInOrder;
		}

	}

	public static final String BUILDER_ID = "aldor.project.AldorBuilder";
	static public final String MARKER_TYPE = "aldor.project.aldorProblem";
	private IFileDependencyState dependencyState;
	private IPath lastAldorPath = null;
	private IMarker noBuildMarker;

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
	protected IProject[] build(final int kind, @SuppressWarnings("rawtypes") final Map args, final IProgressMonitor monitor) throws CoreException {

		IProject project = getProject();
		if (this.noBuildMarker != null)
			noBuildMarker.delete();
		BuildCommands buildCommands = new BuildCommands(project);
		if (!buildCommands.confirmCanBuild()) {
			this.noBuildMarker = buildCommands.emitCannotBuildError();
			return null;
		}

		IResourceDelta delta = getDelta(getProject());
		int actualKind = selectBuildType(kind, delta, buildCommands);

		if (actualKind == FULL_BUILD) {
			this.fullBuild(monitor);
		}
		else {
			this.incrementalBuild(delta, monitor);
		}
		return null;
	}

	private int selectBuildType(int kind, IResourceDelta delta, BuildCommands buildCommands) {
		if (kind != FULL_BUILD) {
			if (Objects.equal(lastAldorPath, buildCommands.aldorExecutable())) {
				kind = FULL_BUILD;
			}
			if (delta == null) {
				kind = FULL_BUILD;
			}
		}
		return kind;
	}

	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
		try {
			getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			AldorResourceVisitor visitor = new AldorResourceVisitor();
			getProject().accept(visitor);
			for (IFile file : visitor.toBeChecked()) {
				if (IFiles.isAldorSourceFile(file)) {
					dependencyState.aldorFileAdded(file);
				}
			}
		} catch (CoreException e) {
		}
		super.forgetLastBuiltState();
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void checkAldor(BuildCommands buildCommands, IResource resource, IProgressMonitor monitor) {
		if (resource instanceof IFile && (resource.getName().endsWith(".as") || resource.getName().endsWith(".ao"))) {
			build(buildCommands, resource, monitor);
		}
	}

	private void build(BuildCommands buildCommands, IResource resource, IProgressMonitor monitor) {
		String fileName = resource.getName();
		if (fileName.endsWith(".as")) {
			assert resource instanceof IFile;
			IFile file = (IFile) resource;
			deleteMarkers(file);
			IPath destPath = buildCommands.intermediateFileName(file.getFullPath());
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

	public void createTemporaryArchiveFile(IProgressMonitor monitor, final BuildCommands buildCommands, IFile file, IPath destPath) {
		if (!needsTemporaryArchiveFile(buildCommands, file)) {
			return;
		}
		List<IFile> prerequisites = dependencyState.prerequisites(file);
		List<IPath> prerequisitePaths = Lists.transform(prerequisites, new Function<IFile, IPath>() {

			@Override
			public IPath apply(IFile prereqFile) {
				return buildCommands.intermediateFileName(prereqFile.getFullPath());
			}
		});

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

		IPath fileName = buildCommands.archiveFileName(file.getFullPath());
		fileName.toFile().delete();
	}

	private void buildIntermediateFile(IProgressMonitor monitor, BuildCommands buildCommands, IFile file, IPath destPath)
			throws CoreException {
		IPaths.createDirectoryForPath(getProject().getFile(destPath).getLocation());
		buildCommands.buildIntermediateFile(file, monitor);
		IFile destFile = getProject().getFile(destPath);
		if (destFile.exists()) {
			destFile.setDerived(true, monitor);
			destFile.refreshLocal(1, monitor);
			build(buildCommands, destFile, monitor); // .. and create things
														// dependent on the
			// .ao
			// file
		}
	}

	private void buildJavaFile(IResource resource, IProgressMonitor monitor, BuildCommands buildCommands) throws CoreException {
		IFile file = (IFile) resource;
		IPath destPath = buildCommands.javaFileName(file.getFullPath());
		IPaths.createDirectoryForPath(getProject().getFile(destPath).getLocation());
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
			final BuildCommands buildCommands = new BuildCommands(getProject());
			dependencyState.release();
			dependencyState = new IFileDependencyState();
			AldorResourceVisitor visitor = new AldorResourceVisitor();
			getProject().accept(visitor);
			for (IFile file : visitor.toBeChecked()) {
				if (IFiles.isAldorSourceFile(file)) {
					dependencyState.aldorFileAdded(file);
				}
			}
			updateDependencies(buildCommands);
			dependencyState.visitInBuildOrder(new Function<IFile, Boolean>() {

				@Override
				public Boolean apply(IFile file) {
					monitor.subTask("Build " + file);
					if (isInterrupted()) {
						return false;
					}

					build(buildCommands, file, monitor);
					dependencyState.built(dependencyState.getName(file));
					return true;
				}
			});
			buildIntermediateLibrary(buildCommands, monitor);
		} catch (CoreException e) {
			AldorCore.log(e);
			throw e;
		}
	}

	// Update dependencies for all items that require it.
	private void updateDependencies(BuildCommands commands) throws CoreException {
		DependencyScanner scanner = new DependencyScanner();
		Collection<IFile> filesToUpdate = new ArrayList<>(dependencyState.needsDependencyUpdate());
		for (IFile file : filesToUpdate) {
			try {
				ScanResult scan = scanner.scan(file);
				if (scan.skipBuild()) {
					if (dependencyState.isKnownName(dependencyState.getName(file)))
						dependencyState.doNotBuild(file);
				} else {
					Iterable<String> filtered = Iterables.filter(scan.dependencies(), new Predicate<String>() {

						@Override
						public boolean apply(String arg0) {
							return dependencyState.isKnownName(arg0);
						}
					});
					dependencyState.updateDependencies(file, filtered);
				}
			} catch (IOException e) {
				throw new CoreException(AldorCore.createStatus("IO Error while scanning " + file, e));
			}
		}
	}

	protected void incrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
		// sort out dependency changes.
		AldorDeltaVisitor visitor = new AldorDeltaVisitor();
		final BuildCommands buildCommands = new BuildCommands(getProject());

		buildCommands.emitBuildPlan(null, "Starting build: " + delta);

		delta.accept(visitor);
		for (IFile file : visitor.removed()) {
			dependencyState.aldorFileRemoved(file);
		}
		for (IFile file : visitor.toBeChecked()) {
			dependencyState.aldorFileChanged(file);
		}
		dependencyState.visitInBuildOrder(new Function<IFile, Boolean>() {

			@Override
			public Boolean apply(IFile input) {
				buildCommands.emitBuildPlan(input, "BuildDeps: " + dependencyState.prerequisites(input));
				return true;
			}
		});
		buildCommands.emitBuildPlan(null, "PreDependencyUpdate: " + dependencyState);
		this.updateDependencies(buildCommands);
		buildCommands.emitBuildPlan(null, "PostDependencyUpdate: " + dependencyState);
		buildCommands.emitBuildPlan(null, "Need to build: " + namesNeedingBuild());
		final int total = countIncrementalWork();
		// and build
		monitor.beginTask("Incremental build", total);
		boolean result = dependencyState.visitInBuildOrderForBuild(new Function<IFile, Boolean>() {

			@Override
			public Boolean apply(IFile file) {
				monitor.worked(1);
				monitor.subTask("Build " + file);
				build(buildCommands, file, monitor);
				dependencyState.built(dependencyState.getName(file));
				return true;
			}
		});

		if (total > 0 && result) {
			buildIntermediateLibrary(buildCommands, monitor);
			buildObjectLibrary(buildCommands, monitor);
		}
	}

	private void buildObjectLibrary(final BuildCommands buildCommands, final IProgressMonitor monitor) throws CoreException {
		final List<IPath> intermediateFiles = new ArrayList<>();
		final List<IPath> objectFiles = new ArrayList<>();
		dependencyState.visitInBuildOrder(new Function<IFile, Boolean>() {

			@Override
			public Boolean apply(IFile input) {
				intermediateFiles.add(buildCommands.intermediateFileName(input.getFullPath()));
				return true;
			}});

		for (IPath intermediatePath: intermediateFiles) {
			IPath objectFile = buildCommands.objectFileForIntermediate(intermediatePath);
			objectFiles.add(objectFile);
			buildCommands.buildObjectFile(intermediatePath, monitor);
		}

		buildCommands.createObjectLibrary(monitor, buildCommands.objectLibraryName(), objectFiles);
	}

	private void buildIntermediateLibrary(final BuildCommands buildCommands, IProgressMonitor monitor) throws CoreException {
		final List<IPath> intermediateFiles = new ArrayList<>();
		dependencyState.visitInBuildOrder(new Function<IFile, Boolean>() {

			@Override
			public Boolean apply(IFile input) {
				intermediateFiles.add(buildCommands.intermediateFileName(input.getFullPath()));
				return true;
			}});
		buildCommands.createIntermediateLibrary(monitor, buildCommands.resultIntermediateLibraryFileName(), intermediateFiles);
	}

	private int countIncrementalWork() {
		final int[] box = new int[] { 0 };
		dependencyState.visitInBuildOrderForBuild(new Function<IFile, Boolean>() {

			@Override
			public Boolean apply(IFile file) {
				if (dependencyState.needsBuild(dependencyState.getName(file))) {
					box[0] = box[0] + 1;
				}
				return true;
			}
		});
		final int total = box[0];
		return total;
	}

	private List<IFile> namesNeedingBuild() {
		return DependencyStates.buildOrderForBuild(this.dependencyState);
	}

}
