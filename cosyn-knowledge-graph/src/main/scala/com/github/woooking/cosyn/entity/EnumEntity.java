package com.github.woooking.cosyn.entity;

import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.stream.Collectors;

@NodeEntity
public class EnumEntity extends TypeEntity {
    transient private ResolvedReferenceTypeDeclaration resolved;

    private String constants;

    public static EnumEntity fromDeclaration(EnumDeclaration decl) {
        return new EnumEntity(decl.resolve(), decl.getJavadocComment().orElse(null));
    }

    protected EnumEntity() {
    }

    protected EnumEntity(ResolvedEnumDeclaration resolved, JavadocComment javadocComment) {
        super(resolved, false, false, javadocComment);
        this.constants = resolved.getEnumConstants().stream().map(ResolvedEnumConstantDeclaration::getName).collect(Collectors.joining(","));
    }

    public String getConstants() {
        return constants;
    }
}
