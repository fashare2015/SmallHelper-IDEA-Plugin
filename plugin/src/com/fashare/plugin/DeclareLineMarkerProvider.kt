package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement

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



        NavTable.table.filter { MyPsiUtil.isEquals(it.declarePsi, element) }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    NavigationGutterIconBuilder.create(TestLineMarkerProvider.ICON)
                            .setTargets(it.map{
                                var cur = it.bridgePsi
//                                while(cur !is PsiCallExpression) {
//                                    cur = cur?.parent
//                                }
                                cur
                            })
                            .setTooltipText("Navigate to a simple property")
                            .createLineMarkerInfo(element)
                }
                ?.apply {
                    result?.add(this)
                }
    }
}