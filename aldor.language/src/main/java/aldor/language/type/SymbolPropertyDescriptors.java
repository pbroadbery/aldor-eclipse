package aldor.language.type;

import java.util.HashMap;
import java.util.Map;

import aldor.util.SExpression;

class SymbolPropertyDescriptors {
	private final static SymbolPropertyDescriptors instance = new SymbolPropertyDescriptors();
	private final Map<String, SymbolPropertyDescriptor<?>> descriptorForName = new HashMap<>();

	private final SxPrimitive<Integer> integer = new SxPrimitive<Integer>(Integer.class) {
		@Override
		Integer convert(SExpression sx) {
			return sx.integer();
		}};
	private final SxPrimitive<String> string = new SxPrimitive<String>(String.class) {
		@Override
		String convert(SExpression sx) {
			return sx.string();
	}};
	private final SxPrimitive<Boolean> boolean_asInt = new SxPrimitive<Boolean>(Boolean.class) {
		@Override
		Boolean convert(SExpression sx) {
			return sx.integer() == 1;
	}};
	private final SxPrimitive<SExpression> sx = new SxPrimitive<SExpression>(SExpression.class) {
		@Override
		SExpression convert(SExpression sx) {
			return sx;
	}};

	SymbolPropertyDescriptor<String> documentation = new SymbolPropertyDescriptor<>(this, string, "documentation");
	SymbolPropertyDescriptor<Integer> symeNameCode = new SymbolPropertyDescriptor<>(this, integer, "symeNameCode", -1);
	SymbolPropertyDescriptor<Integer> symeTypeCode = new SymbolPropertyDescriptor<>(this, integer, "symeTypeCode", -1);
	SymbolPropertyDescriptor<Boolean> _default = new SymbolPropertyDescriptor<>(this, boolean_asInt, "default", false);
	SymbolPropertyDescriptor<SExpression> domExports = new SymbolPropertyDescriptor<>(this, sx, "domExports", SExpression.nil());
	SymbolPropertyDescriptor<SExpression> catExports = new SymbolPropertyDescriptor<>(this, sx, "catExports", SExpression.nil());
	SymbolPropertyDescriptor<SExpression> condition = new SymbolPropertyDescriptor<>(this, sx, "condition", SExpression.nil());


	public final static SymbolPropertyDescriptors instance() {
		return instance;
	}

	public SymbolPropertyDescriptors() {
	}

	void register(SymbolPropertyDescriptor<?> prop) {
		this.descriptorForName.put(prop.name(), prop);
	}

	public SymbolPropertyDescriptor<?> get(String name) {
		return descriptorForName.get(name);
	}
}