package aldor.util.sexpr;

import org.eclipse.core.runtime.Assert;

public class WhitespaceFilter implements ITokeniser {
	private ITokeniser underlying;

	WhitespaceFilter(ITokeniser underlying) {
		this.underlying = underlying;
	}

	@Override
	public Token peek() {
		Token tok = underlying.peek();
		while (tok.isWhitespace()) {
			underlying.next();
			tok = underlying.peek();
		}
		Assert.isTrue(!tok.isWhitespace());
		return tok;
	}

	@Override
	public void next() {
		underlying.next();
	}

	@Override
	public boolean hasNext() {
		return underlying.hasNext();
	}

}