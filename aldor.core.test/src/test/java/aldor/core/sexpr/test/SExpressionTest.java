package aldor.core.sexpr.test;

import static aldor.util.SExpression.cons;
import static aldor.util.SExpression.integer;
import static aldor.util.SExpression.nil;
import static aldor.util.SExpression.read;
import static aldor.util.SExpression.string;
import static aldor.util.SExpression.symbol;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import aldor.util.SExpression;
import aldor.util.sexpr.Type;

public class SExpressionTest {

	@Test
	public void testInt() {
		SExpression sx = integer(22);
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	@Test
	public void testString() {
		SExpression sx = string("hello");
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	@Test
	public void testSymbol() {
		SExpression sx = symbol("hello");
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	@Test
	public void testList() {
		SExpression sx = cons(symbol("hello"), integer(22));
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	@Test
	public void testList2() {
		SExpression sx = cons(symbol("hello"), nil());
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	@Test
	public void testList3() {
		SExpression sx = cons(symbol("hello"), cons(integer(22), nil()));
		SExpression sx2 = writeThenRead(sx);
		assertEquals(sx, sx2);
	}

	private SExpression writeThenRead(SExpression sx) {
		StringWriter w = new StringWriter();
		try {
			sx.write(w);
			String written = w.toString();
			System.out.println("SX: " + written);
			SExpression sx2 = read(new StringReader(written));
			return sx2;
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Test
	public void testEscaped() {
		SExpression sx = read(new StringReader("(|hello| 22)"));
		assertTrue(sx.isOfType(Type.Cons));

		assertEquals("hello", sx.car().symbol());
		assertEquals(22, sx.cdr().car().integer());
		assertTrue(sx.cdr().cdr().isNull());
	}

	public void testEmptyFile() {
		try {
			SExpression.read(new StringReader(""));
			Assert.fail();
		}
		catch (RuntimeException e) {

		}
	}
}
