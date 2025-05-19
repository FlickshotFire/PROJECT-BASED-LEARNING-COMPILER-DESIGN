import java.util.*;

// Token types
enum TokenType {
    KEYWORD,
    IDENTIFIER,
    LITERAL,
    OPERATOR,
    SYMBOL
}

// Token class
class Token {
    TokenType type;
    String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}

// Lexer class
class Lexer {
    String sourceCode;
    List<Token> tokens;

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        this.tokens = new ArrayList<>();
        tokenize();
    }

    private void tokenize() {
        Set<String> keywords = Set.of("if", "else", "while", "for");
        Set<String> operators = Set.of("+", "-", "*", "/", "=", ">", "<");
        Set<String> symbols = Set.of("(", ")", "{", "}", ";");

        int i = 0;
        while (i < sourceCode.length()) {
            char c = sourceCode.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Identifiers or Keywords
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < sourceCode.length() && (Character.isLetterOrDigit(sourceCode.charAt(i)) || sourceCode.charAt(i) == '_')) {
                    sb.append(sourceCode.charAt(i++));
                }
                String word = sb.toString();
                if (keywords.contains(word)) {
                    tokens.add(new Token(TokenType.KEYWORD, word));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
            }

            // Numeric Literals
            else if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < sourceCode.length() && Character.isDigit(sourceCode.charAt(i))) {
                    sb.append(sourceCode.charAt(i++));
                }
                tokens.add(new Token(TokenType.LITERAL, sb.toString()));
            }

            // Operators
            else if (operators.contains(String.valueOf(c))) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                i++;
            }

            // Symbols
            else if (symbols.contains(String.valueOf(c))) {
                tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c)));
                i++;
            }

            // Unknown characters
            else {
                System.err.println("Unrecognized character: " + c);
                i++;
            }
        }
    }
}

// Parser class
class Parser {
    List<Token> tokens;
    int currentIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
    }

    public void parse() {
        while (currentIndex < tokens.size()) {
            Token token = tokens.get(currentIndex);
            switch (token.type) {
                case KEYWORD -> parseKeyword(token);
                case IDENTIFIER -> parseIdentifier(token);
                case LITERAL -> parseLiteral(token);
                case OPERATOR -> parseOperator(token);
                case SYMBOL -> parseSymbol(token);
            }
            currentIndex++;
        }
    }

    private void parseKeyword(Token token) {
        System.out.println("Keyword: " + token.value);
    }

    private void parseIdentifier(Token token) {
        System.out.println("Identifier: " + token.value);
    }

    private void parseLiteral(Token token) {
        System.out.println("Literal: " + token.value);
    }

    private void parseOperator(Token token) {
        System.out.println("Operator: " + token.value);
    }

    private void parseSymbol(Token token) {
        System.out.println("Symbol: " + token.value);
    }
}

// Main compiler class
public class compiler2{
    public static void main(String[] args) {
        String sourceCode = """
            if (x > 5) {
                y = x * 2;
            }
        """;

        Lexer lexer = new Lexer(sourceCode);
        Parser parser = new Parser(lexer.tokens);
        parser.parse();
    }
}
