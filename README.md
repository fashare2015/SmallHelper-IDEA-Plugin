# SmallHelper
为插件化框架 [Small] 建立页面索引和快速跳转

## Small简介
>Small 通过路由表 `bundle.json` 配置页面路由，从而达到模块间的解耦。
see [Small/wiki/UI-route]

以跳转 (detail模块)的 MainActivity 为例：

|路由表 `bundle.json` | 调用处 `xxx.java` | 声明处 `pkg/xxxActivity.java` |
|:----:|:----:|:----:|
|"uri": "detail" | Small.openUri("detail"); | (detail模块) MainActivity.java |

## 识别隐式依赖, 建立页面索引
![screen-record/small.gif]

## 安装使用：install
- Preference -> Plugins
  - Browse repos `SmallHelper`: 插件需要2～3天审核，暂时应该还搜不到
  - Install Plugin From Disk: 下载 [SmallHelper-1.0-SNAPSHOT.zip]，本地安装

## 源码调试: clone && run
如果您对源码感兴趣的话：
1. clone 本项目
2. 命令行执行 `./gradlew runIde`

## bug 反馈
如果您在使用中遇到bug, 请务必提Issue，附上`IDEA版本`以及`错误栈信息`。

## 感谢
[官方文档](http://www.jetbrains.org/intellij/sdk/docs/welcome.html)

[官方源码案例](https://github.com/JetBrains/intellij-sdk-docs/tree/master/code_samples/simple_language_plugin)

[官方gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)


[Small]: https://github.com/wequick/Small
[Small/wiki/UI-route]: https://github.com/wequick/Small/wiki/UI-route
[screen-record/small.gif]: screen-record/small.gif
[SmallHelper-1.0-SNAPSHOT.zip]: release-output/SmallHelper-1.0-SNAPSHOT.zip
