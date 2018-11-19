package com.github.woooking.cosyn.entity;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class MethodEntity extends Entity {
    private String qualifiedSignature;

    public MethodEntity(String qualifiedSignature) {
        this.qualifiedSignature = qualifiedSignature;
    }
}
