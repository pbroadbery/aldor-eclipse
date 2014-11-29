package aldor.language.type;

import java.util.HashMap;
import java.util.Map;

import aldor.util.SExpression;

class SymbolProperties {
	SymbolPropertyDescriptors symbolPropertyDescriptors = SymbolPropertyDescriptors.instance();
	Map<SymbolPropertyDescriptor<?>, SymbolProperty<?>> propertyForDescriptor = new HashMap<>();

	public SymbolProperties(SExpression sx) {
		for (SExpression prop : sx.asList()) {
			String name = prop.car().symbol();
			SymbolPropertyDescriptor<?> desc = symbolPropertyDescriptors.get(name);
			if (desc == null) {
				throw new RuntimeException("Unknown property: " + name);
			}
			add(desc, prop.cdr());
		}
	}

	public <T> T get(SymbolPropertyDescriptor<T> descriptor) {
		SymbolProperty<?> prop0 = propertyForDescriptor.get(descriptor);
		if (prop0 == null) {
			return descriptor.defaultValue();
		}
		SymbolProperty<T> prop = prop0.castTo(descriptor);
		return prop.value;
	}

	public <T> void add(SymbolPropertyDescriptor<T> desc, SExpression sx) {
		propertyForDescriptor.put(desc, new SymbolProperty<T>(desc, desc.convert(sx)));
	}

}