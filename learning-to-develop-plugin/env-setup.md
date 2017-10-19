# 「AS 插件实战」让 AS 识别你的路由框架，建立页面索引（一）
>说到AS插件，个人用的最多的是`GsonFormat`，json一键转javabean，实乃神器。
然而，插件只是作为辅助，完全不了解也不影响开发，我又是为啥开了这个项目？

## 1. 缘起
### 1.1 大势所趋的组件化
>随着组件化和插件化的兴起，越来越多的项目开始以此来解耦。通常会自定义`urlscheme`，来代替依赖具体的页面名。

有这样的：`阿里组件化框架 ARouter`
```java
ARouter.getInstance().build("/test/activity").navigation();
```
或者这样的：`插件化框架 Small`
```java
Small.openUri("main", this);
```
再或者，你们公司自己有一套路由框架...

### 1.2 这样有啥问题？？
哇，解耦是解耦了，可是。。。到底跳哪去了？？？
这也是有些人觉得`EventBus`不好用的原因。

### 1.3 IDE：你问我，我也很绝望啊
![small-can-not-navigate.png]

怎么办呢，妥协吗？妥协是不可能妥协的，这一辈子都不会妥协，只有写写插件才勉强维持得了生活。。

### 1.4 经过一番激烈的调教...
![screen-record/small.gif]

### 1.5 少儿不宜的项目源码
https://github.com/fashare2015/SmallHelper-IDEA-Plugin

## 2. 权衡
### 2.1 你真的需要它吗？
内心纠结中：
 - 小人A："看起来方便多了！"
 - 小人B："可是插件开发完全不会啊 orz"
 - 小人A："gayhub上例子多了, 照猫画虎呗"
 - 小人B："有那时间我项目都做完了..."
 - 小人A："可。。这一劳永逸啊"
 - 小人B："我说，对着路由表找，也不麻烦嘛～"
 - 。。。

### 2.2 然而，嘴上说不要，身体却。。。
> 不知不觉看起了 `idea 文档`

好了，言归正传，我们来看看怎么实现它。分两步：
  1. 环境搭建
  2. 代码实现

别看搭环境简单，从idea插件project默认的`.iml配置`换成`.gradle配置`，踩了不少坑。
网上资料基本没有`.gradle写插件`的资料，只能对着英文文档硬撸。最后折腾下来，反而代码实现更快更轻松。
可能有人要吐槽了，这鸟玩意儿有这么难搞吗？那个，人家是第一次嘛～

### 2.3 下车提示
以下全文都是环境相关的介绍，多年插件开发的老司机可以到站下车了。
  1. 开发 ide 选择：`只能用 IDEA ？？？`
  2. sdk 选择：`IDEA or AS`，`高版本 or 低版本`
  3. 依赖管理：`.iml or .gradle，为啥？`
  4. 开发语言：`java垃圾，我想用kotlin，怎么配？？？`
  5. 版本兼容：`我在 IDEA 上 run 的好好的，装到 AS 上咋不行了？？？`

老司机们，先别走，如果回答不出上述问题，下文还是值得一看的。

> 另，关于代码实现将在续篇（二）中讲解。

## 3. 环境搭建
### 3.1 政治正确的姿势
`IDEA or AS 皆可` + `IDEA 较低版本sdk` + `gradle` + `kotlin` + `兼容IDEA所有系列（理想中...）`

### 3.2 开发 ide 选择
虽说是两者皆可，但还是推荐用IDEA，毕竟是本体。
用AS写的话，由于不能 `new Plugin project`，
怎么办呢，直接`new Java project`或者`new Android project`。
然后从其他插件项目中copy一份`resources/META-INF/plugin.xml`过来即可。

### 3.3 sdk 选择
这个其实也都可以，但是推荐 `IDEA 较低版本sdk`。
因为，如果你用高版本开发，用了低版本上没有的Api，你的插件就run不起来了。
反之，则不然。我用的是 `IDEA IC-141.3058.30`，即插件打包后，将支持`IDEA 141.3058.30+`的所有IDE。

