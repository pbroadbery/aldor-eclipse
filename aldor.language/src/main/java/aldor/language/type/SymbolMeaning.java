package aldor.language.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import aldor.language.type.AbstractSyntax.AbId;
import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.Type.ExportKind;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.Types.TypeKind;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

class SymbolMeaning {
	enum ConstructorType {
		TypeSelfReference, Std
	}

	private final String name;
	private final Type type;
	private final Scope scope;
	private Type effectiveValue;
	private int typeHashCode;
	private String documentation;
	private boolean isDefault;
	private int srcpos;

	public SymbolMeaning(SymbolMeaning.ConstructorType consType, Scope scope, String id, Type type, AbSyn value) {
		this.name = id;
		this.scope = scope;
		this.type = typeForConstructor(consType, id, type, value);

		assert this.scope != null;
		assert this.name != null;
		assert this.type != null;
	}

	public void effectiveValue(Type type) {
		this.effectiveValue = type;
	}

	public Type effectiveValue() {
		return this.effectiveValue;
	}

	public SymbolMeaning(Scope scope, String string, Type type) {
		this(scope, string, type, null);
	}

	public SymbolMeaning(Scope scope, String string, Type type, AbSyn value) {
		this(ConstructorType.Std, scope, string, type, value);
	}

	private Type typeForConstructor(SymbolMeaning.ConstructorType consType, String name, Type type, AbSyn value) {
		if (consType == ConstructorType.TypeSelfReference) {
			assert type == null;
			return new Types.General(scope, new AbId(name));
		}
		if (value == null) {
			assert type != null;
			return type;
		}
		TypeSystem sys = scope.typeSystem();
		return sys.tfDefine(scope, null, sys.tfDeclare(scope, null, this, type), value);
	}

	public void isDefault(boolean flg) {
		this.isDefault = flg;
	}

	public void typeCode(Integer integer) {
		this.typeHashCode = integer;
	}

	public void documentation(String documentation) {
		this.documentation = documentation;
	}

	public void srcpos(int srcpos) {
		this.srcpos = srcpos;
	}

	@Override
	public String toString() {
		return "{S:" + scope + "> " + name + (srcpos() == -1 ? "": ":" + srcpos()) + "}";
	}

	private int srcpos() {
		return srcpos;
	}

	public AbSyn value() {
		Types.Define define = this.type.asKind(TypeKind.define);
		if (define == null)
			return null;
		return define.value();
	}

	public Type type() {
		return type;
	}

	public String name() {
		return name;
	}

	public void setEffectiveValue(Type type) {
		this.effectiveValue = type;
	}

	static class Export {
		final SymbolMeaning declaration;
		final List<SymbolMeaning> definitions;

		public Export(SymbolMeaning declaration, List<SymbolMeaning> definitions) {
			this.declaration = declaration;
			this.definitions = definitions;
		}

		@Override
		public String toString() {
			return "{EXP: " + declaration + " --> " + definitions + "}";
		}
	}

	public Collection<Export> exports() {
		Type type = this.type();

		while (type.isOfKind(TypeKind.map)) {
			type = type.asKind(TypeKind.map).rets();
		}
		if (!type.isOfKind(TypeKind.define)) {
			return Collections.emptyList();
		}

		final AbSyn absyn = type.asKind(TypeKind.define).value();
		final Type rhsType = absyn.toType(this.scope);

		return Collections2.transform(this.effectiveValue().exports(ExportKind.DOM), new Function<SymbolMeaning, Export>() {

			@Override
			public Export apply(SymbolMeaning input) {
				return input.exportForSymbolMeaning(rhsType);
			}
		});
	}

	/** Take an export of a category and attempt to match up with domain level exports */
	private Export exportForSymbolMeaning(Type rhsType) {
		final List<SymbolMeaning> domainExports = rhsType.exports(ExportKind.DOM);
		final List<SymbolMeaning> definitions = new ArrayList<>(1);
		System.out.println("(Finding exports of" + this + " " + this.type().toAbSyn() + " " + domainExports);
		for (SymbolMeaning domainExport : domainExports) {
			if (!domainExport.name().equals(name()))
				continue;
			if (this.scope.typeSystem().isTypeEqualModPercent(scope, this.type(), domainExport.type())) {
				definitions.add(domainExport);
				System.out.println("Found" + domainExport.type());
			}
		}
		System.out.println("Found " + definitions.size() + ")");
		return new Export(this, definitions);
	}

}