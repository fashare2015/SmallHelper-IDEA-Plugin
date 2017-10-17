package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile


/**
 * Created by jinliangshan on 2017/9/30.
 */
class BundleJsonLineMarkerProvider : RelatedItemLineMarkerProvider() {
    companion object {
        val ICON = IconLoader.getIcon("/icons/ic_launcher.png")
    }

    /**
     *  {
            "uri": "detail",
            "pkg": "net.wequick.example.small.app.detail",
            "rules": {
                "sub": "Sub",
                "sub/aa": "Sub2"
            }
        }
     */
    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>?) {
        if(element is PsiFile) {
            NavTable.updateByScan(element.project)
        }

        NavTable.table.filter { MyPsiUtil.isEquals(it.bridgePsi, element) }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    NavigationGutterIconBuilder.create(BundleJsonLineMarkerProvider.ICON)
                            .setPopupTitle("SmallHelper")
                            .setTargets(it.flatMap{
                                it.declarePsiSet.plus(it.invokePsiSet.map{ MyPsiUtil.findFirstAncestor(it, PsiCallExpression::class.java)?: it })
                            })
                            .setTooltipText("Navigate to invocation or declaration")
                            .createLineMarkerInfo(element)
                }
                ?.apply {
                    result?.add(this)
                }
    }
}
