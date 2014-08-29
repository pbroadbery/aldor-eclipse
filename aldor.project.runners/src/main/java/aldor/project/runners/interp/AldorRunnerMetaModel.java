package aldor.project.runners.interp;

public class AldorRunnerMetaModel {
	public final static String TYPE_Aldor_Interp_Launcher = "aldor.project.runners.launchAldorInterp";
	// Consider adding more metadata here (eg. types...)
	public enum ConfigAttribute {
		INTERP_Project("aldor.project"), INTERP_File("aldor.file");

		private final String text;

		ConfigAttribute(String text) {
			this.text = text;
		}

		public final String text() {
			return text;
		}
		
	}

}
