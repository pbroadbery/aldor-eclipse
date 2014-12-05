package aldor.language.type;

import java.util.ArrayList;
import java.util.List;

class SymeList {
	final List<SymbolMeaning> symes;

	SymeList() {
		this.symes = new ArrayList<>();
	}

	public void add(SymbolMeaning syme) {
		symes.add(syme);
	}

	public List<SymbolMeaning> asList() {
		return symes;
	}
}