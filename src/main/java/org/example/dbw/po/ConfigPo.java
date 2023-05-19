package org.example.dbw.po;

public class ConfigPo {
    private  String driver = "com.mysql.cj.jdbc.Driver";
    private  String url = "jdbc:mysql://127.0.0.1:3306/test?serverTimezone=GMT%2B8";
    //数据库账号
    private  String userName = "root";
    //数据库密码
    private  String passWord = "123456";
    //对应数据库  此处是你本地对应的数据库名称
    private  String database = "user-center";
    //对应输出地址
    private  String reportPath = ".\\";

    // 创建12 个栅格,单个栅格宽度 720 为 0.5英寸
    private  Integer celwidw = 720;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public Integer getCelwidw() {
        return celwidw;
    }

    public void setCelwidw(Integer celwidw) {
        this.celwidw = celwidw;
    }
}
