package aldor.command.output;

import java.io.IOException;
import java.io.OutputStream;

import aldor.util.event.EventListener;
import aldor.util.event.EventSource;

import com.google.common.io.*;

public class ParsingOutputStream extends OutputStream {
	private StringBuilder builder = new StringBuilder();
	private EventSource<String> eventHelper = new EventSource<>();
	private OutputStream next;

	public ParsingOutputStream() {
		this.next = ByteStreams.nullOutputStream();
	}

	public ParsingOutputStream(OutputStream next) {
		this.next = next;
	}

	@Override
	public void write(int b) throws IOException {
		builder.append((char) b);
		checkLine();
		next.write(b);
	}

	@Override
	public void close() throws IOException {
		eventHelper.completed();
		next.close();
	}

	// As written, this will not deal with unterminated last lines.
	// .. which shouldn't happen anyway.
	private void checkLine() {
		if (builder.charAt(builder.length() - 1) == '\n') {
			System.out.println("newline!");
			eventHelper.event(builder.substring(0, builder.length()-1));
			builder.setLength(0);
		}
	}

	public EventListener<String> addListener(
			EventListener<String> listener) {
		return eventHelper.addListener(listener);
	}

	public void removeListener(
			EventListener<String> listener) {
		eventHelper.removeListener(listener);
	}
}