> IDEA 每个版本有一个版本号，以此递增：[IDEA 各版本下载](https://www.jetbrains.com/idea/download/previous.html)

### 3.4 依赖管理
你说 `new Plugin project` 默认生成 `.iml`，为啥还换成 `.gradle`，麻烦死了。
我只能说 too young，sometimes naive!

> 话说iml是gradle出来之前，idea用来管理依赖的配置文件，类似xml格式。
和gradle一样，每个模块都会有一个对应的iml

#### 3.4.1 默认的目录结构，基于iml
```
.
├── .idea/                          // ide 自动生成的代码
├── META-INF
│   └── plugin.xml                  // 插件版本说明，功能定义
├── resources                       // 资源根目录
│   └── icons
│       └── icon.png
├── src                             // 代码根目录
│   └── com
│       └── kgmyshin
│           └── ideaplugin
│               └── eventbus3
│                   ├── EventBus3LineMarkerProvider.java
│
├── eventbus3-intellij-plugin.iml   // 依赖配置
```

来看最后一个`.iml`文件：

  - <module>指定了模块类型：`PLUGIN_MODULE->plugin`，`JAVA_MODULE->java`
  - 资源、代码根目录等均有它配置：`/META-INF/plugin.xml`，`/src`，`/resources`
  - <orderEntry>则指定了库依赖，可以引入第三方库，如rxjava

```xml
<module type="PLUGIN_MODULE" version="4">
  <component name="DevKit.ModuleBuildProperties" url="file://$MODULE_DIR$/META-INF/plugin.xml" />
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://$MODULE_DIR$">
      <sourceFolder url="file://$MODULE_DIR$/src" isTestSource="false" />
      <sourceFolder url="file://$MODULE_DIR$/resources" isTestSource="false" />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
  </component>
</module>
```
然而，事实上，整个项目的依赖是由`.iml`和`.idea/`共同决定的。
很多github上的插件工程clone下来以后，找不到plugin模块，
就是因为不清楚这个，把`.idea/`目录加进`.gitignore`了！！！就是这么操蛋。
[stackoverflow/如何导入已有的插件工程](https://stackoverflow.com/questions/18278440/how-to-import-and-run-existing-plugins-from-intellij-community-edition-repo)

#### 3.4.2 gradle 还是熟悉的味道
使用 `.iml` 意味着 `.idea/` 要跟着版本管理走，很难受。学习iml配置还有额外成本。

I\'m angry! 直接上gradle把：
```
.
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── plugin
│   └── src                         // 代码根目录
│   │   └── main
|   |       ├── java
│   │       ├── kotlin
│   │       │   └── com
│   │       │       └── fashare
│   │       │           └── plugin
│   │       │               ├── BundleJsonLineMarkerProvider.kt
│   │       └── resources           // 资源根目录
│   │           ├── META-INF
│   │           │   └── plugin.xml  // 插件版本说明，功能定义
│   │           └── icons
│   │               └── ic_launcher.png
|   ├── build.gradle                // 依赖配置
|
├── build.gradle                    // 依赖配置
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle                 // 依赖配置
```
目录结构基本和 Android 工程一模一样，这也是前面说为啥可以用 AS 来开发。

> PS: 其实这时.iml文件都还在，只是全都在.idea/目录下。由于gradle构建时会生成.iml, 所以全都可以 `gitigiore`。

#### 3.4.3 gradle-intellij-plugin
摒弃.iml而用gradle构建项目，也是官方推荐的，为此还专门做了一个 `groovy插件`：

https://github.com/JetBrains/gradle-intellij-plugin

它包含以下功能 `gradle task`：
 - runIde: 运行、调试插件
 - buildPlugin: 打包插件为 `XXX.zip`
 - publishPlugin: 上传插件到 `官方插件仓库`

在 gradle 中，简单的集成：
```gradle
plugins {
    id "org.jetbrains.intellij" version "0.2.17"
}

apply plugin: 'org.jetbrains.intellij'

intellij {
    pluginName 'SmallHelper'

    // 开发插件所依赖的 IDEA sdk 版本
    def useLocalIdeaSdk = true      // 默认使用本地 IDEA-SDK
    if(useLocalIdeaSdk) {
        localPath '/Applications/IntelliJ IDEA 14 CE.app/Contents'
//        localPath '/Applications/Android Studio.app/Contents'   // mac 下 idea 的路径
    }else {
        version = 'IC-145.2070.6'   // 从远程下载特定版本的 IDEA-SDK (200+MB), 比较慢...
    }

    updateSinceUntilBuild false     // 重要！！！ 用来兼容其他平台，(与plugin.xml中的since-build有关)
    sandboxDirectory = project.rootDir.canonicalPath + "/.sandbox" //插件生成的临时文件的地址

    publishPlugin {
        username 'fashare2015'
        password JETBRAINS_PASSWORD
    }
}
```
主要看 `version` 和 `localPath` 两个参数, 都是用来指定 IDEA sdk 的。
  - version: 从远端下载`指定版本`sdk，超级慢！！！
  - localPath: 从本地`任意IDEA系列`作为sdk，你愿意的话，甚至可以用WebStorm。

一开始不知道有 `localPath`，以为本地的用不了了，下载又超慢，浪费我几个小时。

> 话说，前些天很火的 [阿里 Java 开发规约插件](https://github.com/alibaba/p3c) 也是基于这个配置的。我也。。嗯。。借鉴了下，开源的事。。能叫抄吗？

### 3.5 开发语言 kotlin
你问我怎么弄啊？目录结构都和Android一样了，自己弄去。

废话太多，写的有点长了，版本兼容什么的且听下回分解。

## 4. 总之，有没有跃跃欲试
个人而言，应付日常需求如果已经游刃有余了。不妨跳出舒适区，挑战一个完全陌生的领域，
对查阅英文文档、工程能力是很大的提升。尤其是最后完成的那一刻，瞬间宛如高潮般的体验。

[small-can-not-navigate.png]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/raw/master/screen-record/small-can-not-navigate.png
[screen-record/small.gif]: https://github.com/fashare2015/SmallHelper-IDEA-Plugin/raw/master/screen-record/small.gif