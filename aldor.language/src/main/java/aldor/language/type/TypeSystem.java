package aldor.language.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import aldor.language.type.AbstractSyntax.AbDeclare;
import aldor.language.type.AbstractSyntax.AbKind;
import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.AbstractSyntax.ConcreteAbSyn;
import aldor.language.type.Type.ExportKind;
import aldor.language.type.Types.ConcreteType;
import aldor.language.type.Types.Exports;
import aldor.language.type.Types.LazySyntaxType;
import aldor.language.type.Types.LazyType;
import aldor.language.type.Types.TypeKind;
import aldor.util.SExpression;

public class TypeSystem {
	private static final AbTypes instance = AbTypes.instance();
	private final static SymbolPropertyDescriptors symbolPropertyDescriptors = SymbolPropertyDescriptors.instance();
	private final Scope rootScope;

	TypeSystem(String name) {
		rootScope = new Scope(this, ScopeType.TOP);
	}

	enum ScopeType {
		TOP(true), FILE(true), CAT(true), DOM(true), FUNC(false);
		final boolean needsExports;
		final boolean isTransparent;

		ScopeType(boolean needsExports) {
			this.needsExports = true;
			this.isTransparent = false;
		}

		public boolean needsExports() {
			return needsExports;
		}
	}

	static class Scope {
		private final SymeList boundSymbols;
		private final ScopeType scopeType;
		private final TypeSystem typeSystem;
		private final Scope parentScope;

		public Scope(TypeSystem typeSystem, ScopeType scopeType) {
			this.parentScope = null;
			this.typeSystem = typeSystem;
			this.scopeType = scopeType;
			this.boundSymbols = new SymeList();
		}

		Scope(Scope parentScope, ScopeType scopeType) {
			this.typeSystem = parentScope.typeSystem();
			this.parentScope = parentScope;
			this.scopeType = scopeType;
			this.boundSymbols = new SymeList();
		}

		@Override
		public String toString() {
			return "[" + scopeType + "]";
		}

		Type boundType(String name) {
			return typeSystem.tfLazy(this, name);
		}

		public SymbolMeaning get(String id) {
			Collection<SymbolMeaning> symbols = this.boundSymbolsForName(id);
			if (symbols.isEmpty())
				throw new BindingNotFoundException();
			if (symbols.size() != 1) {
				throw new RuntimeException("get should only be called for unique symbols: " + id + " " + symbols.size());
			}
			return symbols.iterator().next();
		}

		private Collection<SymbolMeaning> boundSymbolsForName(String id) {
			List<SymbolMeaning> lst = new ArrayList<>();
			for (SymbolMeaning sym: boundSymbols.symes) {
				if (sym.name().equals(id))
					lst.add(sym);
			}
			return lst;
		}

		void bindSymbol(SymbolMeaning symbol) {
			boundSymbols.add(symbol);
		}

		public SymeList boundSymbols() {
			return boundSymbols;
		}

		public TypeSystem typeSystem() {
			return typeSystem;
		}

		public ScopeType scopeType() {
			return scopeType;
		}

	}

	public Type tfLazy(Scope scope, String name) {
		return new Types.LazyType(scope, name);
	}

	public Type tfDeclare(Scope parentScope, AbSyn whole, SymbolMeaning symbolMeaning, Type type) {
		return new Types.Declare(parentScope, symbolMeaning, whole, type);
	}

	public Type tfDefine(Scope scope, AbSyn whole, Type type, AbSyn value) {
		return new Types.Define(scope, type, whole, value);
	}

	public Type tfWith() {
		return new Types.With(this.rootScope, this.abWithExpression());
	}

	public Type tfMap(Scope parentScope, AbSyn whole, Type params, Type rets) {
		return new Types.Map(parentScope, whole, params, rets);
	}

	public Type tfCross(Scope scope, AbSyn whole, Type type1, Type type2) {
		return new Types.Cross(scope, whole, type1, type2);
	}

	public Type tfFrAbSyn(Scope scope, AbSyn absyn) {
		return new Types.LazySyntaxType(scope, absyn);
	}

	public AbSyn abApplyId(SymbolMeaning operator, AbSyn arg0) {
		return new AbstractSyntax.AbApply(instance.apply, Collections.singletonList(arg0));
	}

	public Type tfType() {
		return new LazyType(rootScope, "SxType");
	}

