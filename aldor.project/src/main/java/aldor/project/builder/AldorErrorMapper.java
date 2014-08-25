package aldor.project.builder;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

import aldor.command.output.AldorError;
import aldor.command.output.AldorErrorSeverity;

public class AldorErrorMapper {
	static String detailText(AldorError error) {
		StringBuilder detail = new StringBuilder();
		assert !error.headerLine().endsWith("\n");
		if (!error.details().isEmpty())
			detail.append("\n");
		String sep = "";
		for (String line: error.details()) {
			detail.append(sep);
			detail.append(line);
			sep = "\n";
		}
		return error.headerLine().trim() + detail;
	}

	static final Map<AldorErrorSeverity, Integer> severityCodeForSeverity = new EnumMap<AldorErrorSeverity, Integer>(AldorErrorSeverity.class);
	static {
		severityCodeForSeverity.put(AldorErrorSeverity.NOTE, IMarker.SEVERITY_INFO);
		severityCodeForSeverity.put(AldorErrorSeverity.WARN, IMarker.SEVERITY_WARNING);
		severityCodeForSeverity.put(AldorErrorSeverity.ERROR, IMarker.SEVERITY_ERROR);
		severityCodeForSeverity.put(AldorErrorSeverity.FATAL, IMarker.SEVERITY_ERROR);
	}
	public static int severityCode(AldorErrorSeverity severity) {
		assert severityCodeForSeverity.size() == AldorErrorSeverity.values().length;
		return severityCodeForSeverity.get(severity);
	}
	
}
