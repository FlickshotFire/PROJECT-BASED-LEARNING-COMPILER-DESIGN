// Simplified Compiler with AST, Moore Machine Visualization, Code Optimization, and Intermediate Code
import java.util.*;

// --- Token Types ---
enum TokenType {
    KEYWORD, IDENTIFIER, LITERAL, OPERATOR, SYMBOL, STRING, CHAR
}

class Token {
    TokenType type;
    String value;
    int line, column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("[%s: \"%s\" at %d:%d]", type, value, line, column);
    }
}

// --- AST Nodes ---
abstract class ASTNode {}
class BinaryOpNode extends ASTNode {
    String op;
    ASTNode left, right;
    BinaryOpNode(String op, ASTNode left, ASTNode right) {
        this.op = op; this.left = left; this.right = right;
    }
}
class LiteralNode extends ASTNode {
    String value;
    LiteralNode(String value) { this.value = value; }
}
class IdentifierNode extends ASTNode {
    String name;
    IdentifierNode(String name) { this.name = name; }
}
class AssignmentNode extends ASTNode {
    String variable;
    ASTNode expression;
    AssignmentNode(String variable, ASTNode expression) {
        this.variable = variable; this.expression = expression;
    }
}

// --- Lexer ---
class Lexer {
    String code;
    List<Token> tokens = new ArrayList<>();
    int i = 0, line = 1, column = 1;
    Set<String> keywords = Set.of("if", "else", "while", "for", "return");
    Set<String> symbols = Set.of("(", ")", "{", "}", ";", ",");
    Set<String> operators = Set.of("+", "-", "*", "/", "=", "==", "!=", "<", ">", "<=", ">=", "&&", "||");

    public Lexer(String code) {
        this.code = code;
        tokenize();
    }

    void tokenize() {
        while (i < code.length()) {
            char c = code.charAt(i);
            if (Character.isWhitespace(c)) {
                if (c == '\n') { line++; column = 1; } else { column++; }
                i++; continue;
            }
            if (Character.isLetter(c) || c == '_') tokenizeIdentifier();
            else if (Character.isDigit(c)) tokenizeNumber();
            else if (c == '"') tokenizeString();
            else if (c == '\'') tokenizeChar();
            else if (isOperatorStart(c)) tokenizeOperator();
            else if (symbols.contains(String.valueOf(c))) {
                tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c), line, column));
                i++; column++;
            } else { i++; column++; }
        }
    }

    void tokenizeIdentifier() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (i < code.length() && (Character.isLetterOrDigit(code.charAt(i)) || code.charAt(i) == '_')) {
            sb.append(code.charAt(i++)); column++;
        }
        String word = sb.toString();
        TokenType type = keywords.contains(word) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
        tokens.add(new Token(type, word, line, startCol));
    }

    void tokenizeNumber() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (i < code.length() && Character.isDigit(code.charAt(i))) {
            sb.append(code.charAt(i++)); column++;
        }
        tokens.add(new Token(TokenType.LITERAL, sb.toString(), line, startCol));
    }

    void tokenizeString() {
        int startCol = column; i++; column++;
        StringBuilder sb = new StringBuilder();
        while (i < code.length() && code.charAt(i) != '"') {
            sb.append(code.charAt(i++)); column++;
        }
        i++; column++;
        tokens.add(new Token(TokenType.STRING, sb.toString(), line, startCol));
    }

    void tokenizeChar() {
        int startCol = column; i++; column++;
        char ch = code.charAt(i++); column++;
        i++; column++;
        tokens.add(new Token(TokenType.CHAR, String.valueOf(ch), line, startCol));
    }

    void tokenizeOperator() {
        int startCol = column;
        String op2 = i + 1 < code.length() ? code.substring(i, i + 2) : "";

if (operators.contains(op2)) {
            tokens.add(new Token(TokenType.OPERATOR, op2, line, startCol)); i += 2; column += 2;
        } else {
            String op1 = String.valueOf(code.charAt(i));
            tokens.add(new Token(TokenType.OPERATOR, op1, line, startCol)); i++; column++;
        }
    }

    boolean isOperatorStart(char c) {
        return "+-*/=!><&|".indexOf(c) != -1;
    }
}

// --- Parser ---
class Parser {
    List<Token> tokens;
    int index = 0;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

   public ASTNode parseAssignment() {
    if (index >= tokens.size() || tokens.get(index).type != TokenType.IDENTIFIER) {
        throw new RuntimeException("Expected identifier at beginning of assignment");
    }

    String varName = tokens.get(index++).value;

    if (index >= tokens.size() || !tokens.get(index).value.equals("=")) {
        throw new RuntimeException("Expected '=' after identifier in assignment");
    }
    index++; // skip '='

    ASTNode expr = parseExpression();

    // Optional: expect semicolon at the end
    if (index < tokens.size() && tokens.get(index).value.equals(";")) {
        index++;
    }

    return new AssignmentNode(varName, expr);
}


    public ASTNode parseExpression() {
        ASTNode left = parsePrimary();
        while (index < tokens.size() && tokens.get(index).type == TokenType.OPERATOR) {
            String op = tokens.get(index++).value;
            ASTNode right = parsePrimary();
            left = new BinaryOpNode(op, left, right);
        }
        return left;
    }

    public ASTNode parsePrimary() {
        Token token = tokens.get(index++);
        return switch (token.type) {
            case LITERAL -> new LiteralNode(token.value);
            case IDENTIFIER -> new IdentifierNode(token.value);
            default -> null;
        };
    }
}

// --- Intermediate Code Generator ---
class IntermediateGenerator {
    int tempCount = 0;
    List<String> code = new ArrayList<>();

    public String generate(ASTNode node) {
        if (node instanceof LiteralNode ln) return ln.value;
        if (node instanceof IdentifierNode in) return in.name;
        if (node instanceof BinaryOpNode bn) {
            String left = generate(bn.left);
            String right = generate(bn.right);
            String temp = "t" + (tempCount++);
            code.add(temp + " = " + left + " " + bn.op + " " + right);
            return temp;
        }
        if (node instanceof AssignmentNode an) {
            String val = generate(an.expression);
            code.add(an.variable + " = " + val);
            return an.variable;
        }
        return null;
    }

    public void printCode() {
        for (String line : code) System.out.println(line);
    }
}

// --- Main ---
public class CompilerASTMoore {
    public static void main(String[] args) {
        String code = "y = x * 2 + 5;";
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer.tokens);
        ASTNode ast = parser.parseAssignment();
        System.out.println("--- Intermediate Code ---");
        IntermediateGenerator gen = new IntermediateGenerator();
        gen.generate(ast);
        gen.printCode();
    }
}
