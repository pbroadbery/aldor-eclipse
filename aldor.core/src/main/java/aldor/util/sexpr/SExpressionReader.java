package aldor.util.sexpr;

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import aldor.util.ReaderCharacterStream;
import aldor.util.SExpression;
import aldor.util.Stream;
import aldor.util.Strings;

public class SExpressionReader {

	ITokeniser tokeniser;

	public SExpression read() {
		Token tok = tokeniser.peek();

		switch (tok.type()) {
		case CParen:
			throw new RuntimeException("Parse error");
		case Integer:
			return SExpression.integer(Strings.instance().decode(Integer.class, tok.text()));
		case String:
			return SExpression.string(tok.text().substring(1, tok.text().length() -1));
		case Symbol:
			return SExpression.symbol(tok.text());
		case OParen:

			tokeniser.next();
			List<SExpression> lst = new LinkedList<SExpression>();
			while (tokeniser.peek().type() != TokenType.CParen) {
				SExpression next = read();
				lst.add(next);
			}
			tokeniser.next();
		default:
		}
		return null;
	}

	public boolean hasNext() {
		return tokeniser.hasNext();
	}

	class Tokeniser implements ITokeniser {
		private Stream<Character> stream;
		private Token token;

		Tokeniser(Reader stream) {
			this.stream = new ReaderCharacterStream(stream);
			this.token = null;
		}

		@Override
		public
		Token peek() {
			if (token != null)
				return token;

			if (stream.hasNext()) {
				return new Token(TokenType.EOF, "");
			}
			char c = stream.peek();
			if (c == '(') {
				return new Token(TokenType.OParen, "");
			}
			if (c == ')') {
				return new Token(TokenType.CParen, "");
			}
			if (c == '"') {
				String text = readString();
				return new Token(TokenType.String, text);
			}
			if (Character.isDigit(c)) {
				String text = readInteger();
				return new Token(TokenType.Integer, text);
			}
			if (Character.isAlphabetic(c)) {
				String text = readWord();
				return new Token(TokenType.Symbol, text);
			}
			return token;
		}

		private String readString() {
			StringBuilder sb = new StringBuilder();
			while (!stringTerminal(stream.peek())) {
				sb.append(stream.peek());
				stream.next();
			}
			return sb.toString();
		}

		private String readInteger() {
			StringBuilder sb = new StringBuilder();
			while (stream.peek() != null && Character.isDigit(stream.peek())) {
				sb.append(stream.peek());
				stream.next();
			}
			return sb.toString();
		}
		
		private String readWord() {
			StringBuilder sb = new StringBuilder();
			while (stream.peek() != null && Character.isWhitespace(stream.peek())) {
				sb.append(stream.peek());
				stream.next();
			}
			return sb.toString();
		}
	
		private boolean stringTerminal(Character c) {
			if (c == null || c == '"')
				return true;
			return false;
		}
		
		@Override
		public void next() {
			peek();
			token = null;
		}

		@Override
		public boolean hasNext() {
			return peek().type() != TokenType.EOF;
		}

	}

}
