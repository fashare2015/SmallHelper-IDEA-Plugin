# 「AS 插件实战 - coding篇」让 AS 识别你的路由框架，建立页面索引（二）
>本系列共有两篇，简要介绍了从上手到发布插件的主要流程：
 1. [「AS 插件实战 - 环境搭建」让 AS 识别你的路由框架，建立页面索引（一）][env-setup]
 2. [「AS 插件实战 - coding篇」让 AS 识别你的路由框架，建立页面索引（二）][coding]

 上一篇主要说了 `idea-plugin` 环境搭建，这一篇中将介绍代码实现。

## 0. 效果图
 ![screen-record/small.gif]

## 1. 需求回顾
我们需要根据路由框架特定的 `urlscheme` 建立页面索引，即实现 `find-usage` 。

具体对框架 `Small` 而言，则是如何连接这三者，以跳转 (detail模块)的 MainActivity 为例：

|路由表 `bundle.json` | 调用处 `xxx.java` | 声明处 `pkg/xxxActivity.java` |
|:----:|:----:|:----:|
|"uri": "detail" | Small.openUri("detail"); | (detail模块) MainActivity.java |

其他路由框架也逃不开 `路由表`，`调用处`，`声明处` 这三者。

> PS: 关于 Small 详细的路由规则，请参看 [Small/wiki/UI-route]

## 2. 代码实现
### 2.1 路由信息收集——PSI
#### 2.1.1 PSI 介绍
从前面的需求可知，我们有两类文件需要分析，分别是 `.json` 和 `.java`。在IDEA的世界里，所有东西以 [PSI]（程序结构索引）的形式存在，上边的文件会分别对应到 `JsonFile` 和 `PsiJavaFile`。

如果把 `.java` 文件的语法结构看作 `.html` 的 DOM 结构，事实上你会得到一个 `PSI树`。细致到每一个类、方法、字面量、甚至空格，都对应一个类似 html 里的标签，我们称之为 `PsiElement`。
树结构总是复杂的，所幸的是 IDEA 提供了查看的工具，`Tools->View PSI Structure`:

![psi-viewer]

我们贴了一个简单的java类，然后 `Build PSI Tree`，可以看到一个类似 DOM 的结构。里边每一个节点都继承于 `PsiElement`，你可以理解为Android中 `TextView`、`ImageView` 等与 `View` 的关系。
其中，常用的有:

 - PsiJavaFile: .java 文件
 - PsiClass: 整个类代码块，不含 package 和 import
 - PsiField: 变量声明语句
 - PsiLiteralExpression: 字面量，字符串常量之类的
 - 。。。

#### 2.1.2 PSI 解析
脑补成 `View树` 之后，也许你能猜到，官方提供了类似 `findViewById()` 的方法。
```java
public class PsiTreeUtil {
    public static <T extends PsiElement> Collection<T> findChildrenOfType(PsiElement root, Class<? extends T> targetClass){...}
}
```
如 `PsiTreeUtil.findChildrenOfType(psiJavaFile, PsiLiteralExpression.class)` 返回一个列表，包含这个 .java 文件中所有的字面量。然后，我们可以从中筛选出 “符合我们自定义urlscheme” 的那些。

在此之前，我们需要拿到 `psiJavaFile`，`FileBasedIndex` 中有相关方法可以遍历整个 `Project` 的文件。更多细节可以查阅 [PsiFile]

总结一下，可以这么实现：
  1. 扫描整个 project，找到 bundle.json, 解析出其中的 "uri": "detail" (作为桥接者，我称之为 `bridgePsi`)
  2. 根据这个 bridgePsi, 遍历所有java文件，解析出其中的 Small.open("detail") (作为调用方，我称之为 `invokePsi`)
  3. 根据这个 bridgePsi, 遍历 detail 模块下所有java文件，解析出其中的 xxxActivity.java 或 xxxFragment.java (作为声明方，我称之为 `declarePsi`)
  4. 收集完毕，把对应关系保存在全局的单例中 —— `NavTable`

### 2.2 路由信息展示——LineMarkerProvider
IDEA 提供了 `LineMarkerProvider`，如效果图所示，给每行提供一个可跳转的 icon。其他的功能还有 `Action`, `findUsagesProvider` 等。
这些功能都需要在 `resources/META-INF/plugin.xml` 里注册。
看下面一段代码，给每一个 Small.openUri() 加一个行标记。其中，这个回调在每次打开文件时会触发一次。

参数：
  - element：该文件下所有的 PsiElement 都会被遍历到
  - result：如果当前 element 符合要求，即是 Small.openUri() 这样的格式，就向 result 中加入关联的 PsiElement。（.setTargets 那一行）
```kotlin
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
```

### 2.3 版本兼容
一般需要注意的是 plugin.xml 里的 `<idea-version since-build="141.0"/>`。它指定了插件支持的最小版本号，即 IDEA-IC 141.0。
然后，则是sdk兼容问题，如果在某个IDE上没效果，直接把sdk换成这个IDE对应的版本，善用调试功能。

>之前有遇到，某个全局搜.json文件的api，在AS2.3.3上无效，最后避开它，换了另一个api来实现。

## 3. 发布
发布需要在 `plugin.xml` 中注明插件功能、作者、版本等信息。注意用英文，否则过不了审核。然后到 jetbrains 注册个账号，往插件仓库上传即可。

## 4. 感谢
[官方文档](http://www.jetbrains.org/intellij/sdk/docs/welcome.html)

[官方源码案例](https://github.com/JetBrains/intellij-sdk-docs/tree/master/code_samples/simple_language_plugin)

[官方gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)

[EventBus3 插件](https://github.com/likfe/eventbus3-intellij-plugin)

[阿里 Java 开发规约插件](https://github.com/alibaba/p3c)

[env-setup]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/blob/master/learning-to-develop-plugin/env-setup.md
[coding]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/blob/master/learning-to-develop-plugin/coding-and-publish.md
[screen-record/small.gif]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/raw/master/screen-record/small.gif
[Small/wiki/UI-route]: https://github.com/wequick/Small/wiki/UI-route
[PSI]: http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/psi.html
[psi-viewer]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/raw/master/screen-record/psi-viewer.png
[PsiFile]: http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/psi_files.html
