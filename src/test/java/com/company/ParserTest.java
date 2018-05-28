package com.company;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.company.ExFunction.shh;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    private static final String FILE_PATH = "./ex/src/main/java";

    private Stream<CompilationUnit> getCUs() throws IOException {
        return Files.walk(Paths.get(FILE_PATH))
                .filter(p -> p.toString().endsWith(".java")).map(shh(JavaParser::parse));
    }

    @Test
    public void methodNamePrinterTest() throws IOException {
        MethodNamePrinter methodNamePrinter = new MethodNamePrinter();
        getCUs().forEach(cu -> methodNamePrinter.visit(cu, null));
    }

    @Test
    public void methodNameCollectorTest() throws IOException {
        MethodNameCollector methodNameCollector = new MethodNameCollector();
        List<String> collector = new ArrayList<>();
        getCUs().forEach(cu -> methodNameCollector.visit(cu, collector));
        assertThat(collector).containsExactlyInAnyOrder("handle", "main");
    }

    @Test
    public void InitModifierTest() throws IOException {
        InitModifier visitor = new InitModifier();
        String classDecl = "class X {\n" +
                "\tAction action = null;\n" +
                "}";
        String expectedDecl = classDecl.replace("Action", "String");

        TypeDeclaration<?> typeDeclaration = JavaParser.parseTypeDeclaration(classDecl);

        Visitable changed = visitor.visit((ClassOrInterfaceDeclaration) typeDeclaration, null);
        assertThat(changed).isEqualTo(JavaParser.parseTypeDeclaration(expectedDecl));
    }


    @Test
    public void desugTest() throws IOException {
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("ex/src/main/java"));
        reflectionTypeSolver.setParent(reflectionTypeSolver);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(reflectionTypeSolver);
        typeSolver.add(javaParserTypeSolver);

        ParserConfiguration config = JavaParser.getStaticConfiguration();
        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        config.setStoreTokens(true);

        EnumDesugarVisitor desugar = new EnumDesugarVisitor("org.example.Action");
        getCUs().map(cu -> (Node) desugar.visit(cu, null)).forEach(node -> {

            try {
                System.out.print(node);
            } catch (Exception e) {
                System.out.print(node);
            }
        });
    }

    @Test
    public void resoverTest() {
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("ex/src/main/java"));
        reflectionTypeSolver.setParent(reflectionTypeSolver);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(reflectionTypeSolver);
        typeSolver.add(javaParserTypeSolver);

        ParserConfiguration config = JavaParser.getStaticConfiguration();
        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));

        String code = "import org.example.Action;" +
                "import static org.example.Action.*;" +
                "class Test {" +
                "   public void foo(Action a) {" +
                "       switch (a) {" +
                "           case PLAY: System.out.println(a); break;" +
                "           default: break;" +
                "       }" +
                "   }" +
                "}";

        CompilationUnit cu = JavaParser.parse(code);
//        cu.stream().forEach(x -> System.out.println(x.getClass().getSimpleName() + ">> " + x));
        SwitchStmt stmt = Navigator.findSwitch(cu);

        ResolvedType type = stmt.getSelector().calculateResolvedType();
        assertTrue(type.isReferenceType());
        System.out.println(type);
//        ResolvedReferenceTypeDeclaration decl = typeSolver.solveType("org.example.Action");
//
//        ResolvedEnumDeclaration enumDeclaration = decl.asEnum();
//        List<String> values = enumDeclaration.getEnumConstants().stream().map(c -> c.getName()).collect(Collectors.toList());
//        assertThat(values).containsExactly("PLAY", "STOP", "PAUSE");
    }

    @Test
    public void lexicalTest() {
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(reflectionTypeSolver);

        ParserConfiguration config = JavaParser.getStaticConfiguration();
        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));
//        config.setLexicalPreservationEnabled(true);
        String code = "import java.util.List;\n\n" +
                "class A {\n" +
                "}\n" +
                "class B {\n" +
                "   List<A> as, as2[];\n" +
                "}";
        CompilationUnit cu = JavaParser.parse(code);
        ModifierVisitor<Void> visitor = new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(ClassOrInterfaceType n, Void arg) {
                super.visit(n, arg);
                if (n.getNameAsString().equals("A")) {
                    return JavaParser.parseType("String");
                }
                return n;
            }
        };
        visitor.visit(cu, null);
        assertThat(cu.toString()).isEqualToIgnoringWhitespace(code.replace("<A>", "<String>"));
    }

    @Test
    public void changeEqualToMethodCall() {
        String code = "true && action == MainAction.PLAY";
        Node n = JavaParser.parseExpression(code);
        LexicalPreservingPrinter.setup(n);
        n.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                if (n.getOperator() == BinaryExpr.Operator.EQUALS) {
                    return new MethodCallExpr(n.getLeft().clone(), "equals", NodeList.nodeList(n.getRight().clone()));
                }
                return n;
            }
        }, null);
        assertThat(n.toString()).isEqualToIgnoringWhitespace("true && action.equals(MainAction.PLAY)");
    }

    @Test
    public void ifElseChain() {
        String code = "if (a == 1) {} else {}";
        Node n = JavaParser.parseStatement(code);
        LexicalPreservingPrinter.setup(n);
        n.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                if (n.getOperator() == BinaryExpr.Operator.EQUALS) {
                    return new MethodCallExpr(n.getLeft().clone(), "equals", NodeList.nodeList(n.getRight().clone()));
                }
                return n;
            }
        }, null);
//        assertThat(n.toString()).isEqualToIgnoringWhitespace("true && action.equals(MainAction.PLAY)");
    }

    @Test
    public void caseLabel() {
        String code = "switch(a) { case 1: a = 1; a = 1; }";
        Node n = JavaParser.parseStatement(code);
        LexicalPreservingPrinter.setup(n);
        n.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                if (n.getOperator() == BinaryExpr.Operator.EQUALS) {
                    return new MethodCallExpr(n.getLeft().clone(), "equals", NodeList.nodeList(n.getRight().clone()));
                }
                return n;
            }
        }, null);
//        assertThat(n.toString()).isEqualToIgnoringWhitespace("true && action.equals(MainAction.PLAY)");
    }


}
