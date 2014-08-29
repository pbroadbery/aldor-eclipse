package aldor.command.output.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import aldor.command.output.AldorErrorSeverity;
import aldor.command.output.AldorErrorTextHelpers;

public class AldorErrorTextHelpersTest {
	@Test
	public void test1() {
		int[] pos = parsePosition("[L1 C2] Mumble");
		assertEquals(1, pos[0]);
		assertEquals(2, pos[1]);
	}

	@Test
	public void test2() {
		int[] pos = parsePosition("[L1 C2 Mumble");
		assertNull(pos);
	}

	@Test
	public void test3() {
		int[] pos = parsePosition("[");
		assertNull(pos);
	}

	public void test4() {
		int[] pos = parsePosition("[L1 C25] Mumble");
		assertEquals(1, pos[0]);
		assertEquals(25, pos[1]);
	}

	public void test6() {
		int[] pos = parsePosition("[L1 C25] #1 (Error) It's still all broken");
		assertEquals(11, pos[0]);
		assertEquals(25, pos[1]);
	}

	@Test
	public void testSeverity() {
		AldorErrorSeverity sev = AldorErrorTextHelpers.parseSeverity("[L11 C12] #22 (Error) This is borked");
		assertEquals(AldorErrorSeverity.ERROR, sev);
	}
	
	@Test
	public void testWarningSeverity() {
		AldorErrorSeverity sev = AldorErrorTextHelpers.parseSeverity("[L11 C12] #22 (Warning) This is slightly borked");
		assertEquals(AldorErrorSeverity.WARN, sev);
	}
	
	private int[] parsePosition(String text) {
		int[] p = AldorErrorTextHelpers.parsePosition(text);
		if (p != null) {
			assertEquals(2, p.length);
		}
		return p;
	}
}
