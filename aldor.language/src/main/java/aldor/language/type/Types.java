package aldor.language.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;

import com.google.common.collect.Lists;

public class Types {

	static class TypeKind<T extends ConcreteType<T>> {
		static final TypeKind<Define> define = new TypeKind<Define>(Define.class, "Define", false);
		static final TypeKind<Declare> declare = new TypeKind<Declare>(Declare.class, "Declare", false);
		static final TypeKind<Map> map = new TypeKind<Map>(Map.class, "Map");
		static final TypeKind<Cross> cross = new TypeKind<Cross>(Cross.class, "Cross");
		static final TypeKind<General> general = new TypeKind<General>(General.class, "General");
		static final TypeKind<With> with = new TypeKind<With>(With.class, "With");
		static final TypeKind<Error> error = new TypeKind<Error>(Error.class, "Error");
		static final TypeKind<Third> third = new TypeKind<Third>(Third.class, "Third");
		static final TypeKind<Exports> exports = new TypeKind<Exports>(Exports.class, "Exports");

		private final Class<T> clss;
		private final String name;
		private final boolean hasExports;

		// , Declare, Map, General;

		public TypeKind(Class<T> clss, String name) {
			this(clss, name, true);
		}

		public TypeKind(Class<T> clss, String name, boolean hasExports) {
			this.clss = clss;
			this.name = name;
			this.hasExports = hasExports;
		}

		public Class<T> clss() {
			return clss;
		}

		public String name() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public boolean hasScope() {
			return hasExports;
		}

	}

	public static abstract class ConcreteType<T extends ConcreteType<T>> implements Type {
		TypeKind<T> kind;
		AbSyn absyn;

		ConcreteType(TypeKind<T> kind, Scope parentScope, AbSyn absyn) {
			assert parentScope != null;
			this.kind = kind;
			this.absyn = absyn;
		}

		@Override
		public <X extends ConcreteType<X>> boolean isOfKind(TypeKind<X> kind) {
			return kind == this.kind;
		}

		@Override
		public <X extends ConcreteType<X>> X asKind(TypeKind<X> kind) {
			if (isOfKind(kind)) {
				return kind.clss().cast(this);
			}
			return null;
		}

		@Override
		public void addExport(ExportKind kind, SymbolMeaning sym) {
			throw new RuntimeException("" + this.kind + " does not support adding exports");
		}

		@Override
		public List<SymbolMeaning> exports(ExportKind kind) {
			throw new RuntimeException("" + this.kind + " has no " + kind + " exports " + this.toAbSyn());
		}

		@Override
		public TypeKind<?> kind() {
			return kind;
		}

		@Override
		public final AbSyn toAbSyn() {
			return absyn;
		}

		abstract boolean isTypeEqualModPercent(Scope scope, T other);
	}

	public static class Third extends ConcreteType<Third> {

		private Type inner;

		Third(Scope parentScope, Type inner, AbSyn whole) {
			super(TypeKind.third, parentScope, whole);
			this.inner = inner;
		}

		public Type inner() {
			return inner;
		}
		@Override
		public boolean isTypeEqualModPercent(Scope scope, Third type) {
			throw new RuntimeException("Not implemented");
		}
	}

	public static class Define extends ConcreteType<Define> {
		final private AbSyn value;
		final private Type type;

		Define(Scope scope, Type type, AbSyn whole, AbSyn value) {
			super(TypeKind.define, scope, whole);
			this.value = value;
			this.type = type;
		}

		public AbSyn value() {
			return value;
		}

		public Type type() {
			return type;
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, Define type) {
			throw new RuntimeException("Not implemented");
		}
	}

	public static class Declare extends ConcreteType<Declare> {
		private final SymbolMeaning syme;
		private final Type type;

		public Declare(Scope scope, SymbolMeaning symbolMeaning, AbSyn whole, Type type) {
			super(TypeKind.declare, scope, whole);
			this.syme = symbolMeaning;
			this.type = type;
		}

		public Type type() {
			return type;
		}

		public SymbolMeaning declaredSymbol() {
			return syme;
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, Declare type) {
			throw new RuntimeException("Not implemented");
		}

	}

	public static class Map extends ConcreteType<Map> {
		private Type params;
		private Type rets;

		Map(Scope parentScope, AbSyn whole, Type params, Type rets) {
			super(TypeKind.map, parentScope, whole);
			this.params = params;
			this.rets = rets;
		}

		public Type params() {
			return params;
		}

		public Type rets() {
			return rets;
		}

		public List<Type> paramList() {
			if (params.isOfKind(TypeKind.cross)) {
				return params.asKind(TypeKind.cross).asList();
			}
			return Collections.singletonList(params);
		}

		public List<Type> retList() {
			if (rets.isOfKind(TypeKind.cross)) {
				return rets.asKind(TypeKind.cross).asList();
			}
			return Collections.singletonList(rets);
		}

		public int retCount() {
			return retList().size();
		}
		@Override
		public boolean isTypeEqualModPercent(Scope scope, Map type) {
			return scope.typeSystem().isTypeEqualModPercent(scope, this.rets(), type.rets())
					&& scope.typeSystem().isTypeEqualModPercent(scope, this.params(), type.params());
		}
	}

