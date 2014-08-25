package aldor.util.sexpr;


class Token {
	public Token(TokenType type, String string) {
		this.type = type;
		this.text = string;
	}

	public String text() {
		return text;
	}

	public TokenType type() {
		return type;
	}

	TokenType type;
	String text;
}
