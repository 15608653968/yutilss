# yutilss 数据库设计文档自动生成
## 简介
 项目开发完成后经常需要数据库设计文档，可以通过工具或代码逆向生成，但是网上的工具通常功能不全,要不就是没有ER图，要不就是导出的Word格式混乱，所以自己就码出来一个小工具，现在分享给大家，希望大家喜欢

## 特点
 使用非常简单只需要一行代码即可生成数据库设计文档
- 1. 导出标识符
- 2. 导出er图
- 3. 导出逻辑结构设计
- 4. 导出物理结构设计
- 5. 代码十分简单，只使用了poi与uml，二开十分方便
- 6. 使用栅格设计，使导出的word格式美观
- 7. 等比缩放ER图，设置4个实体一张图片，能将ER图完整清晰的展示在word上，同时ER实体图与关系图分开展示，解决了工具生成ER图拥挤不完整的情况，同时标注了关联关系
- 8. 逻辑设计部分与物理设计部分，由于大小伙伴使用数据库时肯没有写注释，会自动判断，没有注释的展示原始字段

![输入图片说明](image.png)
## 使用方式
- 执行命令
`java -jar MainClass.jar prop.txt`  
- prop.txt 参数说明 test 目录下有示例

```
    celwidw= A4 使用此设置，默认是A4 684，代表单个栅格宽度大概1厘米左右
    database=数据库名称
    driver=驱动包（注意MySQL版本区别）
    url=连接地址
    userName=用户名
    passWord=密码
    reportPath=生成文件保存地址
```
## 环境介绍
发行版jar包是以java11 编译的，可以更改源码编译版本。

## 支持的数据库
- mysql 理论上mysql一族的都支持，需要自行更换驱动，默认是mysql8.0

## 支持的文档
- word  使用栅格设计将正文行分为12个栅格，内容按照栅格展示，使页面整齐美观，默认使A4的栅格，可以设置单个栅格的` celwidw `宽度以适应不同纸张大小684是A4纸栅格宽度

## 下一版本更新
- 1. ER 关系图，遇见关系特别多表特别多的情况，缩放严重，无法清晰展示，需要自行再次切割。 下一版本打算 将链式关系单独导出，完成自动切割，实现生成就能直接打印的完美目标
- 2. ER 关系图中没有标注一对一，一对多，多对多等关系； 因为自动判断是根据主键或唯一约束来判断的不一定符合实际业务的关系，所以这一版本没有加上，下一版本添加开关可自行控制是否生成

## 求Star
第一次写开源项目,希望大家多多支持，多提意见，喜欢的可以点个Star （不喜欢的也可点 …………^_^）

