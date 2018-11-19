package com.github.woooking.cosyn.entity;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class TypeEntity extends Entity {
    private String qualifiedName;
    private boolean isInterface;

    public TypeEntity(String qualifiedName, boolean isInterface) {
        this.qualifiedName = qualifiedName;
        this.isInterface = isInterface;
    }
}
