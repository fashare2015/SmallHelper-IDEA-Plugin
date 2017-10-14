package com.fashare.plugin

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
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
                            //              println(TAG + "find !!!\n");
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
                println(TAG + "=======> " + psiJavaFile?.packageName + "  ." + psiJavaFile?.name)

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
                        val find = it.text == key + "Activity" || it.text ==  key + "Fragment"
                        println(TAG + it.text + ", " + key)
                        if(find)
                            println(TAG + "find !!!\n")
                        find
                    })
                }

                println(TAG + "<======= " + psiJavaFile?.name + "\n")
            }
        }
        return result
    }

//    fun <T : PsiElement> findMainDeclareTargets(project: Project, key: String, clazz: Class<T>): List<T> {
//        var result: MutableList<T> = mutableListOf()
//        val virtualFiles = FileBasedIndex.getInstance().getContainingFiles<FileType, Void>(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
//        for (virtualFile in virtualFiles) {
//            val psiJavaFile = PsiManager.getInstance(project).findFile(virtualFile) as PsiJavaFile?
//
//            if (psiJavaFile!!.name == "R.java")
//                continue
//
//            println(TAG + "=======> " + psiJavaFile.name)
//            if (psiJavaFile != null) {
//                val properties = PsiTreeUtil.findChildrenOfType(psiJavaFile, PsiClass::class.java)
//                for (property in properties) {
//                    val psiClassIdentify: Collection<T> = PsiTreeUtil.findChildrenOfType(property, PsiKeyword::class.java)
//                            .filter { it.text == PsiKeyword.CLASS }
//                            .map{
//                                var cur: PsiElement? = it
//                                while(cur != null && cur !is PsiIdentifier){
//                                    cur = cur.nextSibling
//                                }
//                                cur
//                            }
//                            .filter { it != null }
//                            as Collection<T>
//
//                    result.addAll(psiClassIdentify.filter{
//                        val find = it.text == key + "Activity" || it.text ==  key + "Fragment"
//                        println(TAG + it.text + ", " + key)
//                        if(find)
//                            println(TAG + "find !!!\n")
//                        find
//                    })
//                }
//            }
//
//            println(TAG + "<======= " + psiJavaFile.name + "\n\n\n\n")
//        }
//        return result
//    }
}
