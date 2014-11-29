package aldor.language.type;


class SymbolProperty<T> {
	SymbolPropertyDescriptor<T> desc;
	T value;

	public SymbolProperty(SymbolPropertyDescriptor<T> desc, T value) {
		this.desc = desc;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	<X> SymbolProperty<X> castTo(SymbolPropertyDescriptor<X> desc) {
		if (desc == this.desc) {
			return (SymbolProperty<X>) this;
		}
		throw new RuntimeException();
	}
}