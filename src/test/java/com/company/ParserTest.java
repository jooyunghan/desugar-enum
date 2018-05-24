package com.company;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.company.ExFunction.shh;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(collector).containsExactly("handle", "main");
    }

    @Test
    public void InitModifierTest() throws IOException {
        InitModifier visitor = new InitModifier();
        String classDecl = "class X {\n" +
                "\tAction action = null;\n" +
                "}";
        TypeDeclaration<?> typeDeclaration = JavaParser.parseTypeDeclaration(classDecl);
        Visitable changed = visitor.visit((ClassOrInterfaceDeclaration) typeDeclaration, null);
        assertThat(changed.toString()).isEqualTo(classDecl.replace("Action", "String"));
    }

    static class InitModifier extends ModifierVisitor<Void> {
        @Override
        public FieldDeclaration visit(FieldDeclaration n, Void arg) {
            super.visit(n, arg);
            n.getVariables().forEach(vd -> {
                if (vd.getTypeAsString().equals("Action")) {
                    vd.setType("String");
                }
            });
            return n;
        }
    }
}
