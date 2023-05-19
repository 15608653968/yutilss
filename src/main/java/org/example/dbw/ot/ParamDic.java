package org.example.dbw.ot;

//一些默认配置
public interface ParamDic {

    String sqlFkey="SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_COLUMN_NAME,REFERENCED_TABLE_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE CONSTRAINT_SCHEMA= ? AND REFERENCED_TABLE_NAME is not null";
    String sqlGetTb = "select table_name,table_comment from information_schema.tables where table_schema = ?";

    String sqlColumns = "select (@i:=@i+1) AS rownum,table_name,column_name,column_comment,data_type,column_default,\n" +
            "CASE \n" +
            "\tWHEN CHARACTER_MAXIMUM_LENGTH is not null  THEN\n" +
            "\t\tCHARACTER_MAXIMUM_LENGTH\n" +
            "\tWHEN NUMERIC_PRECISION  is not null  THEN\n" +
            "\t\tNUMERIC_PRECISION\n" +
            "END data_length,\n" +
            "IF(NUMERIC_SCALE is null,0,NUMERIC_SCALE) dpoint,\n" +
            "is_nullable,\n" +
            "IF(column_key = \"PRI\",\"Y\",\"N\") ispri\n" +
            " from information_schema.columns,(SELECT @i := 0) AS i  where  table_schema=?  ORDER BY rownum";

}
