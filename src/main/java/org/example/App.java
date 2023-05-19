package org.example;

import org.example.dbw.ExportToWord;
import org.example.dbw.po.ConfigPo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App{
    public static void main( String[] args )
    {
        Logger global = Logger.getGlobal();
        if (args == null || args.length==0) {
            global.info("请传入配置文件地址！！！\n " +
                    "配置文件示例\n" +
                    "database=数据库名称\n" +
                    "driver=驱动包\n" +
                    "url=连接地址\n" +
                    "userName=用户名\n" +
                    "passWord=密码\n" +
                    "reportPath=生成文件保存地址\n\n" +
                    "");
            return;
        }
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(args[0])) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ConfigPo configPo = new ConfigPo();
        configPo.setCelwidw(Integer.parseInt(properties.getProperty("celwidw","684")));
        configPo.setDatabase(properties.getProperty("database"));
        configPo.setDriver(properties.getProperty("driver"));
        configPo.setUrl(properties.getProperty("url"));
        configPo.setPassWord(properties.getProperty("passWord"));
        configPo.setUserName(properties.getProperty("userName"));
        configPo.setReportPath(properties.getProperty("reportPath"));
        ExportToWord.genDbDesigDoc(configPo);
    }
}