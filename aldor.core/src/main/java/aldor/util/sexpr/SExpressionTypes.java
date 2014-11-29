package aldor.util.sexpr;

import java.io.IOException;
import java.io.Writer;

import aldor.util.SExpression;

public class SExpressionTypes {
	public static class Cons extends SExpression {
		SExpression car;
		SExpression cdr;

		public Cons(SExpression car, SExpression cdr) {
			super(SxType.Cons);
			this.car = car;
			this.cdr = cdr;
		}

		@Override
		protected boolean innerEqual(SExpression other) {
			SExpression otherCons = other;
			return car.equals(otherCons.car()) && cdr.equals(otherCons.cdr());
		}

		@Override
		public final SExpression car() {
			return car;
		}

		@Override
		public final SExpression cdr() {
			return cdr;
		}

		@Override
		public void setCar(SExpression car) {
			this.car = car;
		}

		@Override
		public void setCdr(SExpression cdr) {
			this.cdr = cdr;
		}

		@Override
		public void write(Writer w) throws IOException {
			w.write("(");
			SExpression current = this;
			boolean done = false;
			while (!done) {
				current.car().write(w);

				if (current.cdr().isOfType(SxType.Cons)) {
					w.write(" ");
					current = current.cdr();
				} else if (current.cdr().isNull()) {
					done = true;
				} else {
					w.write(" . ");
					current.cdr().write(w);
					done = true;
				}
			}
			w.write(")");
		}

	}

	public static class AbstractAtom<T> extends SExpression {
		T value;

		AbstractAtom(SxType type, T value) {
			super(type);
			this.value = value;
		}

		protected T value() {
			return value;
		}

		@Override
		protected boolean innerEqual(SExpression other) {
			@SuppressWarnings("unchecked")
			AbstractAtom<T> otherAtom = (AbstractAtom<T>) other;
			return this.value().equals(otherAtom.value());
		}

	}

	public static class IntegerAtom extends AbstractAtom<Integer> {
		public IntegerAtom(int n) {
			super(SxType.Integer, n);
		}

		@Override
		public int integer() {
			return value();
		}

		@Override
		public void write(Writer w) throws IOException {
			w.append("" + value());
		}

	}

	public static class StringAtom extends AbstractAtom<String> {
		public StringAtom(String text) {
			super(SxType.String, text);
		}

		@Override
		public String string() {
			return value();
		}

		@Override
		public void write(Writer w) throws IOException {
			w.append("\"");
			w.append(string());
			w.append("\"");
		}
	}

	public static class SymbolAtom extends AbstractAtom<String> {
		public SymbolAtom(String text) {
			super(SxType.Symbol, text);
		}

		@Override
		public String symbol() {
			return value();
		}

		@Override
		public void write(Writer w) throws IOException {
			w.append(this.value());
		}
	}

	public static class Nil extends SExpression {
		public Nil() {
			super(SxType.Nil);
		}

		@Override
		public void write(Writer w) throws IOException {
			w.append("()");
		}

		@Override
		protected boolean innerEqual(SExpression other) {
			return true;
		}

	}

	final static SExpression nil = new Nil();

	public static SExpression nil() {
		return nil;
	}

}