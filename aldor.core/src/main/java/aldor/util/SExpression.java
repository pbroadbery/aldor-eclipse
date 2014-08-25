package aldor.util;

import java.util.List;

import aldor.util.sexpr.SExpressionTypes;
import aldor.util.sexpr.Type;

abstract public class SExpression {
	private Type type;
	
	public SExpression(Type type) {
		this.type = type;
	}

	Type type() {
		return type;
	}
	
	public List<SExpression> list() {
		throw new RuntimeException();
	}

	protected Integer integer() {
		throw new RuntimeException();
	}

	protected String string() {
		throw new RuntimeException();
	}

	protected String symbol() {
		throw new RuntimeException();
	}

	public static SExpression integer(Integer value) {
		return new SExpressionTypes.IntegerAtom(value);
	}

	public static SExpression string(String substring) {
		return new SExpressionTypes.StringAtom(substring);
	}

	public static SExpression symbol(String text) {
		return new SExpressionTypes.SymbolAtom(text);
	}

}
