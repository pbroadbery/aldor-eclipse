package aldor.command.output;

import aldor.util.event.EventSource;

public class AldorErrorMessageParser extends EventSource<AldorError> implements AldorLineEventListener {
	AldorError currentError;

	@Override
	public void onEvent(String line) {
		assert !line.endsWith("\n");
		if (line.length() == 0)
			return;
		if (line.startsWith("\"")) {
			flushCurrentError();
		} else if (line.startsWith("[")) {
			flushCurrentError();
			currentError = createAldorError(line);
		} else if (currentError != null) {
			currentError.addDetail(line);
		}

	}

	private AldorError createAldorError(String line) {
		String text = line.substring(0, line.length() - 1);
		if (line.startsWith("[")) {
			int[] spos = AldorErrorTextHelpers.parsePosition(text);
			AldorErrorSeverity severity = AldorErrorTextHelpers.parseSeverity(text);
			return new AldorError(spos, severity, text);
		} else {
			AldorErrorSeverity severity = AldorErrorTextHelpers.parseSeverity(text);
			return new AldorError(null, severity, text);
		}
	}

	@Override
	public void completed() {
		if (currentError != null)
			flushCurrentError();
		super.completed();
	}

	@Override
	public void onError(RuntimeException e) {
		super.error(e);
	}

	private void flushCurrentError() {
		if (currentError != null) {
			super.event(currentError);
			currentError = null;
		}
	}

}