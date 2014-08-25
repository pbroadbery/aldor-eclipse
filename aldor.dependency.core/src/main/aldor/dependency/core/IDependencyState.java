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

	boolean isKnownName(String name);

	void updateDependencies(FileObj file, Iterable<String> filtered);
}