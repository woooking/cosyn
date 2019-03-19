package com.github.woooking.cosyn.kg.entity;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
public class MethodJavadocEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private String returnDescription;

    @Relationship(type = "PARAM")
    private Set<MethodParamJavadocEntity> params = new HashSet<>();

    public MethodJavadocEntity() {
    }

    public MethodJavadocEntity(Javadoc javadoc) {
        var groupedTags = javadoc.getBlockTags().stream().collect(Collectors.groupingBy(JavadocBlockTag::getType));
        this.params = groupedTags.getOrDefault(Type.PARAM, List.of()).stream()
            .map(blockTag -> new MethodParamJavadocEntity(blockTag.getName().get(), blockTag.getContent().toText()))
            .collect(Collectors.toSet());
        this.returnDescription = groupedTags.getOrDefault(Type.RETURN, List.of())
            .stream()
            .findFirst()
            .map(JavadocBlockTag::getContent)
            .map(JavadocDescription::toText)
            .orElse(null);
        this.description = javadoc.getDescription().toText();
    }

    public String getDescription() {
        return description;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public Set<MethodParamJavadocEntity> getParams() {
        return Collections.unmodifiableSet(params);
    }
}
