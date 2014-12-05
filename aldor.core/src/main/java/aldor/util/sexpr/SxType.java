package aldor.util.sexpr;

import aldor.util.SExpression;
import aldor.util.sexpr.SExpressionTypes.Cons;
import aldor.util.sexpr.SExpressionTypes.IntegerAtom;
import aldor.util.sexpr.SExpressionTypes.Nil;
import aldor.util.sexpr.SExpressionTypes.StringAtom;

public class SxType<T extends SExpression> {
	public static final SxType<SExpression> Any = new SxType<>("Any", SExpression.class);
	public static final SxType<Cons> Cons = new SxType<>("Cons", Cons.class);
	public static final SxType<IntegerAtom> Integer = new SxType<>("Integer", IntegerAtom.class);
	public static final SxType<StringAtom> String = new SxType<>("String", StringAtom.class);
	public static final SxType<SExpression> Symbol = new SxType<>("Symbol", SExpression.class);
	public static final SxType<Nil> Nil = new SxType<>("Nil", Nil.class);
	private final Class<T> clss;
	private final java.lang.String name;

	public SxType(String string, Class<T> class1) {
		this.name = string;
		this.clss = class1;
	}

	@Override
	public java.lang.String toString() {
		return this.name;
	}

	public T cast(SExpression sx) {
		if (sx.isOfType(this)) {
			return this.clss.cast(sx);
		}
		throw new RuntimeException("Cast wanted a " + this + " got: " + sx.type());
	}



}