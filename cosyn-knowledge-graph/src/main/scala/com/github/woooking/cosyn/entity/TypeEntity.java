package com.github.woooking.cosyn.entity;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public class TypeEntity extends Entity {
    private String qualifiedName;
    private boolean isInterface;

    @Relationship(type = "EXTENDS")
    private Set<TypeEntity> extendedTypes;

    @Relationship(type = "IMPLEMENTS")
    private Set<TypeEntity> implementedTypes;

    public TypeEntity(String qualifiedName, boolean isInterface) {
        this.qualifiedName = qualifiedName;
        this.isInterface = isInterface;
    }

    public void addExtendedTypes(Set<TypeEntity> extendedTypes) {
        this.extendedTypes.addAll(extendedTypes);
    }

    public void addImplementedTypes(Set<TypeEntity> implementedTypes) {
        this.implementedTypes.addAll(implementedTypes);
    }
}
