package com.github.woooking.cosyn.entity;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.google.common.collect.ImmutableSet;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class TypeEntity extends Entity {
    transient private ResolvedReferenceTypeDeclaration resolved;
    private String qualifiedName;
    private boolean isInterface;

    @Relationship(type = "HAS_METHOD")
    private Set<MethodEntity> hasMethods = new HashSet<>();

    @Relationship(type = "EXTENDS")
    private Set<TypeEntity> extendedTypes = new HashSet<>();

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
    }

    public void addImplementedTypes(Set<TypeEntity> implementedTypes) {
        this.extendedTypes.addAll(implementedTypes);
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
}
