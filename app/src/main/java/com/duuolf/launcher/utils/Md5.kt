package com.duuolf.launcher.utils

import java.security.MessageDigest

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray()) // 计算 MD5
    return digest.joinToString("") { "%02x".format(it) } // 转换为十六进制字符串
}