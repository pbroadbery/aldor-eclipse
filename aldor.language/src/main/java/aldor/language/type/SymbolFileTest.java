package aldor.language.type;

import java.io.File;
import java.util.List;

import org.junit.Test;

import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.Type.ExportKind;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;
import aldor.language.type.TypeSystem.SymbolMeaning;
import aldor.language.type.Types.Map;
import aldor.language.type.Types.TypeKind;

public class SymbolFileTest {
	@Test
	public void testLang() {
		TypeSystem typeSystem = new TypeSystem("types");
		Scope global = new Scope(typeSystem, ScopeType.TOP);
		File file = new File("/home/pab/Work/aldorgit/opt/share/lib/aldor/sal_lang.asy");
		new SymbolFile(global, file);
	}

	@Test
	public void testPArray() {
		TypeSystem typeSystem = new TypeSystem("types");
		Scope global = new Scope(typeSystem, ScopeType.TOP);
		File file = new File("/home/pab/Work/aldorgit/opt/share/lib/aldor/sal_lang.asy");
		new SymbolFile(global, file);

		File file2 = new File("/home/pab/Work/aldorgit/opt/share/lib/aldor/sal_parray.asy");
		SymbolFile symFile2 = new SymbolFile(global, file2);

		System.out.println("Symbols: " + symFile2.boundSymbols());

		for (SymbolMeaning syme : symFile2.boundSymbols()) {
			if (syme.type().isOfKind(TypeKind.map)) {
				Map theMap = syme.type().asKind(TypeKind.map);
				Type params = theMap.params();
				Type rets = theMap.rets();
				List<Type> paramTypes = theMap.paramList();
				List<Type> retTypes = theMap.retList();
				for (Type t: paramTypes) {
					System.out.println("" + syme.name() + " ->Param->" + t);
				}
				for (SymbolMeaning catExport: syme.effectiveValue().exports(ExportKind.CAT)) {
					AbSyn abSyn = catExport.type().toAbSyn();
					System.out.println(syme.name() + "->"+catExport.name() + "->CAT: " + abSyn.render());
				}
				for (SymbolMeaning domExport: syme.effectiveValue().exports(ExportKind.DOM)) {
					AbSyn abSyn = domExport.type().toAbSyn();
					System.out.println(""+syme.name() + "->"+domExport.name() + "->DOM: " + abSyn.render());
				}
			} else {
				for (SymbolMeaning catExport: syme.effectiveValue().exports(ExportKind.CAT)) {
					AbSyn abSyn = catExport.type().toAbSyn();
					System.out.println(syme.name() + "+>"+catExport.name() + "->CAT: " + abSyn.render());
				}
				for (SymbolMeaning domExport: syme.effectiveValue().exports(ExportKind.DOM)) {
					AbSyn abSyn = domExport.type().toAbSyn();
					System.out.println(""+syme.name() + "+>"+domExport.name() + "->DOM: " + abSyn.render());
				}
			}
			System.out.println("Symbols: " + symFile2.boundSymbols());

		}
	}
}
