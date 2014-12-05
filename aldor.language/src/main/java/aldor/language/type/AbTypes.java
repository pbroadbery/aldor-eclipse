package aldor.language.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aldor.language.type.AbstractSyntax.AbApply;
import aldor.language.type.AbstractSyntax.AbBoundGeneric;
import aldor.language.type.AbstractSyntax.AbDeclare;
import aldor.language.type.AbstractSyntax.AbId;
import aldor.language.type.AbstractSyntax.AbKind;
import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.AbstractSyntax.AbTypeApply;
import aldor.language.type.AbstractSyntax.AbTypeGeneric;
import aldor.language.type.Type.ExportKind;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;
import aldor.language.type.Types.TypeKind;
import aldor.util.SExpression;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

class AbTypes {
	static final AbTypes instance = new AbTypes();

	interface AbSynToTypeConverter {
		Type toType(Scope scope, AbSyn absyn);
	}

	private final AbSynToTypeConverter defaultConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(Scope scope, AbSyn absyn) {
			return new Types.General(scope, absyn);
		}
	};

	private final AbSynToTypeConverter declareConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(Scope scope, AbSyn absyn) {

			AbDeclare declaration = absyn.asKind(AbTypes.instance().declare);
			Type type = scope.typeSystem().tfFrAbSyn(scope, declaration.type());
			SymbolMeaning syme = scope.typeSystem().createSymbolMeaning(scope, declaration.name(), type, declaration.properties());
			scope.bindSymbol(syme);


			return new Types.Declare(scope, syme, absyn, type);
		}
	};

	private final AbSynToTypeConverter applyConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(Scope scope, AbSyn absyn) {
			AbApply abApply = absyn.asKind(apply);
			AbSyn operator = abApply.operator();
			Scope applyScope = new Scope(scope, ScopeType.FUNC);
			if (AbstractSyntax.isTheMapSymbol(operator)) {
				return scope.typeSystem().tfMap(applyScope, absyn, abApply.argument(0).toType(applyScope),
												abApply.argument(1).toType(applyScope));
			}
			return new Types.General(scope, absyn);
		}
	};

	private final AbSynToTypeConverter defineConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(Scope scope, AbSyn absyn) {
			Type defineType  = scope.typeSystem().tfFrAbSyn(scope, absyn.asKind(define).child(0));
			AbSyn value = absyn.asKind(define).child(1);
			return new Types.Define(scope, defineType, absyn, value);
		}
	};

	private final AbSynToTypeConverter commaConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(final Scope scope, AbSyn absyn) {
			List<Type> args = Lists.transform(absyn.asKind(comma).children(), new Function<AbSyn, Type>() {

				@Override
				public Type apply(AbSyn input) {
					return scope.typeSystem().tfFrAbSyn(scope, input);
				}});
			return new Types.Cross(scope, absyn, args);
		}
	};

	private final AbSynToTypeConverter addConverter = new AbSynToTypeConverter() {

		@Override
		public Type toType(Scope scope, AbSyn absyn) {
			System.out.println("Item: " + absyn);
			@SuppressWarnings("unused")
			AbSyn addLhs = absyn.asKind(add).child(0);
			List<AbSyn> addBody = absyn.asKind(add).children();
			System.out.println("Item: " + addBody);
			Type bodyType = new Types.Exports(scope, "<<anon>>");
			for (AbSyn item: addBody.subList(1, addBody.size())) {
				System.out.println("Item: " + item);
				addBodyAddExports(scope, bodyType, item);
			}
			return bodyType;
		}

		private void addBodyAddExports(Scope scope, Type bodyType, AbSyn item) {
			if (item == null) {
				return;
			}
			if (item.isOfKind(define)) {
				System.out.println("Decl: " + item);
				AbBoundGeneric theDefine = item.asKind(define);
				Type t = scope.typeSystem().tfFrAbSyn(bodyType.asKind(TypeKind.exports).scope(), theDefine.child(0));
				if (t.isOfKind(TypeKind.declare)) {
					SymbolMeaning syme = t.asKind(TypeKind.declare).declaredSymbol();
					bodyType.addExport(ExportKind.DOM, syme);
				}
			}
			else if (item.isOfKind(sequence)) {
				for (AbSyn ab: item.asKind(sequence).children()) {
					addBodyAddExports(scope, bodyType, ab);
				}
			}
		}
	};


	final public Map<String, AbKind<?>> kindForId = new HashMap<>();
	final AbKind<AbApply> apply = new AbTypeApply(this, AbApply.class, "Apply", applyConverter) {

	};
	final AbKind<AbDeclare> declare = new AbKind<AbDeclare>(this, AbDeclare.class, "Declare", declareConverter) {

		@Override
		public AbSyn parse(SExpression sx) {
			return AbDeclare.parse(sx);
		}
	};
	final AbKind<AbId> id = new AbKind<AbId>(this, AbId.class, "Id", defaultConverter) {

		@Override
		public AbSyn parse(SExpression sx) {
			return new AbId(sx.symbol());
		}
	};
	public AbKind<AbBoundGeneric> with = new AbTypeGeneric(this, "With", defaultConverter);
	public AbKind<AbBoundGeneric> comma = new AbTypeGeneric(this, "Comma", commaConverter);
	public AbKind<AbBoundGeneric> define = new AbTypeGeneric(this, "Define", defineConverter);
	public AbKind<AbBoundGeneric> add = new AbTypeGeneric(this, "Add", addConverter);
	public AbKind<AbBoundGeneric> sequence = new AbTypeGeneric(this, "Sequence", defineConverter);

	public static AbTypes instance() {
		return instance;
	}

	public AbKind<?> kindForId(String name) {
		AbKind<?> kind = kindForId.get(name);
		if (kind == null) {
			kind = new AbTypeGeneric(this, name, defaultConverter);
			kindForId.put(name, kind);
		}
		return kind;
	}

	public void register(AbKind<?> abKind) {
		this.kindForId.put(abKind.name(), abKind);
	}
}