# yutilss 数据库设计文档自动生成
## 简介
 项目开发完成后经常需要数据库设计文档，可以通过工具或代码逆向生成，但是网上的工具通常功能不全，所以自己就码出来一套，现在分享给大家

## 特点
 使用非常简单只需要一行代码即可生成数据库设计文档
- 1. 导出标识符
- 2. 导出er图
- 3. 导出逻辑结构设计
- 4. 导出物理结构设计
 代码十分简单，只使用了poi与uml，二开十分方便

![输入图片说明](image.png)
## 使用方式
- 执行命令
`java -jar MainClass.jar prop.txt`  
- prop.txt 参数说明 test 目录下有示例

```
    database=数据库名称
    driver=驱动包（注意MySQL版本区别）
    url=连接地址
    userName=用户名
    passWord=密码
    reportPath=生成文件保存地址
```
##环境介绍
发行版jar包是以java11 编译的，可以更改源码编译版本。

