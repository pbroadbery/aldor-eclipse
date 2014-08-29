package aldor.command.output.test;

import static org.junit.Assert.*;

import org.junit.Test;

import aldor.command.output.AldorError;
import aldor.command.output.AldorErrorMessageParser;
import aldor.util.event.EventCounter;
import aldor.util.event.EventSinkLast;

public class AldorErrorMessageParserTest {

	@Test
	public void test() {
		AldorErrorMessageParser parser = new AldorErrorMessageParser();
		EventSinkLast<AldorError> sink = new EventSinkLast<AldorError>();
		parser.addListener(sink);
		parser.onEvent("this is a line\n");
		parser.onEvent("[L1 C2] #1 (Warning) You smell of elderberries\n");
		parser.completed();
		assertNotNull(sink.last());
		
	}
	
	@Test
	public void test2() {
		AldorErrorMessageParser parser = new AldorErrorMessageParser();
		EventSinkLast<AldorError> sink = new EventSinkLast<AldorError>();
		EventCounter<AldorError> counter = new EventCounter<>();
		parser.addListener(sink);
		parser.addListener(counter);
		parser.onEvent("this is a line");
		parser.onEvent("[L1 C2] #1 (Error) You are a hamster");
		parser.onEvent("[L2 C3] #1 (Warning) You smell of elderberries");
		assertNotNull(sink.last());
		parser.completed();
		assertEquals(2, counter.total());
		
	}

	
	@Test
	public void test3() {
		AldorErrorMessageParser parser = new AldorErrorMessageParser();
		EventSinkLast<AldorError> sink = new EventSinkLast<AldorError>();
		EventCounter<AldorError> counter = new EventCounter<>();
		parser.addListener(sink);
		parser.addListener(counter);
		parser.onEvent("this is a line");
		parser.onEvent("[L1 C2] #1 (Error) You are a hamster");
		parser.onEvent("  This is a continuation");
		parser.completed();
		assertNotNull(sink.last());
		assertEquals(1, counter.total());
		AldorError lastError = sink.last();
		assertEquals(1, lastError.details().size());
		
	}
	
}
