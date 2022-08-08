#

[![fun.gatsby:commons-lang package in public@Release feed in Azure Artifacts](https://feeds.dev.azure.com/expressed/18c3bc69-f653-4776-b17f-d7cea24c5d6a/_apis/public/Packaging/Feeds/public@Release/Packages/571960af-198f-4aeb-85eb-8a9bb502e624/Badge)](https://dev.azure.com/expressed/PJ/_artifacts/feed/public@Release/maven/fun.gatsby%2Fcommons-lang)

# 引入

在pom文件中导入目标仓库

```xml

<repository>
    <id>azure</id>
    <url>https://pkgs.dev.azure.com/expressed/PJ/_packaging/public/maven/v1</url>
</repository>
```

添加项目依赖

```xml

<dependency>
    <groupId>fun.gatsby</groupId>
    <artifactId>commons-lang</artifactId>
    <version>${commons.version}</version>
</dependency>
```
