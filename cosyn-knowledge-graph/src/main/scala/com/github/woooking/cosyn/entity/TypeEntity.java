package com.github.woooking.cosyn.entity;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.google.common.collect.ImmutableSet;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class TypeEntity {
    transient private ResolvedReferenceTypeDeclaration resolved;
    @Id
    private String qualifiedName;
    private boolean isInterface;

    @Relationship(type = "HAS_METHOD")
    private Set<MethodEntity> hasMethods = new HashSet<>();

    @Relationship(type = "EXTENDS")
    private Set<TypeEntity> extendedTypes = new HashSet<>();

    @Relationship(type = "EXTENDS", direction = Relationship.INCOMING)
    private Set<TypeEntity> subTypes = new HashSet<>();

    @Relationship(type = "PRODUCES", direction = Relationship.INCOMING)
    private Set<MethodEntity> producers = new HashSet<>();

    public TypeEntity() {
    }

    public TypeEntity(ResolvedReferenceTypeDeclaration resolved, boolean isInterface) {
        this.resolved = resolved;
        this.qualifiedName = resolved.getQualifiedName();
        this.isInterface = isInterface;
    }

    public void addHasMethod(MethodEntity methodEntity) {
        this.hasMethods.add(methodEntity);
    }

    public void addExtendedTypes(Set<TypeEntity> extendedTypes) {
        this.extendedTypes.addAll(extendedTypes);
        this.extendedTypes.forEach(t -> t.addSubType(this));
    }

    public void addSubType(TypeEntity subType) {
        this.subTypes.add(subType);
    }

    public void addProducer(MethodEntity producer) {
        this.producers.add(producer);
    }

    public ResolvedReferenceTypeDeclaration getResolved() {
        return resolved;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public Set<MethodEntity> getHasMethods() {
        return hasMethods;
    }

    public Set<TypeEntity> getExtendedTypes() {
        return ImmutableSet.copyOf(extendedTypes);
    }

    public Set<TypeEntity> getSubTypes() {
        return ImmutableSet.copyOf(subTypes);
    }

    public Set<MethodEntity> getProducers() {
        return ImmutableSet.copyOf(producers);
    }
}
