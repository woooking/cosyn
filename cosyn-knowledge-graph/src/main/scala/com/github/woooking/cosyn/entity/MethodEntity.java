package com.github.woooking.cosyn.entity;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class MethodEntity extends Entity {
    transient private ResolvedMethodDeclaration resolved;
    private String signature;
    private String qualifiedSignature;

    @Relationship(type = "HAS_METHOD", direction = Relationship.INCOMING)
    private TypeEntity declareType;

    @Relationship(type = "EXTENDS")
    private Set<MethodEntity> extendedMethods = new HashSet<>();

    public MethodEntity(ResolvedMethodDeclaration resolved, TypeEntity declareType) {
        this.resolved = resolved;
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.declareType = declareType;
        declareType.addHasMethod(this);
    }

    public void addExtendedMethods(Set<MethodEntity> extendedMethods) {
        this.extendedMethods.addAll(extendedMethods);
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
}
