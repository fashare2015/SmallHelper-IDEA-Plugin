package com.fashare.plugin

import com.intellij.psi.PsiElement

object MyPsiUtil {
    fun isEquals(a: PsiElement?, b: PsiElement?): Boolean {
//        return a?.javaClass == b?.javaClass && a?.text == b?.text
        return a === b
    }

    fun contains(a: MutableSet<PsiElement>, b: PsiElement): Boolean {
        return a.find { isEquals(it, b) } != null
    }

    fun <T: PsiElement> findFirstAncestor(it: PsiElement, ancestorClazz: Class<T>): T?{
        var cur :PsiElement? = it
        while(cur != null && !ancestorClazz.isAssignableFrom(cur.javaClass)){
            cur = cur.parent
        }
        return cur as? T
    }

}