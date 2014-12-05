package aldor.language.type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import aldor.language.type.AbstractSyntax.AbDeclare;
import aldor.language.type.AbstractSyntax.AbSyn;
import aldor.language.type.TypeSystem.Scope;
import aldor.language.type.TypeSystem.ScopeType;
import aldor.util.SExpression;

public class SymbolFile {
	private final File pathToFile;
	private SymeList boundSymbols;
	private boolean hasError;
	private final Scope globalScope;

	SymbolFile(Scope globalScope, File file) {
		this.globalScope = globalScope;
		this.pathToFile = file;
	}

	public boolean hasError() {
		return hasError;
	}

	public List<SymbolMeaning> boundSymbols() {
		if (this.boundSymbols == null) {
			try {
				read();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				hasError = true;
			}
		}
		return boundSymbols.asList();
	}

	private void read() throws FileNotFoundException {
		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		SExpression sx = SExpression.read(reader, true);
System.out.println("" + sx);
		Scope scope = new Scope(this.globalScope, ScopeType.FILE);
		bindSymbolsToScope(scope, sx);
		this.boundSymbols = scope.boundSymbols();
	}

	private void bindSymbolsToScope(Scope scope, SExpression sx) {
		for (SExpression decl: sx.asList()) {
			if (decl.car().symbol().equals("Declare"))
				bindDeclaration(scope, decl);
			else {
				// TODO: Do something a little more useful than this:
				System.out.println("Skipping " + decl);
			}
		}
	}

	private void bindDeclaration(Scope scope, SExpression decl) {
		if (!decl.car().symbol().equals("Declare")) {
			throw new RuntimeException("Expected a declaration");
		}
		AbSyn absyn = AbstractSyntax.parse(decl);
		AbDeclare declaration = absyn.asKind(AbTypes.instance().declare);
		Type type = scope.typeSystem().tfFrAbSyn(scope, declaration.type());
		SymbolMeaning syme = scope.typeSystem().createSymbolMeaning(scope, declaration.name(), type, declaration.properties());
		scope.bindSymbol(syme);
	}

}
