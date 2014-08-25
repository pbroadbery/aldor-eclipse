package aldor.command.output;

import java.util.LinkedList;
import java.util.List;

public class AldorError {
	int line;
	int col;
	AldorErrorSeverity severity;
	String headerLine;
	List<String> details;

	public AldorError(int[] spos, AldorErrorSeverity severity, String headerLine) {
		if (spos == null) {
			line = -1;
			col = 0;
		}
		else {
			line = spos[0];
			col = spos[1];
		}
		this.severity = severity;
		this.headerLine = headerLine;
		details = new LinkedList<String>();
	}

	public void addDetail(String line) {
		this.details.add(line);
	}

	public AldorErrorSeverity severity() {
		return severity;
	}

	public int lineNumber() {
		return line;
	}

	public String headerLine() {
		return headerLine;
	}
	public List<String> details() {
		return details;
	}
}