	public static class Cross extends ConcreteType<Cross> {
		final List<Type> types;

		Cross(Scope scope, AbSyn whole, Type type1, Type type2) {
			super(TypeKind.cross, scope, whole);
			types = Lists.newArrayList(type1, type2);
		}

		public Cross(Scope scope, AbSyn absyn, List<Type> children) {
			super(TypeKind.cross, scope, absyn);
			types = Lists.newArrayList(children);
		}

		public List<Type> asList() {
			return types;
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, Cross type) {
			if (this.length() != type.length()) {
				return false;
			}
			for (int i=0; i<length(); i++) {
				if (!scope.typeSystem().isTypeEqualModPercent(scope, component(i), type.component(i))) {
					return false;
				}
			}
			return true;
		}

		private Type component(int i) {
			return types.get(i);
		}

		private int length() {
			return types.size();
		}

	}

	static class General extends ConcreteType<General> {
		private final AbSyn syntax;

		public General(Scope parentScope, AbSyn absyn) {
			super(TypeKind.general, parentScope, absyn);
			this.syntax = absyn;
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, General type) {
			return scope.typeSystem().isAbSynEqualModPercent(scope, this.absyn, type.absyn);
		}

	}

	public static class With extends ConcreteType<With> {
		SymbolMeaning self;
		List<SymbolMeaning> catExports = new ArrayList<SymbolMeaning>();
		private Scope bindingScope;

		With(Scope parentScope, AbSyn whole) {
			super(TypeKind.with, parentScope, whole);
			bindingScope = new Scope(parentScope, ScopeType.CAT);
			this.self = new SymbolMeaning(this.bindingScope, "%", parentScope.typeSystem().tfType());
		}

		public Type self() {
			return null;
		}

		@Override
		public void addExport(ExportKind kind, SymbolMeaning sym) {

		}

		public Scope bindingScope() {
			return bindingScope;
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, With type) {
			throw new RuntimeException("Not implemented");
		}
	}

	static class LazyType extends ForwardingType {
		private final String id;
		private final Scope scope;
		private Type value;

		LazyType(Scope scope, String id) {
			this.scope = scope;
			this.id = id;
		}

		@Override
		public Type doFollow() {
			return value = scope.get(id).value().meaning();
		}
	}

	static class LazySyntaxType extends ForwardingType {
		private Scope scope;
		private AbSyn absyn;

		LazySyntaxType(Scope scope, AbSyn absyn) {
			this.scope = scope;
			this.absyn = absyn;
		}

		@Override
		public Type doFollow() {
			return syntaxFromAbSyn(scope, absyn);
		}

		AbSyn absyn() {
			return absyn;
		}
	}

	abstract static class ForwardingType implements Forward<Type>, Type {
		Type value;

		@Override
		public <X extends ConcreteType<X>> boolean isOfKind(TypeKind<X> kind) {
			return follow().isOfKind(kind);
		}

		@Override
		public <T extends ConcreteType<T>> T asKind(TypeKind<T> kind) {
			return follow().asKind(kind);
		}

		@Override
		public void addExport(ExportKind kind, SymbolMeaning sym) {
			follow().addExport(kind, sym);
		}

		@Override
		public List<SymbolMeaning> exports(ExportKind kind) {
			return follow().exports(kind);
		}

		abstract Type doFollow();

		@Override
		public final Type follow() {
			if (value == null)
				value = doFollow();
			return value;
		}
		@Override
		public TypeKind<?> kind() {
			return follow().kind();
		}

		@Override
		public AbSyn toAbSyn() {
			return follow().toAbSyn();
		}

	}

	public static class Error extends ConcreteType<Error> {

		Error(Scope scope) {
			super(TypeKind.error, scope, null);
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, Error type) {
			throw new RuntimeException("Not implemented");
		}
	}

	public static class Exports extends ConcreteType<Exports> {
		private final SymeList domExports = new SymeList();
		private final SymeList catExports = new SymeList();
		private Scope bindingScope;
		Exports(Scope scope, String name) {
			super(TypeKind.exports, scope, null);
			this.bindingScope = new Scope(scope, ScopeType.CAT);
		}

		@Override
		public void addExport(ExportKind kind, SymbolMeaning sym) {
			this.bindingScope.bindSymbol(sym);
			if (kind == ExportKind.DOM)
				domExports.add(sym);
			else
				catExports.add(sym);
		}

		@Override
		public List<SymbolMeaning> exports(ExportKind kind) {
			if (kind == ExportKind.DOM)
				return domExports.asList();
			if (kind == ExportKind.CAT)
				return catExports.asList();
			throw new RuntimeException();
		}

		@Override
		public boolean isTypeEqualModPercent(Scope scope, Exports type) {
			throw new RuntimeException("Not implemented");
		}

		public Scope scope() {
			return bindingScope;
		}
	}

	static Type syntaxFromAbSyn(Scope scope, AbSyn absyn) {
		return absyn.toType(scope);
	}

}
