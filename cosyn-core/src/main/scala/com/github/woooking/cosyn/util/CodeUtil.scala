package com.github.woooking.cosyn.util

import com.github.javaparser.JavaParser
import com.github.woooking.cosyn.entity.MethodEntity

object CodeUtil {
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

    /**
      * 从类的全限定名称中提取简化名称，即以'.'分割后的最后一段字符串
      * 例：
      * Object => Object
      * java.lang.Object => Object
      * org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener => SheetRecordCollectingListener
      * @param qualifiedName 类的全限定名称
      * @return 类的简化名称
      */
    def qualifiedClassName2Simple(qualifiedName: String): String = {
        qualifiedName.split('.').last
    }

    /**
      * 根据方法名称判断一个方法是否是get方法，即方法名以get开头，后面接一个大写字母
      * 例：
      * get => false
      * getSheet => true
      * getsheet => false
      * @param simpleName 方法的简化名称
      * @return 是否是get方法
      */
    def isGetMethod(simpleName: String): Boolean = {
        simpleName.matches("^get[A-Z].*")
    }

    /**
      * 从方法签名中提取参数类型列表
      * 例：
      * func() => []
      * func(int, long) => ["int", "long"]
      * org.test.func(java.lang.Object, float) => ["java.lang.Object", "float"]
      * @param signature 方法的签名
      * @return 参数类型列表
      */
    def methodParams(signature: String): Seq[String] = {
        val pattern = """.*\(([a-zA-Z., ]*)\)""".r
        pattern.findFirstMatchIn(signature) match {
            case Some(m) => m.group(1).split(", ")
            case None => Seq()
        }
    }

    /**
      * 从方法签名中提取简单方法名
      * 例：
      * func() => "func"
      * func(int, long) => "func"
      * org.test.func(java.lang.Object, float) => "func"
      * @param signature 方法的签名
      * @return 方法的简单名
      */
    def methodSimpleNameParams(signature: String): String = {
        val pattern = """([a-zA-Z_]*.)*(?<name>[a-zA-Z_]*)\(([a-zA-Z_.,]*)\)""".r
        pattern.findFirstMatchIn(signature) match {
            case Some(m) => m.group("name")
            case None => ???
        }
    }

    /**
      * 判断一个类型名是否是原始类型
      * @param ty 需要判断的类型
      * @return 是否是原始类型
      */
    def isPrimitive(ty: String): Boolean = {
        PrimitiveTypes.contains(ty)
    }

    def parseJavadoc(): Unit = {
//        JavaParser.parseJavadoc();
    }
}
