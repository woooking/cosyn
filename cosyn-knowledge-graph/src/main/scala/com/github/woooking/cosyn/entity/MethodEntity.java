package com.github.woooking.cosyn.entity;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.google.common.collect.ImmutableSet;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class MethodEntity {
    @Id
    private String qualifiedSignature;
    private String signature;
    private String simpleName;
    private boolean isStatic;
    private boolean isConstructor;
    private boolean isDeprecated;
    private AccessSpecifier accessSpecifier;
    private String javadoc;
    private String paramNames;

    @Relationship(type = "HAS_METHOD", direction = Relationship.INCOMING)
    private TypeEntity declareType;

    @Relationship(type = "EXTENDS")
    private Set<MethodEntity> extendedMethods = new HashSet<>();

    @Relationship(type = "PRODUCES")
    private TypeEntity produce;

    @Relationship(type = "PRODUCES_MULTIPLE")
    private TypeEntity produceMultiple;

    public MethodEntity() {
    }

    public MethodEntity(ResolvedConstructorDeclaration resolved, boolean isDeprecated, TypeEntity declareType, JavadocComment javadocComment) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = true;
        this.isConstructor = true;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadocComment == null ? "" : javadocComment.getContent();
        declareType.addHasMethod(this);
    }

    public MethodEntity(ResolvedMethodDeclaration resolved, boolean isDeprecated, TypeEntity declareType, JavadocComment javadocComment) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = resolved.isStatic();
        this.isConstructor = false;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadocComment == null ? "" : javadocComment.getContent();
        declareType.addHasMethod(this);
    }

    public void addExtendedMethods(Set<MethodEntity> extendedMethods) {
        this.extendedMethods.addAll(extendedMethods);
    }

    public void setProduce(TypeEntity produce) {
        assert this.produce == null;
        this.produce = produce;
        produce.addProducer(this);
    }

    public void setProduceMultiple(TypeEntity produceMultiple) {
        assert this.produceMultiple == null;
        this.produceMultiple = produceMultiple;
        produceMultiple.addMultipleProducer(this);
    }

    public String getSignature() {
        return signature;
    }

    public String getQualifiedSignature() {
        return qualifiedSignature;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public TypeEntity getDeclareType() {
        return declareType;
    }

    public TypeEntity getProduce() {
        return produce;
    }

    public AccessSpecifier getAccessSpecifier() {
        return accessSpecifier;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public Set<MethodEntity> getExtendedMethods() {
        return ImmutableSet.copyOf(extendedMethods);
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }
}
