package aldor.dependency.core;

import java.util.Collection;

import com.google.common.base.Function;

public interface IDependencyState<FileObj> {

	void release();

	void aldorFileAdded(FileObj file);

	void aldorFileRemoved(FileObj file);

	void aldorFileChanged(FileObj file);

	Collection<FileObj> needsDependencyUpdate();

	void visitInBuildOrder(Function<FileObj, Boolean> function);
	void visitInBuildOrder(Function<FileObj, Boolean> function, FileObj start);

	boolean visitInBuildOrderForBuild(Function<FileObj, Boolean> function);

	//  Each fileObj should have a unique name.. this tests if we know about it.
	boolean isKnownName(String name);

	// Mark file as depending on the supplied names.
	void updateDependencies(FileObj file, Iterable<String> filtered);

	boolean needsBuild(String name);
	void built(String name);
}