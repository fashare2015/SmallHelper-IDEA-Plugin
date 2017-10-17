package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.*

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-10-14
 * Time: 15:49
 * <br></br><br></br>
 */
class DeclareLineMarkerProvider : RelatedItemLineMarkerProvider(){
    /**
     * package net.wequick.example.small.app.detail;
     *
     * public class MainFragment extends Fragment{...} or
     * public class MainActivity extends Activity{...} or
     *
     * public class SplashActivity extends Activity{...} (with "android.intent.category.LAUNCHER" in AndroidManifest.xml)
     */
    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>?) {
        if(element is PsiFile) {
            NavTable.updateByScan(element.project)
        }

        if(!isJavaClassDeclarePsi(element))
            return

        NavTable.table.filter {
                    MyPsiUtil.contains(it.declarePsiSet, element)
                            && it.pkgName == (element.containingFile as? PsiJavaFile)?.packageName
                }
                .takeIf { it.isNotEmpty() }
                ?.let{
                    NavigationGutterIconBuilder.create(BundleJsonLineMarkerProvider.ICON)
                            .setPopupTitle("SmallHelper")
                            .setTargets(it.flatMap{ listOf(it.bridgePsi).plus(it.invokePsiSet.map{ MyPsiUtil.findFirstAncestor(it, PsiCallExpression::class.java)?: it }) })
                            .setTooltipText("Navigate to bundle.json or invocation")
                            .createLineMarkerInfo(element)
                }
                ?.apply {
                    result?.add(this)
                }
    }

    fun isJavaClassDeclarePsi(element: PsiElement): Boolean{
        var cur: PsiElement? = element
        while(cur != null && cur !is PsiKeyword){
            cur = cur.prevSibling
        }

        return (cur as? PsiKeyword)?.text == PsiKeyword.CLASS
    }
}