package com.github.woooking.cosyn.comm.util

object StringUtil {
    def decapitalize(s: String): String = {
        if (s == null) null
        else if (s.length == 0) ""
        else if (s.charAt(0).isLower) s
        else {
            val chars = s.toCharArray
            chars(0) = chars(0).toLower
            new String(chars)
        }
    }
}
