package aldor.language.type;

import java.util.List;

import aldor.language.type.AbTypes.AbSynToTypeConverter;
import aldor.language.type.TypeSystem.Scope;
import aldor.util.SExpression;
import aldor.util.Strings;
import aldor.util.sexpr.SxType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AbstractSyntax {

	private static final AbTypes abTypes= AbTypes.instance();

	static abstract class AbKind<T extends ConcreteAbSyn<T>> {
		String name;
		Class<T> clss;
		AbSynToTypeConverter converter;

		public AbKind(AbTypes types, Class<T> class1, String name, AbSynToTypeConverter converter) {
			this.clss = class1;
			this.name = name;
			this.converter = converter;
			if (converter == null) {
				throw new RuntimeException();
			}
			types.register(this);
		}

		@Override
		public String toString() {
			return "{Kind: " + name + "}";
		}

		public String name() {
			return name;
		}

		public abstract AbSyn parse(SExpression sx);

		public Type toType(Scope scope, AbSyn absyn) {
			return converter.toType(scope, absyn);
		}
	}

	static public abstract class AbSyn {
		private final AbKind<?> kind;
		private Type meaning;

		AbSyn(AbKind<?> type) {
			this.kind = type;
		}

		@Override
		public abstract String toString();

		/**
		 * The kind denoted by this expression - for example, (-> Int Int) is
		 * Map(Int->Int)
		 */
		public Type meaning() {
			return meaning;
		}

		public void meaning(Type meaning) {
			this.meaning = meaning;
		}

		public Type toType(Scope scope) {
			return kind.toType(scope, this);
		}

		public AbKind<?> kind() {
			return kind;
		}

		public <T extends ConcreteAbSyn<T>> T asKind(AbKind<T> expected) {
			return expected.clss.cast(this);
		}

		public boolean isOfKind(AbKind<?> kind) {
			return this.kind == kind;
		}

		public String render() {
			StringBuilder sb = new StringBuilder();
			AbstractSyntax.render(sb, this);
			return sb.toString();
		}

	}

	abstract static class ConcreteAbSyn<T extends ConcreteAbSyn<T>> extends AbSyn {

		ConcreteAbSyn(AbKind<T> type) {
			super(type);
		}

	}

	static abstract class AbType1Generic<T extends ConcreteAbSyn<T>> extends AbKind<T> {

		public AbType1Generic(AbTypes types, Class<T> class1, String name, AbSynToTypeConverter converter) {
			super(types, class1, name, converter);
		}
		@Override
		public AbSyn parse(SExpression sx) {
			List<AbSyn> children = Lists.transform(sx.cdr().asList(), new Function<SExpression, AbSyn>() {

				@Override
				public AbSyn apply(SExpression input) {
					return AbstractSyntax.parse(input);
				}
			});

			AbSyn absyn = this.newInstance(children);

			return absyn;
		}

		protected abstract T newInstance(List<AbSyn> children);
	}

	static class AbTypeGeneric extends AbType1Generic<AbBoundGeneric> {

		public AbTypeGeneric(AbTypes abTypes, String name, AbSynToTypeConverter converter) {
			super(abTypes, AbBoundGeneric.class, name, converter);
		}

		@Override
		protected AbBoundGeneric newInstance(List<AbSyn> children) {
			return new AbBoundGeneric(this, children);
		}

	}

	static class AbTypeApply extends AbType1Generic<AbApply> {

		public AbTypeApply(AbTypes types, Class<AbApply> class1, String name, AbSynToTypeConverter converter) {
			super(types, class1, name, converter);
		}

		@Override
		protected AbApply newInstance(List<AbSyn> children) {
			return new AbApply(this, children);
		}


	}

	static class AbId extends ConcreteAbSyn<AbId> {
		String id;

		AbId(String id) {
			super(abTypes.id);
			this.id = id;
		}

		@Override
		public String toString() {
			return id;
		}

		public String id() {
			return id;
		}

	}

	static class AbGeneric<T extends ConcreteAbSyn<T>> extends ConcreteAbSyn<T> {
		private final List<AbSyn> children;

		public AbGeneric(AbKind<T> type, List<AbSyn> children) {
			super(type);
			this.children = children;
		};

		@Override
		public String toString() {
			return "(" + kind() + " " + Strings.toString(" ", children) + ")";
		}

		public List<AbSyn> children() {
			return children;
		}
	}

	static class AbBoundGeneric extends AbGeneric<AbBoundGeneric> {

		public AbBoundGeneric(AbKind<AbBoundGeneric> type, List<AbSyn> children) {
			super(type, children);
		}

	}

	static class AbApply extends AbGeneric<AbApply> {

		public AbApply(AbKind<AbApply> type, List<AbSyn> children) {
			super(type, children);
		}

		public AbSyn operator() {
			return children().get(0);
		}

		public AbSyn argument(int n) {
			return children().get(n+1);
		}

		public int argumentCount() {
			return children().size()-1;
		}


		public List<AbSyn> arguments() {
			return children().subList(1, children().size());
		}

	}

	static class AbDeclare extends ConcreteAbSyn<AbDeclare> {

		private final String sym;
		private final AbSyn type;
		private final SymbolProperties properties;

		public AbDeclare(String sym, AbSyn type, SymbolProperties properties) {
			super(AbTypes.instance().declare);
			this.sym = sym;
			this.type = type;
			this.properties = properties;
		}

		public static AbSyn parse(SExpression sx) {
			SExpression declareArgs = sx.cdr();
			SExpression sym = declareArgs.car();
			SExpression type = declareArgs.cdr().car();
			SExpression sxproperties = declareArgs.cdr().cdr().isNull() ? SExpression.nil() : declareArgs.cdr().cdr().car();
			SymbolProperties properties = new SymbolProperties(sxproperties);
			String name = sym.isNull() ? "_" : sym.symbol();
			return new AbDeclare(name, AbstractSyntax.parse(type), properties);
		}

		@Override
		public String toString() {
			return "(Declare...)";
		}

		public SymbolProperties properties() {
			return properties;
		}

		public AbSyn type() {
			return type;
		}

		public String name() {
			return sym;
		}

	}

	static AbSyn parse(SExpression sx) {
		if (sx.isOfType(SxType.Symbol)) {
			return new AbId(sx.symbol());
		}
		if (sx.isNull()) {
			return null;
		}
		SExpression typeId = sx.car();
		AbKind<?> type = AbTypes.instance().kindForId(typeId.symbol());
		return type.parse(sx);
	}

	public static boolean isTheMapSymbol(AbSyn ab) {
		return ab.isOfKind(abTypes.id) && ab.asKind(abTypes.id).id().equals("->");
	}

	public static void render(StringBuilder builder, AbSyn absyn) {
		if (absyn.isOfKind(abTypes.id)) {
			builder.append(absyn.asKind(abTypes.id).id());
		}
		else if (absyn.isOfKind(abTypes.apply)) {
			renderApply(builder, absyn);
		}
		else if (absyn.isOfKind(abTypes.comma)) {
			renderComma(builder, absyn);
		}
		else {
			builder.append("<<" + absyn.kind() + ">>");
		}
	}

	private static void renderComma(StringBuilder builder, AbSyn absyn) {
		AbBoundGeneric comma = absyn.asKind(abTypes.comma);
		if (comma.children().size() == 0) {
			builder.append("()");
		}
		else if (comma.children().size() == 1) {
			render(builder, comma.children().get(0));
		}
		else {
			builder.append("(");
			String sep = "";
			for (AbSyn child: comma.children()) {
				builder.append(sep);
				render(builder, child);
				sep = ", ";
			}
			builder.append(")");
		}
	}

	private static void renderApply(StringBuilder builder, AbSyn absyn) {
		AbApply apply = absyn.asKind(abTypes.apply);
		AbSyn operator = apply.operator();
		if (isTheMapSymbol(operator) && apply.argumentCount() == 2) {
			render(builder, apply.argument(0));
			builder.append(" \u2192 ");
			render(builder, apply.argument(1));
		}
		else {
			render(builder, operator);
			builder.append("(");
			String sep = "";
			for (AbSyn arg: apply.arguments()) {
				builder.append(sep);
				render(builder, arg);
				sep = ", ";
			}
			builder.append(")");
		}
	}

}
