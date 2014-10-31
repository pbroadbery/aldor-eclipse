package aldor.project.runners;

public class AldorRunnerMetaModel {
	public final static String TYPE_Aldor_Interp_Launcher = "aldor.project.runners.launchAldorInterp";
	public final static String TYPE_Aldor_Binary_Launcher = "aldor.project.runners.launchAldorBinary";
	// Consider adding more metadata here (eg. types...)
	public enum ConfigAttribute {
		RUNNER_Project("aldor.project"), RUNNER_File("aldor.file");

		private final String text;

		ConfigAttribute(String text) {
			this.text = text;
		}

		public final String text() {
			return text;
		}
		
	}

}
