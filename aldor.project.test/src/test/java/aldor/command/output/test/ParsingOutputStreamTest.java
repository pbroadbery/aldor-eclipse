package aldor.command.output.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import aldor.command.output.ParsingOutputStream;
import aldor.util.event.EventCounter;
import aldor.util.event.EventStreamListBuilder;

import com.google.common.collect.Lists;

public class ParsingOutputStreamTest {

	@Test
	public void testSimpleCase() throws IOException {
		OutputStream os = new ParsingOutputStream();
		os.write(64);
		os.close();
	}

	@Test
	public void testLineCounts() throws IOException {
		assertEquals(0, countLines(""));
		assertEquals(1, countLines("hello\n"));
		assertEquals(2, countLines("hello\nThere\n"));
		assertEquals(1, countLines("hello\nThere")); // unterminated final lines are dropped.
	}
	
	private int countLines(String txt) throws IOException {
		ParsingOutputStream os = new ParsingOutputStream();
		EventCounter<String> counter = new EventCounter<String>();
		os.addListener(counter);
		InputStream source = new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8));
		int b;
		while ( (b=source.read()) != -1) {
			os.write(b);
		}
		os.close();
		return counter.total();
	}

	@Test
	public void testContent() throws IOException {
		assertEquals(Lists.newArrayList(), toList(""));
		assertEquals(Lists.newArrayList("hello\n"), toList("hello\n"));
		assertEquals(Lists.newArrayList("hello\n", "there\n"), toList("hello\nthere\n"));
		assertEquals(Lists.newArrayList("\n", "zzz\n", "\n"), toList("\nzzz\n\n"));
	}
	
	private List<String> toList(String txt) throws IOException {
		ParsingOutputStream os = new ParsingOutputStream();
		EventStreamListBuilder<String> lister = new EventStreamListBuilder<String>();
		os.addListener(lister);
		InputStream source = new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8));
		int b;
		while ( (b=source.read()) != -1) {
			os.write(b);
		}
		os.close();
		return lister.all();
	}

	
	
}
