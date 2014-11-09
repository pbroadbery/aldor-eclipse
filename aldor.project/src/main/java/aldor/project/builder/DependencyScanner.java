package aldor.project.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class DependencyScanner {
	DependencyScanner() {

	}

	// TODO: Replace with a proper parser.
	ScanResult scan(IFile file) throws CoreException, IOException {
		try (BufferedReader content = new BufferedReader(new InputStreamReader(
				file.getContents()))) {
			List<String> dependencies = new ArrayList<String>(10);
			boolean skip = false;
			while (true) {
				String line = content.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("--DEPS: ")) {
					List<String> list = Arrays.asList(line.split(" "));
					list = list.subList(1, list.size());
					dependencies.addAll(list);
				}
				if (line.startsWith("--NOBUILD")) {
					skip = true;
				}
			}
			return new ScanResult(skip, dependencies);
		}
	}

	static class ScanResult {
		private final List<String> dependencies;
		private final boolean skipBuild;

		ScanResult(boolean skip, List<String> dependencies) {
			this.dependencies = dependencies;
			this.skipBuild = skip;
		}

		public List<String> dependencies() {
			return dependencies;
		}

		public boolean skipBuild() {
			return skipBuild;
		}
	}
}
