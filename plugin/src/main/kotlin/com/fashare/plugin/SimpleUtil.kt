package com.fashare.plugin

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.FileBasedIndex
import java.util.*

object SimpleUtil {
    val TAG = "SimpleUtil: "

    fun <T : PsiElement> findProperties(project: Project, key: String, clazz: Class<T>): List<T> {
        var result: MutableList<T>? = null
        val virtualFiles = FileBasedIndex.getInstance().getContainingFiles<FileType, Void>(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        for (virtualFile in virtualFiles) {
            val psiJavaFile = PsiManager.getInstance(project).findFile(virtualFile) as PsiJavaFile?

            if (psiJavaFile!!.name == "R.java")
                continue

            //      println(TAG + "=======> " + psiJavaFile.getName());
            if (psiJavaFile != null) {
                val properties = PsiTreeUtil.findChildrenOfType(psiJavaFile, clazz)
                if (properties != null) {
                    for (property in properties) {
                        //            println(TAG + property.getText() + ", " + key);
                        if (property.text.startsWith(key)) {
                            //              println(TAG + "updateNavTableByScan !!!\n");
                            if (result == null) {
                                result = ArrayList()
                            }
                            result.add(property)
                        }
                    }
                }
            }

            //      println(TAG + "<======= " + psiJavaFile.getName() + "\n\n\n\n");
        }
        return if (result != null) result else emptyList()
    }

    fun <T : PsiElement> findDeclareTargets(project: Project, key: String, pkg: String, clazz: Class<T>): List<T> {
        var result: MutableList<T> = mutableListOf()
        val virtualFiles = FileBasedIndex.getInstance().getContainingFiles<FileType, Void>(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        for (virtualFile in virtualFiles) {
            val psiJavaFile = PsiManager.getInstance(project).findFile(virtualFile) as PsiJavaFile?

            if (psiJavaFile?.name == "R.java")
                continue

            if (psiJavaFile?.packageName == pkg) {
//                println(TAG + "=======> " + psiJavaFile?.packageName + "  ." + psiJavaFile?.name)

                val properties = PsiTreeUtil.findChildrenOfType(psiJavaFile, PsiClass::class.java)
                for (property in properties) {
                    val psiClassIdentify: Collection<T> = PsiTreeUtil.findChildrenOfType(property, PsiKeyword::class.java)
                            .filter { it.text == PsiKeyword.CLASS }
                            .map{
                                var cur: PsiElement? = it
                                while(cur != null && cur !is PsiIdentifier){
                                    cur = cur.nextSibling
                                }
                                cur
                            }
                            .filter { it != null }
                            as Collection<T>

                    result.addAll(psiClassIdentify.filter{
                        val _key = if(key.startsWith(".")) key.substring(1) else key

                        val find = it.text == _key + "Activity" || it.text ==  _key + "Fragment"
//                        println(TAG + it.text + ", " + _key)
//                        if(updateNavTableByScan)
//                            println(TAG + "updateNavTableByScan !!!\n")
                        find
                    })
                }

//                println(TAG + "<======= " + psiJavaFile?.name + "\n")
            }
        }
        return result
    }

    fun updateNavTableByScan(project: Project) {
        // getContainingFiles() 不知为啥在 AS 2.3.3 上无效
//        val virtualFiles = FileBasedIndex.getInstance().getContainingFiles<FileType, Void>(FileTypeIndex.NAME, JsonFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        val virtualFiles = FilenameIndex.getVirtualFilesByName(project, "bundle.json", GlobalSearchScope.projectScope(project))
        for (virtualFile in virtualFiles) {
            val jsonFile = PsiManager.getInstance(project).findFile(virtualFile) as? JsonFile? ?: continue

            println(TAG + "=======> " + jsonFile.name)

            val jsonProps = PsiTreeUtil.findChildrenOfType(jsonFile, JsonProperty::class.java)

            jsonProps.filter { it.name == "uri" && it.value is JsonStringLiteral }
                    .forEach {
                        val bundlePsi = it.parent as? JsonObject

                        /**
                         * updateNavTableByScan "uri": "detail"
                         */
                        val uriPsi = it
                        val uriValue = (uriPsi.value as? JsonStringLiteral)?.value

                        /**
                         * updateNavTableByScan "pkg": "net.wequick.example.small.app.detail"
                         */
                        val pkgPsi = bundlePsi?.children?.find {
                            val child = it as? JsonProperty
                            child?.name == "pkg" && child.value is JsonStringLiteral
                        } as? JsonProperty
                        val pkgValue = (pkgPsi?.value as? JsonStringLiteral)?.value

                        /**
                         * updateNavTableByScan "rules" : {
                                    "sub": "Sub",
                                    "sub/aa": "Sub2"
                                }
                         */
                        val rulesPsi = bundlePsi?.children?.find {
                            val child = it as? JsonProperty
                            child?.name == "rules" && child.value is JsonObject
                        } as? JsonProperty
                        val rulesValue = rulesPsi?.value as? JsonObject

                        SimpleUtil.findProperties(project, "\"${uriValue}", PsiLiteralExpression::class.java)
                                .filter { it.text.length >= 2 && it.text.startsWith("\"") && it.text.endsWith("\"") }   // 带有双引号
                                .forEach {
                                    val invokeTarget = it
                                    val invokePathValue = it.text.substring(1, it.text.length-1).split("?")[0]  // 去掉双引号, 取?号前部分

                                    if(!invokePathValue.contains("/")){                         // detail?from=app.home 取?号前部分 不含 /
                                        /**
                                         * updateNavTableByScan default Activity in manifest
                                         */
                                        var useMainAsDefault = true
                                        FilenameIndex.getFilesByName(project, "AndroidManifest.xml", GlobalSearchScope.projectScope(project))
                                                .filter{
                                                    PsiTreeUtil.findChildrenOfType(it, XmlTag:: class.java)
                                                            .filter{
                                                                it.text.startsWith("<manifest")
                                                            }
                                                            .map{ it.getAttribute("package") }
                                                            .filter{ it != null && it.value == pkgValue }
                                                            .isNotEmpty()
                                                }
                                                .forEach {
                                                    PsiTreeUtil.findChildrenOfType(it, XmlTag:: class.java)
                                                            .filter{
                                                                it.text.startsWith("<activity")
                                                            }
                                                            .filter{
                                                                PsiTreeUtil.findChildrenOfType(it, XmlTag:: class.java)
                                                                        .filter{ it.text.startsWith("<category")
                                                                                && it.getAttribute("android:name")?.value == "android.intent.category.LAUNCHER"
                                                                        }.isNotEmpty()
                                                            }
                                                            .forEach {
                                                                val pathValue = it.getAttribute("android:name")?.value?.let{
                                                                    if(it.endsWith("Activity")) it.substring(0, it.length - "Activity".length)  else ""
                                                                }

                                                                val declareTargets = SimpleUtil.findDeclareTargets(project, pathValue?: "", pkgValue?: "", PsiElement::class.java)
                                                                NavTable.addRow(NavTable.Row(mutableSetOf(), uriPsi, declareTargets.toMutableSet(), pkgValue))

                                                                if(pathValue != "Main" && pathValue != ".Main")
                                                                    useMainAsDefault = false
                                                            }
                                                }

                                        val pathName = ""
                                        val pathValue = "Main"
                                        println(TAG + "$pathName: $pathValue")

                                        val declareTargets = SimpleUtil.findDeclareTargets(project, pathValue?: "", pkgValue?: "", PsiElement::class.java)
                                                .filter{ useMainAsDefault || it.text.endsWith("Fragment") }
                                        NavTable.addRow(NavTable.Row(mutableSetOf(invokeTarget), uriPsi, declareTargets.toMutableSet(), pkgValue))

                                    }else{
                                        rulesValue?.propertyList?.forEach{
                                            val pathName = it.name
                                            val pathValue = (it.value as? JsonStringLiteral)?.value
                                            println(TAG + "$pathName: $pathValue")

                                            if(invokePathValue == "$uriValue/$pathName") {       // detail/sub?from=app.home 取?号前部分 == detail + ／ + sub
                                                val declareTargets = SimpleUtil.findDeclareTargets(project, pathValue?: "", pkgValue?: "", PsiElement::class.java)
                                                NavTable.addRow(NavTable.Row(mutableSetOf(invokeTarget), it, declareTargets.toMutableSet(), pkgValue))
                                            }
                                        }
                                    }
                                }
                    }

            NavTable.print()

            println(TAG + "<======= " + jsonFile.name + "\n")
        }
    }
}
