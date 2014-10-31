package aldor.command.output;

public enum AldorErrorSeverity {
	NOTE("Note"), WARN("Warning"), ERROR("Error"), FATAL("Fatal"), UNKNOWN("???");

	private String text;
	AldorErrorSeverity(String text) {
		this.text = text;
	}
	public static AldorErrorSeverity lookup(String levelText) {
		for (AldorErrorSeverity sev: AldorErrorSeverity.values()) {
			if (sev.text.equals(levelText))
				return sev;
		}
		return UNKNOWN;
	}
}