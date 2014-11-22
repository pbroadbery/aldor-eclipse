package aldor.util;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharacterStream implements Stream<Character> {
	Exception exception;
	int c;
	private Reader reader;

	public ReaderCharacterStream(Reader stream) {
		this.reader = stream;
		c = -2;
	}

	@Override
	public Character peek() {
		if (c == -2) {
			try {
				c = reader.read();
			} catch (IOException exception) {
				this.exception = exception;
			}
		}
		return (char) c;
	}

	@Override
	public void next() {
		c = -2;
	}

	@Override
	public boolean hasNext() {
		peek();
		return c != -1;
	}

	public boolean isInError() {
		peek();
		return exception != null;
	}

	public void throwError() {
		throw new RuntimeException(exception);
	}

}
