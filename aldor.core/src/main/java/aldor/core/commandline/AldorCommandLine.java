package aldor.core.commandline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.google.common.base.Optional;

public class AldorCommandLine {
	final IPath executablePath;
	Optional<OptimisationOption> optimisationLevel = Optional.absent();
	final List<Option> optionList = new LinkedList<>();
	IPath inputFilePath;

	public AldorCommandLine(IPath executablePath) {
		if (executablePath == null)
			throw new RuntimeException();
		this.executablePath = executablePath;
	}

	public IPath executablePath() {
		return executablePath;
	}

	public void inputFilePath(IPath iPath) {
		this.inputFilePath = iPath;
	}

	public void addLibrary(String libId, IPath fileName) {
		optionList.add(new AliasedLibraryOption(libId, fileName));
	}

	public void addLibrary(String libId) {
		optionList.add(new LibraryOption(libId));
	}

	void optimisationLevel(int lvl) {
		this.optimisationLevel = Optional.of(new OptimisationOption(lvl));
	}

	void clearOptimisationLevel() {
		this.optimisationLevel = Optional.absent();
	}

	void addStringOption(String text) {
		optionList.add(new StringOption(text));
	}

	public void addOutput(FileType file, IPath aoFile) {
		this.optionList.add(new FileGenerationOption(file, aoFile));
	}

	public void addRunType(RunType interpType) {
		this.optionList.add(new RunOption(interpType));
	}


	private static String toCommandString(AldorOption option, Augment augment, String value) {
		return "-" + option.abbrev() + augment.id() + "=" + value;
	}


	private interface Option {
		String toCommandString();
	}

	private class StringOption implements Option {
		final private String text;

		StringOption(String text) {
			this.text = text;
		}

		@Override
		public String toCommandString() {
			return text;
		}
	}

	private class FileGenerationOption implements Option {
		private final FileType fileType;
		private final IPath destination;

		FileGenerationOption(FileType fileType, IPath filePath) {
			this.fileType = fileType;
			this.destination = filePath;
		}

		@Override
		public String toCommandString() {
			return AldorCommandLine.toCommandString(AldorOption.File, fileType, this.destination.toOSString());
		}
	}

	private interface Augment {
		String id();
	}

	private class AliasedLibraryOption implements Option {
		String libName;
		IPath path;

		AliasedLibraryOption(String libName, IPath path) {
			this.libName = libName;
			this.path = path;
			if (libName == null || libName.length() == 0)
				throw new RuntimeException("Empty library name");
		}

		@Override
		public String toCommandString() {
			return AldorCommandLine.toCommandString(AldorOption.Library, new LibNameAugment(libName), path.toOSString());
		}
	}

	private class LibraryOption implements Option {
		private final String libName;

		LibraryOption(String libName) {
			this.libName = libName;
		}

		@Override
		public String toCommandString() {
			return "-" + AldorOption.Library.abbrev + libName;
		}
	}


	public class LibNameAugment implements Augment {
		private final String id;

		LibNameAugment(String name) {
			this.id = name;
		}
		@Override
		public String id() {
			return id;
		}
	}

	private class OptimisationOption implements Option {
		int optimisationLevel;

		public OptimisationOption(int lvl) {
			this.optimisationLevel = lvl;
		}

		@Override
		public String toCommandString() {
			return ""+ AldorOption.OptimisationLevel.abbrev() + optimisationLevel;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof OptimisationOption))
				return false;
			OptimisationOption otherOpt = (OptimisationOption) other;
			return optimisationLevel == otherOpt.optimisationLevel;
		}

		@Override
		public int hashCode() {
			return optimisationLevel;
		}
	}

	private class RunOption implements Option {
		final RunType type;

		public RunOption(RunType type) {
			this.type = type;
		}

		@Override
		public String toCommandString() {
			return "-" + AldorOption.Run.abbrev() + type.type();
		}
	}

	public String toCommandString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.executablePath.toOSString());
		for (Option opt: this.optionList) {
			sb.append(" ");
			sb.append(opt.toCommandString());
		}
		sb.append(" ");
		sb.append(this.inputFilePath);
		return sb.toString();
	}

	static public enum FileType implements Augment {
		Source("Source", "as"), Intermediate("Intermediate", "ao"), Object("object", "o"), Foam("Foam", "fm"), Java("Java", "java");
		String extension;

		FileType(String type, String extension) {
			assert !extension.contains(" ");
			this.extension = extension;

		}

		@Override
		public String id() {
			return extension;
		}
	}

	static enum AldorOption {
		File("F"), Library("l"), LibraryPath("Y"), OptimisationLevel("Q"), Run("G");

		private String abbrev;

		AldorOption(String abbrev) {
			this.abbrev = abbrev;
		}

		String abbrev() {
			return abbrev;
		}
	}

	public enum RunType {
		Loop("loop"), Interp("interp"), Run("run");
		private final String type;
		RunType(String type) {
			this.type = type;
		}

		public String type() {
			return type;
		}
	}


	public String[] arguments() {
		List<String> optionText = new ArrayList<>(optionList.size() + 1);
		for (Option option: optionList) {
			optionText.add(option.toCommandString());
		}
		optionText.add(this.inputFilePath.toOSString());
		return optionText.toArray(new String[0]);
	}

	public static IPath outputNameForName(FileType type, IPath location, String name) {
		String substring = nameForFileName(name);
		return location.append(substring + "." + type.extension);
	}

	public static String nameForFileName(String name) {
		int index = name.lastIndexOf(".");
		if (index == -1) {
			throw new RuntimeException();
		}
		String substring = name.substring(0, index);
		return substring;
	}

}
