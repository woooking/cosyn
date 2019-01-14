package com.github.woooking.cosyn.util

import com.github.woooking.cosyn.code.model.Type

object CodeUtil {
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
    def methodParams(signature: String): Seq[Type] = {
        val pattern = """.*\(([a-zA-Z., ]*)\)""".r
        pattern.findFirstMatchIn(signature) match {
            case Some(m) =>
                val g = m.group(1)
                if (g != "") {
                    g.split(", ").map(Type.fromString)
                } else {
                    Seq()
                }
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

}