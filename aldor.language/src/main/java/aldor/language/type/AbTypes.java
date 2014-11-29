package aldor.language.type;

import java.util.HashMap;
import java.util.Map;

import aldor.language.type.AbstractSyntax.AbApply;
import aldor.language.type.AbstractSyntax.AbBoundGeneric;
import aldor.language.type.AbstractSyntax.AbDeclare;
import aldor.language.type.AbstractSyntax.AbId;
import aldor.language.type.AbstractSyntax.AbKind;
import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.AbstractSyntax.AbTypeApply;
import aldor.language.type.AbstractSyntax.AbTypeGeneric;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;
import aldor.language.type.TypeSystem.SymbolMeaning;
import aldor.util.SExpression;

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
	public AbKind<AbBoundGeneric> comma = new AbTypeGeneric(this, "Comma", defaultConverter);

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