package aldor.util.sexpr;


final class Token {
	private final TokenType type;
	private final String text;

	public Token(TokenType type, String string) {
		this.type = type;
		this.text = string;
	}

	final public String text() {
		return text;
	}

	final public TokenType type() {
		return type;
	}

	@Override
	final public String toString() {
		return "{T:" + type +" " + text + "}";
	}

	public boolean isWhitespace() {
		return type.isWhitespace();
	}

}
