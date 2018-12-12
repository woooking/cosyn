package com.github.woooking.cosyn.pattern.model.ty

sealed trait Type

object Type {
    private val PrimitiveTypes = Array(
        "boolean",
        "byte",
        "short",
        "int",
        "long",
        "float",
        "double",
        "char",
    )

    object PrimitiveOrString {
        def unapply(ty: Type): Option[String] = ty match {
            case BasicType(t) if PrimitiveTypes.contains(t) || t == "java.lang.String" => Some(t)
            case _ => None
        }
    }

    def fromString(ty: String): Type = {
        if (ty.endsWith("[]")) ArrayType(fromString(ty.take(ty.length - 2)))
        else BasicType(ty)
    }
}

case class BasicType(ty: String) extends Type {
    override def toString: String = ty
}

case class ArrayType(componentType: Type) extends Type {
    override def toString: String = s"$componentType[]"
}


