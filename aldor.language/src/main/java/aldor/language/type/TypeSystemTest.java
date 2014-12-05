package aldor.language.type;

import org.junit.Test;

import aldor.language.type.AbstractSyntax.AbId;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;
import aldor.language.type.Types.TypeKind;
import aldor.language.type.Types.With;


public class TypeSystemTest {

	@Test
	public void test0() {
		TypeSystem sys = new TypeSystem("test");
		Scope scope = new Scope(sys, ScopeType.TOP);
		SymbolMeaning tuple = new SymbolMeaning(scope, "Tuple", sys.tfMap(scope, null, sys.tfLazy(scope, "SxType"), sys.tfWith())); // Tuple: SxType -> with == add;
		SymbolMeaning type = new SymbolMeaning(scope, "SxType", sys.tfLazy(scope, "SxType"));
		Type tupleType = sys.tfFrAbSyn(scope, sys.abApplyId(tuple, new AbId("Type")));
		SymbolMeaning map = new SymbolMeaning(scope, "->", sys.tfMap(scope, null, sys.tfCross(scope, null, tupleType, tupleType), sys.tfWith()));

		SymbolMeaning category = new SymbolMeaning(scope, "Tuple", sys.tfMap(scope, null, sys.tfLazy(scope, "SxType"), sys.tfWith())); // Tuple: SxType -> with == add;

		scope.bindSymbol(type);
		scope.bindSymbol(map);
		scope.bindSymbol(category);

		SymbolMeaning group = new SymbolMeaning(scope, "Group", scope.boundType("Category"), sys.abWithExpression());

		With groupType = group.value().meaning().asKind(TypeKind.with);
		Type groupSelf = groupType.self();
		Scope groupScope = groupType.bindingScope();
		Type binaryOperationOnGroup = sys.tfMap(scope, null, sys.tfCross(scope, null, groupSelf, groupSelf), groupSelf);
		SymbolMeaning groupStar = new SymbolMeaning(groupScope, "*", binaryOperationOnGroup);
		groupType.addExport(Type.ExportKind.CAT, groupStar);

		SymbolMeaning integer = new SymbolMeaning(scope, "Integer", sys.tfLazy(scope, "Group"));
	}

}