	public SymbolMeaning createSymbolMeaning(Scope scope, String name, Type type, SymbolProperties symbolProperties) {
		SymbolMeaning theSymbol = new SymbolMeaning(scope, name, type);
		theSymbol.isDefault(symbolProperties.get(symbolPropertyDescriptors._default));
		theSymbol.typeCode(symbolProperties.get(symbolPropertyDescriptors.symeTypeCode));
		theSymbol.documentation(symbolProperties.get(symbolPropertyDescriptors.documentation));
		theSymbol.srcpos(symbolProperties.get(symbolPropertyDescriptors.srcpos));
		final List<SExpression> domExports = symbolProperties.get(symbolPropertyDescriptors.domExports).asList();

		final List<SExpression> catExports = symbolProperties.get(symbolPropertyDescriptors.catExports).asList();
		theSymbol.setEffectiveValue(createEffectiveType(scope, name, domExports, catExports));

		return theSymbol;
	}

	private Type createEffectiveType(Scope scope, String name, List<SExpression> domExports, List<SExpression> catExports) {
		Exports exporter = new Types.Exports(scope, name);
		for (SExpression exp: domExports) {
			AbSyn absyn = AbstractSyntax.parse(exp);
			AbDeclare declaration = absyn.asKind(AbTypes.instance().declare);
			Type type = scope.typeSystem().tfFrAbSyn(scope, declaration.type());
			SymbolMeaning syme = scope.typeSystem().createSymbolMeaning(scope, declaration.name(), type, declaration.properties());
			exporter.addExport(ExportKind.DOM, syme);
		}
		for (SExpression exp: catExports) {
			AbSyn absyn = AbstractSyntax.parse(exp);
			AbDeclare declaration = absyn.asKind(AbTypes.instance().declare);
			Type type = scope.typeSystem().tfFrAbSyn(scope, declaration.type());
			SymbolMeaning syme = scope.typeSystem().createSymbolMeaning(scope, declaration.name(), type, declaration.properties());
			exporter.addExport(ExportKind.CAT, syme);
		}
		return exporter;
	}


	public List<SymbolMeaning> tfCatExports(Type type) {
		if (type.isOfKind(TypeKind.declare)) {
			return tfCatExports(type.asKind(TypeKind.declare).type());
		}
		if (type.isOfKind(TypeKind.define)) {
			return tfCatExports(type.asKind(TypeKind.define).value().meaning());
		}
		return type.exports(Type.ExportKind.CAT);
	}

	protected Type tfThird(Scope parentScope, AbSyn whole, LazySyntaxType inner) {
		return new Types.Third(parentScope, inner, whole);
	}

	public AbSyn abWithExpression() {
		return new AbstractSyntax.AbBoundGeneric(instance.with, Collections.<AbSyn>emptyList());
	}

	public boolean isTypeEqualModPercent(Scope scope, Type type1, Type type2) {
		if (type1.isOfKind(TypeKind.declare))
			type1 = type1.asKind(TypeKind.declare).type();
		if (type2.isOfKind(TypeKind.declare))
			type2 = type2.asKind(TypeKind.declare).type();
		boolean flg = _isTypeEqualModPercent(scope, type1.kind(), type1, type2);
		System.out.println("Eq: " + type1.toAbSyn() + " " + type2.toAbSyn() + " --> " + flg);
		return flg;
	}

	// Only needed to get generics right without casting.
	private <T extends ConcreteType<T>> boolean _isTypeEqualModPercent(Scope scope, TypeKind<T> kind, Type type1, Type type2) {
		if (kind != type2.kind()) {
			return false;
		}

		T _type1 = type1.asKind(kind);
		T _type2 = type2.asKind(kind);

		return _type1.isTypeEqualModPercent(scope, _type2);
	}

	public boolean isAbSynEqualModPercent(Scope scope, AbSyn absyn1, AbSyn absyn2) {
		boolean flg = _isAbSynEqualModPercent(scope, absyn1.kind(), absyn1, absyn2);
		System.out.println("AbEq: " + absyn1 + " " + absyn2 + " --> " + flg);
		return flg;
	}

