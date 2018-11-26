package com.github.woooking.cosyn.util

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
      * @param qualifiedName 方法的简化名称
      * @return 是否是get方法
      */
    def isGetMethod(simpleName: String): Boolean = {
        simpleName.matches("^get[A-Z].*")
    }
}
