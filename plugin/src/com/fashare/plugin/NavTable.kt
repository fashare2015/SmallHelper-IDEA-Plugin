package com.fashare.plugin

import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

object NavTable{
    const val TAG = "NavTable: "
    val table: MutableSet<Row> = mutableSetOf()

    fun updateByScan(project: Project) {
        SimpleUtil.updateNavTableByScan(project)
    }

    fun addRow(row: Row) {
        val target = table.find {
            it.bridgePsi?.name == row.bridgePsi?.name
                    && it.bridgePsi?.value == row.bridgePsi?.value
        }

        if(target == null)
            table.add(row)
        else{
            target.fill(row)
        }
    }

    data class Row(var invokePsiSet: MutableSet<PsiElement> = mutableSetOf(),
                   var bridgePsi: JsonProperty,
                   var declarePsiSet: MutableSet<PsiElement> = mutableSetOf(),
                   var pkgName: String?) {

        fun fill(other: Row) {
            invokePsiSet.addAll(other.invokePsiSet.filter {
                !MyPsiUtil.contains(invokePsiSet, it)
            })
            other.bridgePsi?.apply { bridgePsi = this }
            declarePsiSet.addAll(other.declarePsiSet.filter {
                !MyPsiUtil.contains(declarePsiSet, it)
            })
            other.pkgName?.apply { pkgName = this }
        }

        override fun toString(): String {
            return """
                Row: {
                    bridgePsi(uri): ${bridgePsi?.text},
                    invokePsiSet(Small.openUri()): ${invokePsiSet.map { it.text }},
                    pkgName(pkg): "$pkgName",
                    declarePsiSet(rules): ${declarePsiSet.map { it.text }}
                }"""
        }
    }

    fun print(){
        table.forEach { println(TAG + it) }
        print("\n\n")
    }
}