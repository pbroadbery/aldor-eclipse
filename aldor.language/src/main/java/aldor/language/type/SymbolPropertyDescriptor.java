package aldor.language.type;

import aldor.util.SExpression;

class SymbolPropertyDescriptor<T> {
	final private String name;
	final private SxPrimitive<T> primitive;
	final private T defaultValue;

	public SymbolPropertyDescriptor(SymbolPropertyDescriptors descs, SxPrimitive<T> primitive, String name) {
		this(descs, primitive, name, null);
	}
	public SymbolPropertyDescriptor(SymbolPropertyDescriptors descs, SxPrimitive<T> primitive, String name, T defaultValue) {
		this.primitive = primitive;
		this.name = name;
		this.defaultValue = defaultValue;
		descs.register(this);
	}

	public T convert(SExpression sx) {
		return primitive.convert(sx);
	}

	public SxPrimitive<T> primitive() {
		return primitive;
	}

	public T defaultValue() {
		return defaultValue;
	}

	public String name() {
		return name;
	}
}