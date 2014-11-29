package aldor.util.sexpr;

import java.io.Reader;

import aldor.util.ReaderCharacterStream;
import aldor.util.SExpression;
import aldor.util.Stream;
import aldor.util.Strings;

public class SExpressionReader {
	ITokeniser tokeniser;

	public SExpressionReader(Reader reader) {
		tokeniser = new WhitespaceFilter(new Tokeniser(reader));
	}

	public SExpression read() {
		Token tok = tokeniser.peek();
		tokeniser.next();
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
			return readListOrNil();
		case DOT:
			throw new RuntimeException("Parse error: " + tok);
		default:
		}
		throw new RuntimeException("Unexpected token: "+ tok);
	}

	private SExpression readListOrNil() {
		Token next = tokeniser.peek();
		if (next.type() == TokenType.CParen) {
			tokeniser.next();
			return SExpression.nil();
		}
		else {
			return readList();
		}
	}

	private SExpression readList() {
		SExpression head = SExpression.cons(SExpression.nil(), SExpression.nil());
		SExpression ptr = head;
		boolean done = false;
		while (!done) {
			SExpression next = read();
			ptr.setCar(next);
			Token nextToken = tokeniser.peek();
			if (nextToken.type() == TokenType.DOT) {
				tokeniser.next();
				SExpression endSx = read();
				ptr.setCdr(endSx);
				nextToken = tokeniser.peek();
				if (nextToken.type() != TokenType.CParen) {
					throw new RuntimeException("Parse error " + nextToken);
				}
				tokeniser.next();
				done = true;
			}
			else if (nextToken.type() == TokenType.CParen) {
				tokeniser.next();
				done = true;
			}
			else {
				SExpression newConsCell = SExpression.cons(head, SExpression.nil());
				ptr.setCdr(newConsCell);
				ptr = newConsCell;
			}
		}
		return head;
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

			if (!stream.hasNext()) {
				token = new Token(TokenType.EOF, "");
			}
			char c = stream.peek();
			if (c == '(') {
				token = new Token(TokenType.OParen, "");
				stream.next();
			}
			else if (c == ')') {
				token = new Token(TokenType.CParen, "");
				stream.next();
			}
			else if (c == '"') {
				String text = readString();
				token = new Token(TokenType.String, text);
			}
			else if (c == '.') {
				token = new Token(TokenType.DOT, ".");
				stream.next();
			}
			else if (Character.isDigit(c)) {
				String text = readInteger();
				token = new Token(TokenType.Integer, text);
			}
			else if (isSymbolStartCharacter(c)) {
				String text = readWord();
				token = new Token(TokenType.Symbol, text);
			}
			else if (c == '|') {
				String text = readEscapedWord();
				token = new Token(TokenType.Symbol, text);
			}
			else if (Character.isWhitespace(c)) {
				token = new Token(TokenType.WS, ""+c);
				stream.next();
			}
			else {
				throw new RuntimeException("Unknown character " + c);
			}
			return token;
		}

		private String readString() {
			assert stream.peek() == '"';
			StringBuilder sb = new StringBuilder();
			sb.append(stream.peek());
			stream.next();
			while (!stringTerminal(stream.peek())) {
				sb.append(stream.peek());
				stream.next();
			}
			sb.append(stream.peek());
			stream.next();
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
			while (stream.hasNext()
					&& isSymbolCharacter(stream.peek())) {
				Character thisChar = stream.peek();
				if (thisChar == '\\') {
					stream.next();
					thisChar = stream.peek();
				}
				sb.append(thisChar);
				stream.next();
			}
			return sb.toString();
		}

		private boolean isSymbolStartCharacter(char c) {
			if (Character.isDigit(c))
				return false;
			if (isSymbolCharacter(c)) {
				return true;
			}
			return false;
		}

		private boolean isSymbolCharacter(char c) {
			if (Character.isWhitespace(c))
				return false;
			if (Character.isDigit(c)
					|| Character.isAlphabetic(c)
					|| c == '\\'
					|| c == '='
					|| c == '<'
					|| c == '>'
					|| c == '-'
					|| c == '+'
					|| c == '*'
					|| c == '/'
					|| c == '^'
					|| c == '%'
					|| c == '~') {
				return true;
			}
			return false;
		}


		private String readEscapedWord() {
			StringBuilder sb = new StringBuilder();
			stream.next();
			while (stream.hasNext()
					&& stream.peek() != '|') {
				sb.append(stream.peek());
				stream.next();
			}
			if (stream.peek() == '|') {
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
