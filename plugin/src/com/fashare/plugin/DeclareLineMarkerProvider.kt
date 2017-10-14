package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiKeyword

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-10-14
 * Time: 15:49
 * <br></br><br></br>
 */
class DeclareLineMarkerProvider : RelatedItemLineMarkerProvider(){
    override fun getLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        return null
    }

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>?) {
        var cur: PsiElement? = element
        while(cur != null && cur !is PsiKeyword){
            cur = cur.prevSibling
        }

        if((cur as? PsiKeyword)?.text != PsiKeyword.CLASS){
            return
        }

        NavTable.table.filter {
                    MyPsiUtil.isEquals(it.declarePsi, element)
                            && it.pkgName == (element.containingFile as? PsiJavaFile)?.packageName
                }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    NavigationGutterIconBuilder.create(TestLineMarkerProvider.ICON)
                            .setTargets(it.map{ it.bridgePsi })
                            .setTooltipText("Navigate to a simple property")
                            .createLineMarkerInfo(element)
                }
                ?.apply {
                    result?.add(this)
                }
    }
}