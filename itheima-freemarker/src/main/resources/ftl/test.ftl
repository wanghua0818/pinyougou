<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>FreeMaker</title>
</head>
<body>
<#--这是freemarker注释,不会输出到文件上-->
<h2>${name}--${message}</h2>
<#--简单类型-->
<#assign username="黑马"/>
用户名:${username}
<#--对象-->
<#assign person={"age":"18","gender":"男"}/>
<div>
    年龄:${person.age} <br>
</div>
性别:${person.gender}<br>
<#--include-->
<#include "test2.ftl"/><br>
<#--if-->
<#assign boo1=true/>
<#if boo1>
    <em style="color: chartreuse">嘿嘿嘿</em>
<#else>
    <em style="color:red">哈哈哈</em>
</#if>
<br>
<#list goodsList as goods>
    ${goods_index},名称为:${goods.name} 价格为:${goods.price} <br>
</#list>
<#--获取集合总记录数-->
总共${goodsList?size}条记录
<br>
<#--将字符串转换为json对象-->
<#assign str="{'id':123,'text':'itcast'}">
<#assign jsonObj=str?eval>
id为:${jsonObj.id} <br>
text为:${jsonObj.text}
<br>
<#--日期对象-->
当前日期为:${today?date} <br>
当前时间为:${today?time}<br>
当前日期时间为:${today?datetime}<br>
格式化当前日期时间:${today?string("yyyy年MM月dd日 HH时mm分ss秒")}<br>
<#--数字转换为字符串-->
${number} ---- ${number?c} <br>
<#--空值处理-->
${estr!"孤儿马华阳"}<br>
<#--判断变量是否存在-->
<#if str??>
    str存在
<#else >
    str不存在
</#if>
</body>
</html>