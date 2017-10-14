package com.fashare.plugin

import com.intellij.psi.PsiElement

object MyPsiUtil {
    fun isEquals(a: PsiElement?, b: PsiElement?): Boolean {
        return a?.javaClass == b?.javaClass && a?.text == b?.text
    }

    fun contains(a: MutableSet<PsiElement>, b: PsiElement): Boolean {
        return a.find { isEquals(it, b) } != null
    }

}