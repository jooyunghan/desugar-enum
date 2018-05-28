package com.company;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.EnumSet;

import static com.github.javaparser.ast.expr.BinaryExpr.Operator.EQUALS;
import static com.github.javaparser.ast.expr.BinaryExpr.Operator.NOT_EQUALS;
import static com.github.javaparser.ast.expr.UnaryExpr.Operator.LOGICAL_COMPLEMENT;

public class EnumDesugarVisitor extends ModifierVisitor<Void> {
    private final String target;

    public EnumDesugarVisitor(String target) {
        this.target = target;
    }

    @Override
    public Visitable visit(EnumDeclaration n, Void arg) {
        super.visit(n, arg);
        if (n.resolve().getQualifiedName().equals(target)) {
            return desugar(n);
        }
        return n;
    }

    @Override
    public Visitable visit(BinaryExpr n, Void arg) {
        super.visit(n, arg);
        if (n.getOperator() == EQUALS
                || n.getOperator() == NOT_EQUALS) {
            try {
                ResolvedType resolvedType = n.getLeft().calculateResolvedType();
                if (resolvedType.isReferenceType() && resolvedType.asReferenceType().getQualifiedName().equals(target)) {
                    Expression expr = new MethodCallExpr(n.getLeft().clone(), "equals", NodeList.nodeList(n.getRight().clone()));
                    if (n.getOperator() == NOT_EQUALS) {
                        expr = new UnaryExpr(expr, LOGICAL_COMPLEMENT);
                    }
                    return expr;
                }
            } catch (Exception e) {

            }
        }
        return n;
    }

    @Override
    public Visitable visit(ClassOrInterfaceType n, Void arg) {
        super.visit(n, arg);
        try {
            ResolvedReferenceType resolve = n.resolve();
            if (resolve.getQualifiedName().equals(target)) {
                n.setName("String");
            }
        } catch (Exception e) {

        }
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
                    try {
                        ResolvedType type = label.calculateResolvedType();
                        return type.isReferenceType() && type.asReferenceType().getQualifiedName().equals(target);
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            return false;
        })) {
            n.getImports().add(JavaParser.parseImport("import static " + target + ".*;"));
        }
        return n;
    }

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
        }

        decl.getMembers().forEach(m -> {
            if (m.isFieldDeclaration() && m.asFieldDeclaration().isStatic()
                    || m.isCallableDeclaration() && m.asCallableDeclaration().isStatic()
                    || m.isInitializerDeclaration() && m.asInitializerDeclaration().isStatic()) {
                classDecl.addMember(m.clone());
            } else {
                System.out.println("DROP:" + m.getClass().getSimpleName() + "\n" + m.toString());
            }
        });

        return classDecl;
    }
}