	public <T extends ConcreteAbSyn<T>> boolean _isAbSynEqualModPercent(Scope scope, AbKind<T> kind, AbSyn absyn1, AbSyn absyn2) {
		if (!absyn1.kind().equals(absyn2.kind())) {
			return false;
		}

		T _absyn1 = absyn1.asKind(kind);
		T _absyn2 = absyn2.asKind(kind);

		return _absyn1.isAbSynEqualModPercent(scope, _absyn2);

	}
}
/*
	static interface SxConverter {
		Type convert(Scope scope, SExpression sx);
	}

	SxConverter declareConverter = new SxConverter() {
		@Override
		public Type convert(Scope scope, SExpression input) {
			System.out.println("Declare: "  + input);
			final SExpression declareArgs = input.cdr();
			final SExpression sym = declareArgs.car();
			final SExpression type = declareArgs.cdr().car();

			AbSyn parsed = AbstractSyntax.parse(input);
			if (sym.isNull()) {
				return new Types.Declare(scope, null, parsed, new Types.LazySyntaxType(scope, parsed));
			}

			SymbolMeaning syme = createSymbolMeaning1(scope, parsed);
			scope.bindSymbol(syme);
			return new Types.Declare(scope, syme, parsed, syme.type());
		}

		SymbolMeaning createSymbolMeaning1(Scope scope, AbSyn absyn) {
			final SExpression declareArgs = parsed.cdr();
			final SExpression sym = declareArgs.car();
			final SExpression type = declareArgs.cdr().car();
			System.out.println("Declare Type: "  + type);
			final SExpression properties = declareArgs.cdr().cdr().car();
			final SymbolProperties symbolProperties = new SymbolProperties(properties);
			final SymbolMeaning syme = createSymbolMeaning(scope, sym.symbol(), parsed, symbolProperties);

			final List<SExpression> domExports = symbolProperties.get(symbolPropertyDescriptors.domExports).asList();

			final List<SExpression> catExports = symbolProperties.get(symbolPropertyDescriptors.catExports).asList();
			syme.setEffectiveValue(createEffectiveType(scope, syme.name(), domExports, catExports));
			return syme;
		}

		private Type createEffectiveType(Scope scope, String name, List<SExpression> domExports, List<SExpression> catExports) {
			Exports exporter = new Types.Exports(scope, name);
			for (SExpression exp: domExports) {
				SymbolMeaning syme = createSymbolMeaning1(scope, AbstractSyntax.parse(exp));
				exporter.addExport(ExportKind.DOM, syme);
			}
			for (SExpression exp: catExports) {
				SymbolMeaning syme = createSymbolMeaning1(scope, AbstractSyntax.parse(exp));
				exporter.addExport(ExportKind.CAT, syme);
			}
			return exporter;
		}
	};

	SxConverter defineConverter = new SxConverter() {

		@Override
		public Type convert(Scope scope, SExpression sx) {
			SExpression lhs = sx.cdr().car();
			SExpression rhs = sx.cdr().cdr().car();
			return new Types.Define(scope, new LazySyntaxType(scope, new AbSynSExpression(lhs)),
					new AbSynSExpression(rhs));
		}
	};
	SxConverter applyConverter = new SxConverter() {
		@Override
		public Type convert(Scope scope, SExpression sx) {
			SExpression applyOperator = sx.cdr().car();
			SxConverter opConverter = sxOpConverter(applyOperator.symbol());
			return opConverter.convert(scope, sx.cdr());
		}
	};

	SxConverter anyConverter = new SxConverter() {

		@Override
		public Type convert(Scope scope, SExpression sx) {
			return new General(scope, new AbSynSExpression(sx));
		}
	};

	SxConverter mapConverter = new SxConverter() {
		@Override
		public Type convert(Scope scope, SExpression sx) {
			Scope functionScope = new Scope(scope, ScopeType.FUNC);
			System.out.println("MapConverter: "  + sx);
			SExpression params = sx.cdr().car();
			SExpression rets = sx.cdr().cdr().car();
			return new Types.Map(scope, new LazySyntaxType(functionScope, new AbSynSExpression(params)),
					new LazySyntaxType(functionScope, new AbSynSExpression(rets)));
		}
	};

	SxConverter thirdConverter = new SxConverter() {
		@Override
		public Type convert(Scope scope, SExpression sx) {
			return tfThird(scope, new LazySyntaxType(scope, new AbSynSExpression(sx.cdr().car())));
		}
	};

	SxConverter applyAnyConverter = new SxConverter() {
		@Override
		public Type convert(Scope scope, SExpression sx) {
			System.out.println("Apply any: "+ sx);
			SExpression operator = sx.cdr().car();
			List<SExpression> args = sx.cdr().cdr().asList();
			List<AbSyn> abArgs = Lists.transform(args, new Function<SExpression, AbSyn>() {

				@Override
				public AbSyn apply(SExpression input) {
					return new AbSynSExpression(input);
				}});
			return new Types.General(scope, new AbSynApply(new AbSynSExpression(operator), abArgs));
		}
	};

	public SxConverter sxConverter(String name) {
		if (name.equals("Declare"))
			return declareConverter;
		else if (name.equals("Define"))
			return defineConverter;
		else if (name.equals("Apply"))
			return applyConverter;
		else
			return anyConverter;
	}

	public SxConverter sxOpConverter(String name) {
		if (name.equals("->"))
			return mapConverter;
		else if (name.equals("Third"))
			return thirdConverter;
		return applyAnyConverter;
	}
*/