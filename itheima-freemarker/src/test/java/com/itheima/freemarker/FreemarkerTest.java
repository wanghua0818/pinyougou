package com.itheima.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;

import java.io.FileWriter;
import java.util.*;

public class FreemarkerTest {
    @Test
    public void test() throws Exception {
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        //设置默认生成文件编码
        configuration.setDefaultEncoding("utf-8");
        //设置模板路径
        configuration.setClassForTemplateLoading(FreemarkerTest.class, "/ftl");
        //获取模板
        Template template = configuration.getTemplate("test.ftl");
        //加载数据
        Map<String, Object> dataModel = new HashMap<>();
        List<Map<String, String>> goodsList = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("name", "橘子");
        map.put("price", "23");
        goodsList.add(map);
        Map<String, String> map2 = new HashMap<>();
        map2.put("name", "苹果");
        map2.put("price", "12");
        goodsList.add(map2);
        dataModel.put("goodsList", goodsList);
        dataModel.put("name", "孤儿马");
        dataModel.put("message", "马华阳");
        dataModel.put("today", new Date());
        dataModel.put("number",123456789);
        //创建输出对象
        FileWriter fileWriter = new FileWriter("D:/test/test.html");
        //渲染模板和数据
        template.process(dataModel, fileWriter);
        //关闭输出
        fileWriter.close();
    }
}
