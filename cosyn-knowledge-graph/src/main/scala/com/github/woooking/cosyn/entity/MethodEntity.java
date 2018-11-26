package com.github.woooking.cosyn.entity;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class MethodEntity {
    transient private ResolvedMethodLikeDeclaration resolved;
    @Id
    private String qualifiedSignature;
    private String signature;
    private boolean isStatic;
    private boolean isConstructor;
    private AccessSpecifier accessSpecifier;

    @Relationship(type = "HAS_METHOD", direction = Relationship.INCOMING)
    private TypeEntity declareType;

    @Relationship(type = "EXTENDS")
    private Set<MethodEntity> extendedMethods = new HashSet<>();

    @Relationship(type = "PRODUCES")
    private TypeEntity produce;

    public MethodEntity() {
    }

    public MethodEntity(ResolvedConstructorDeclaration resolved, TypeEntity declareType) {
        this.resolved = resolved;
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.isStatic = true;
        this.isConstructor = true;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        declareType.addHasMethod(this);
    }

    public MethodEntity(ResolvedMethodDeclaration resolved, TypeEntity declareType) {
        this.resolved = resolved;
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.isStatic = resolved.isStatic();
        this.isConstructor = false;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
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

    public String getSignature() {
        return signature;
    }

    public String getQualifiedSignature() {
        return qualifiedSignature;
    }

    public TypeEntity getDeclareType() {
        return declareType;
    }

    public AccessSpecifier getAccessSpecifier() {
        return accessSpecifier;
    }

    public boolean isConstructor() {
        return isConstructor;
    }
}
