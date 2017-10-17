package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-10-14
 * Time: 14:59
 * <br></br><br></br>
 */
class InvokeLineMarkerProvider : RelatedItemLineMarkerProvider() {
    /**
     * Small.openUri("detail?from=app.home", getContext());
     */
    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>?) {
        if(element is PsiFile) {
            NavTable.updateByScan(element.project)
        }

        NavTable.table.filter { MyPsiUtil.contains(it.invokePsiSet, element) }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    NavigationGutterIconBuilder.create(BundleJsonLineMarkerProvider.ICON)
                            .setPopupTitle("SmallHelper")
                            .setTargets(it.flatMap{ listOf(it.bridgePsi).plus(it.declarePsiSet) })
                            .setTooltipText("Navigate to bundle.json or declaration")
                            .createLineMarkerInfo(element)
                }
                ?.apply {
                    result?.add(this)
                }
    }
}