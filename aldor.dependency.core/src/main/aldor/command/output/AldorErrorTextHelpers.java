package aldor.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AldorErrorTextHelpers {
	static final private Pattern POS_PATTERN = Pattern.compile("^\\[L([0-9]+) C([0-9]+)\\].*");
	static final Pattern SEV_PATTERN = Pattern.compile("^[^(]*\\(([A-Za-z]+)\\).*");

	public static int[] parsePosition(String line) {
		assert !line.endsWith("\n");
		Matcher matcher = POS_PATTERN.matcher(line);
		if (!matcher.matches())
			return null;
		return new int[] { Integer.parseInt(matcher.group(1)),
				Integer.parseInt(matcher.group(2)) };
	}

	public static AldorErrorSeverity parseSeverity(String line) {
		assert !line.endsWith("\n");
		Matcher matcher = SEV_PATTERN.matcher(line);
		if (!matcher.matches())
			return AldorErrorSeverity.ERROR;
		else {
			String levelText = matcher.group(1);
			return AldorErrorSeverity.lookup(levelText);
		}
	}

}
