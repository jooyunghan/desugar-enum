package com.company;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.modules.ModuleUsesStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import java.util.EnumSet;

class EnumDesugarVisitor extends ModifierVisitor<Void> {
//    @Override
//    public Visitable visit(EnumDeclaration n, Void arg) {
//        super.visit(n, arg);
//        if (n instanceof  EnumDeclaration) {
//            if (n.resolve().getQualifiedName().equals("org.example.Action")) {
//                return desugar(n);
//            }
//        }
//        return n;
//    }

    @Override
    public Visitable visit(ClassOrInterfaceType n, Void arg) {
        super.visit(n, arg);
        ResolvedReferenceType resolve = n.resolve();
        if (resolve.getQualifiedName().equals("org.example.Action")) {
            n.setName("String");
        }
//        if (n.getIdentifier().equals("Action")) {
//            if (n.getParentNode().isPresent()) {
//                if (n.getParentNode().get() instanceof ClassOrInterfaceType) {
//                    ClassOrInterfaceType parent = (ClassOrInterfaceType) n.getParentNode().get();
//                    ResolvedReferenceType resolve = parent.resolve();
//                    if (resolve.getQualifiedName().equals("org.example.Action")) {
//
//                    }
//                }
//            }
//            System.out.println("ACTION>>>" + n.getParentNode().map(Object::getClass) + ">>" + n.getParentNode().flatMap(Node::getParentNode).map(Object::getClass) + ">>" + n.getParentNode().flatMap(Node::getParentNode).flatMap(Node::getParentNode).map(Object::getClass));
//        }
        return n;
    }

    @Override
    public Visitable visit(CompilationUnit n, Void arg) {
        super.visit(n, arg);
        if (n.stream().anyMatch((Node node) -> {
            if (node instanceof SwitchEntryStmt) {
                SwitchEntryStmt stmt = (SwitchEntryStmt) node;
                if (stmt.getLabel().isPresent()) {
                    Expression label = stmt.getLabel().get();
                    ResolvedType type = label.calculateResolvedType();
                    return type.isReferenceType() && type.asReferenceType().getQualifiedName().equals("org.example.Action");
                }
            }
            return false;
        })) {
            n.getImports().add(JavaParser.parseImport("import static org.example.Action.*;"));
        }
        return n;
    }
//
//    @Override
//    public Visitable visit(SwitchEntryStmt n, Void arg) {
//        SwitchEntryStmt visit = (SwitchEntryStmt) super.visit(n, arg);
//        visit.getLabel().filter(label -> {
//            ResolvedType type = label.calculateResolvedType();
//            return (type.isReferenceType() && type.asReferenceType().getQualifiedName().equals("org.example.Action"));
//        }).ifPresent(label -> System.out.println("case/default >>" + label));
//        return visit;
//    }

    static ClassOrInterfaceDeclaration desugar(EnumDeclaration decl) {
        ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration();

        EnumSet<Modifier> modifiers = decl.getModifiers();
        modifiers.add(Modifier.FINAL);
        classDecl.setModifiers(modifiers);

        classDecl.setName(decl.getName());

        for (EnumConstantDeclaration entry : decl.getEntries()) {
            String name = entry.getNameAsString();
            FieldDeclaration field = classDecl.addField("String", name, Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC);
            field.getVariable(0).setInitializer(new StringLiteralExpr(name.toLowerCase()));
            field.setComment(entry.getComment().orElse(null));
        }

        decl.getMembers().forEach(m -> {
            if (m.isFieldDeclaration() && m.asFieldDeclaration().isStatic()
                    || m.isCallableDeclaration() && m.asCallableDeclaration().isStatic()
                    || m.isInitializerDeclaration() && m.asInitializerDeclaration().isStatic()) {
                classDecl.addMember(m);
            } else {
                System.out.println("DROP:" + m.getClass().getSimpleName() + "\n" + m.toString());
            }
        });

        return classDecl;
    }
}
