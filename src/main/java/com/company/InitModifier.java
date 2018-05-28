package com.company;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;

class InitModifier extends ModifierVisitor<Void> {
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
