package aldor.language.type;

import java.util.List;

import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.TypeSystem.SymbolMeaning;
import aldor.language.type.Types.TypeKind;

interface Type {
	enum ExportKind { DOM, CAT; }

	boolean isOfKind(TypeKind<?> kind);
	<T> T asKind(TypeKind<T> kind);

	void addExport(ExportKind kind, SymbolMeaning sym);
	List<SymbolMeaning> exports(ExportKind kind);
	TypeKind<?> kind();

	AbSyn toAbSyn();
}