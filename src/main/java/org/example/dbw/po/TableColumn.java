package org.example.dbw.po;
//表属性
public class TableColumn {
    // 表名
    private String tableName;
    // 字段名
    private String columnName;
    // 字段类型
    private String columnType;
    // 字段注释
    private String columnComment;
    // 可否为空
    private String isNullable;
    // 约束
    private String columnKey;

    //默认值
    private String defValue;
    //数据长度
    private String dataLength;

    //小数位
    private String dpoint;
    //序号
    private String rownum;

    public String getRownum() {
        return rownum;
    }

    public void setRownum(String rownum) {
        this.rownum = rownum;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public String getDataLength() {
        return dataLength;
    }

    public void setDataLength(String dataLength) {
        this.dataLength = dataLength;
    }

    public String getDpoint() {
        return dpoint;
    }

    public void setDpoint(String dpoint) {
        this.dpoint = dpoint;
    }
}
