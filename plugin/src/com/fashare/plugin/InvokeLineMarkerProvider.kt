package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-10-14
 * Time: 14:59
 * <br></br><br></br>
 */
class InvokeLineMarkerProvider : RelatedItemLineMarkerProvider() {
    companion object {
        private val TAG = "InvokeLineMarkerProvider: "
    }

    override fun getLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        return null
    }

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>?) {
        NavTable.table.filter { MyPsiUtil.contains(it.invokePsiSet, element) }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    println(TAG + element::class.java.simpleName + "-> " + element.text + "!!! get\n\n")

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