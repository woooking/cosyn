package com.github.woooking.cosyn.entity;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.neo4j.ogm.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@NodeEntity
public class MethodEntity {
    @Id
    private String qualifiedSignature;
    private String signature;
    private String simpleName;
    private boolean isStatic;
    private boolean isConstructor;
    private boolean isDeprecated;
    private Modifier.Keyword accessSpecifier;
    private String paramNames;

    @Relationship(type = "HAS_METHOD", direction = Relationship.INCOMING)
    private TypeEntity declareType;

    @Relationship(type = "EXTENDS")
    private Set<MethodEntity> extendedMethods = new HashSet<>();

    @Relationship(type = "PRODUCES")
    private TypeEntity produce;

    @Relationship(type = "PRODUCES_MULTIPLE")
    private TypeEntity produceMultiple;

    @Relationship(type = "JAVADOC")
    private MethodJavadocEntity javadoc;

    @Transient
    private Map<String, String> paramJavadocs;

    public MethodEntity() {
    }

    public MethodEntity(ResolvedConstructorDeclaration resolved, boolean isDeprecated, TypeEntity declareType, MethodJavadocEntity javadoc) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = true;
        this.isConstructor = true;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadoc;
        var paramNames = new ArrayList<String>();
        var paramNum = resolved.getNumberOfParams();
        for (int i = 0; i < paramNum; ++i) {
            var param = resolved.getParam(i);
            paramNames.add(param.getName());
        }
        this.paramNames = String.join(",", paramNames);
        declareType.addHasMethod(this);
    }

    public MethodEntity(ResolvedMethodDeclaration resolved, boolean isDeprecated, TypeEntity declareType, MethodJavadocEntity javadoc) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = resolved.isStatic();
        this.isConstructor = false;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadoc;
        var paramNames = new ArrayList<String>();
        var paramNum = resolved.getNumberOfParams();
        for (int i = 0; i < paramNum; ++i) {
            var param = resolved.getParam(i);
            paramNames.add(param.getName());
        }
        this.paramNames = String.join(",", paramNames);
        declareType.addHasMethod(this);
    }

    @PostLoad
    public void setup() {
        this.paramJavadocs = javadoc == null ?
            Map.of() :
            javadoc.getParams().stream().collect(Collectors.toMap(MethodParamJavadocEntity::getName, MethodParamJavadocEntity::getDescription));
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

    public Keyword getAccessSpecifier() {
        return accessSpecifier;
    }

    public String getParamNames() {
        return paramNames;
    }

    public MethodJavadocEntity getJavadoc() {
        return javadoc;
    }

    public String getParamJavadoc(String param) {
        return this.paramJavadocs.get(param);
    }

    public Set<MethodEntity> getExtendedMethods() {
        return Collections.unmodifiableSet(extendedMethods);
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
