package com.github.woooking.cosyn.entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class JavadocParamEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;

    public JavadocParamEntity() {
    }

    public JavadocParamEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
