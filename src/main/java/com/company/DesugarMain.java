package com.company;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.company.ExFunction.shh;

public class DesugarMain {
    public static void main(String[] args) throws IOException {

        String[] paths = new String[]{"/Users/jooyung.han/ai/service/src", "/Users/jooyung.han/ai/service/engine"};
        String[] jars = new String[]{};

        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
        reflectionTypeSolver.setParent(reflectionTypeSolver);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        for (String p : paths) {
            typeSolver.add(new JavaParserTypeSolver(new File(p)));
        }

        for (String p : jars) {
            typeSolver.add(new JarTypeSolver(p));
        }
        typeSolver.add(reflectionTypeSolver);
//
        ParserConfiguration config = JavaParser.getStaticConfiguration();
        config.setSymbolResolver(new JavaSymbolSolver(typeSolver));


//         Arrays.stream(paths).map(Paths::get).flatMap(shh(Files::walk)).filter(p -> p.toString().endsWith(".java")).forEach(System.out::println);
        EnumDesugarVisitor desugar = new EnumDesugarVisitor("com.lge.speech.nlp.type.MainActionType");

        CompilationUnit cu = JavaParser.parse(new File("/Users/jooyung.han/ai/service/engine/com/lge/speech/nlp/engineHU/dm/geely/phone/output/IOT_COMMON.java"));
        LexicalPreservingPrinter.setup(cu);
        desugar.visit(cu, null);

//        getCUs(paths)
//                .map(cu -> desugar.visit(cu, null))
//                .forEach(node -> {
//                    System.out.print(node);
//                });
    }

    private static Stream<CompilationUnit> getCUs(String[] paths) {
        return Arrays.stream(paths).map(Paths::get).flatMap(shh(Files::walk))
                .filter(p -> p.toString().endsWith(".java"))
                .peek(System.out::println)
                .map(shh(JavaParser::parse))
                .map(LexicalPreservingPrinter::setup);
    }
}
