package aldor.language.type;

import aldor.util.SExpression;

public abstract class SxPrimitive<T> {
	@SuppressWarnings("unused")
	final private Class<T> clss;

	public SxPrimitive(Class<T> clss) {
		this.clss = clss;
	}

	abstract T convert(SExpression sx);
}
