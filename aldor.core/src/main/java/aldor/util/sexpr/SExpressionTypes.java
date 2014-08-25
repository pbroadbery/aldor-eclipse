package aldor.util.sexpr;

import java.util.List;

import aldor.util.SExpression;

public class SExpressionTypes {
	public static class ListExpression extends SExpression {
		List<SExpression> lst;

		ListExpression(List<SExpression> lst) {
			super(Type.List);
			this.lst = lst;
		}

		@Override
		public List<SExpression> list() {
			return lst;
		}
	}
	
	public static class AbstractAtom<T> extends SExpression {
		T value;
		AbstractAtom(Type type, T value) {
			super(type);
			this.value = value;
		}
		
		protected T value() {
			return value;
		}
	}
	
	public static class IntegerAtom extends AbstractAtom<Integer> {
		public IntegerAtom(int n) {
			super(Type.Integer, n);
		}
		
		@Override
		public Integer integer() {
			return value();
		}
	}
	
	public static class StringAtom extends AbstractAtom<String> {
		public StringAtom(String text) {
			super(Type.String, text);
		}
		
		@Override
		public String string() {
			return value();
		}
	}

	public static class SymbolAtom extends AbstractAtom<String> {
		public SymbolAtom(String text) {
			super(Type.Symbol, text);
		}
		
		@Override
		public String symbol() {
			return value();
		}
	}

}