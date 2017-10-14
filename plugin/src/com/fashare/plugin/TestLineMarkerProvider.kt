package com.fashare.plugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression


/**
 * Created by jinliangshan on 2017/9/30.
 */
class TestLineMarkerProvider : RelatedItemLineMarkerProvider() {
    companion object {
        private val TAG = "TestLineMarkerProvider: "

        val ICON = IconLoader.getIcon("/icons/ic_launcher.png")
    }

    override fun getLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        return null
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
        // uri: 匹配 "detail" 以及 "detail?from=app.home"
        (element as? JsonProperty)
                ?.takeIf { it.name == "uri" && it.value is JsonStringLiteral }
                ?.let {
                    val bundlePsi = it.parent as? JsonObject

                    val pkgPsi = bundlePsi?.children?.find {
                        val child = it as? JsonProperty
                        child?.name == "pkg" && child.value is JsonStringLiteral
                    } as? JsonProperty
                    val pkgValue = (pkgPsi?.value as? JsonStringLiteral)?.value

                    val uriValue = (it.value as? JsonStringLiteral)?.value

                    val pathValue = "Main"  // TODO: find in manifest

                    println(TAG + element::class.java.simpleName + "-> " + element.text + "!!! get\n\n\n\n")
                    val properties = SimpleUtil.findProperties(element.project, "\"${uriValue}", PsiLiteralExpression::class.java)
                            .filter {
                                !it.text.split("?")[0].contains("/")
                            }

                    val declareTargets = SimpleUtil.findDeclareTargets(element.project, pathValue, pkgValue?: "", PsiElement::class.java)

                    properties.forEach {
                        NavTable.addRow(NavTable.Row(mutableSetOf(it), element as JsonProperty, declareTargets.firstOrNull(), pkgValue))
                    }

                    NavTable.print()

                    properties.map{
                                var cur: PsiElement = it
                                while(cur !is PsiCallExpression) {
                                    cur = cur.parent
                                }
                                cur
                            }
                            .plus(declareTargets)
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                NavigationGutterIconBuilder.create(TestLineMarkerProvider.ICON)
                                        .setTargets(it)
                                        .setTooltipText("Navigate to a simple property")
                                        .createLineMarkerInfo(element)
                            }

                }?.apply {
                    result?.add(this)
                }

        // rules: 匹配 "detail/sub" 以及 "detail/sub?from=app.home"
        (element as? JsonProperty)
                ?.takeIf {
                    val rulesPsi = it.parent.parent
                    (rulesPsi as? JsonProperty)?.name == "rules"
                }
                ?.let {
                    val bundlePsi = it.parent.parent.parent as? JsonObject

                    val pkgPsi = bundlePsi?.children?.find {
                        val child = it as? JsonProperty
                        child?.name == "pkg" && child.value is JsonStringLiteral
                    } as? JsonProperty
                    val pkgValue = (pkgPsi?.value as? JsonStringLiteral)?.value

                    val uriPsi = bundlePsi?.children?.find {
                        val child = it as? JsonProperty
                        child?.name == "uri" && child.value is JsonStringLiteral
                    } as? JsonProperty
                    val uriValue = (uriPsi?.value as? JsonStringLiteral)?.value

                    val pathName = it.name
                    val pathValue = (it.value as? JsonStringLiteral)?.value

                    println(TAG + element::class.java.simpleName + "-> " + element.text + "!!! get\n\n\n\n")
                    val properties = SimpleUtil.findProperties(element.project, "\"${uriValue}", PsiLiteralExpression::class.java)
                            .filter {
                                if(it.text.length < 2)
                                    return@filter false
                                it.text.substring(1, it.text.length-1)  // 去掉双引号
                                        .split("?")[0] == "$uriValue/$pathName"     // detail/sub?from=app.home 取?号前部分 == detail + ／ + sub
                            }

                    val declareTargets = SimpleUtil.findDeclareTargets(element.project, "$pathValue", pkgValue?: "", PsiElement::class.java)

                    properties.forEach {
                        NavTable.addRow(NavTable.Row(mutableSetOf(it), element as JsonProperty, declareTargets.firstOrNull(), pkgValue?: ""))
                    }

                    NavTable.print()

                    properties.map{
                                var cur: PsiElement = it
                                while(cur !is PsiCallExpression) {
                                    cur = cur.parent
                                }
                                cur
                            }
                            .plus(declareTargets)
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                NavigationGutterIconBuilder.create(TestLineMarkerProvider.ICON)
                                        .setTargets(it)
                                        .setTooltipText("Navigate to a simple property")
                                        .createLineMarkerInfo(element)
                            }

                }?.apply {
                    result?.add(this)
                }
    }
}
