package org.example.dbw.ot;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.security.SFile;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.example.dbw.po.ConfigPo;
import org.example.dbw.po.ForeignKey;
import org.example.dbw.po.Table;
import org.example.dbw.po.TableColumn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EtService {

    Logger global = Logger.getGlobal();
    private Connection conn = null;
    private ConfigPo config;

    private ResultSet rs = null;
    private PreparedStatement pst = null;

    public EtService(ConfigPo config) {
        global.info("开始获取数据库连接...");
        this.config = config;
        try {
            Class.forName(config.getDriver());
            this.conn = DriverManager.getConnection(config.getUrl(), config.getUserName(), config.getPassWord());
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取查询值
     *
     * @param database
     * @param tableName
     * @param sql
     * @return
     */
    private ResultSet getResult(String database, String tableName, String sql) {
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, database);
            if (!"".equals(tableName)) {
                pst.setString(2, tableName);
            }
//            global.info("打印SQL:"+pst.toString());
            return pst.executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取表的外键关系
     *
     * @param database
     * @param tableName
     * @return
     * @throws SQLException
     */
    public List<ForeignKey> getTbForeignKey(String database, String tableName) throws SQLException {
        rs = getResult(database, tableName, ParamDic.sqlFkey);
        List<ForeignKey> fkeys = new ArrayList<>();
        while (rs.next()) {
            ForeignKey item = new ForeignKey();
            item.setTableName(rs.getString("TABLE_NAME"));
            item.setConstraintName(rs.getString("CONSTRAINT_NAME"));
            item.setColumnName(rs.getString("COLUMN_NAME"));
            item.setReferencedColumnName(rs.getString("REFERENCED_COLUMN_NAME"));
            item.setReferencedTableName(rs.getString("REFERENCED_TABLE_NAME"));
            fkeys.add(item);
        }
        this.releaseConn();
        return fkeys;
    }


    /**
     * 获取所有表
     *
     * @param database
     * @return
     * @throws Exception
     */
    public List<Table> getTables(String database) throws Exception {
        rs = getResult(database, "", ParamDic.sqlGetTb);
        List<Table> tables = new ArrayList<>();
        while (rs.next()) {
            Table table = new Table();
            table.setTableName(rs.getString("table_name"));
            table.setTableCommont(rs.getString("table_comment"));
            tables.add(table);
        }
        this.releaseConn();
        return tables;
    }

    /**
     * 关闭连接
     */
    private void releaseConn() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取表和字段
     *
     * @param database
     * @return
     * @throws Exception
     */
    public Map<String, List<TableColumn>> getDbTbcData(String database) throws Exception {
        List<TableColumn> columns = this.getColumns(database, "");
        return columns.stream().collect(Collectors.groupingBy(TableColumn::getTableName));
    }

    public List<TableColumn> getColumns(String database, String tableName) throws Exception {
        rs = getResult(database, tableName, ParamDic.sqlColumns);
        List<TableColumn> tableColumns = new ArrayList<>();
        while (rs.next()) {
            TableColumn tc = new TableColumn();
            tc.setTableName(rs.getString("table_name"));
            tc.setColumnName(rs.getString("column_name"));
            tc.setColumnType(rs.getString("data_type"));
            tc.setColumnKey(rs.getString("ispri"));
            tc.setIsNullable(rs.getString("is_nullable"));
            tc.setColumnComment(rs.getString("column_comment"));
            tc.setDataLength(rs.getString("data_length"));
            tc.setDefValue(rs.getString("column_default"));
            tc.setDpoint(rs.getString("dpoint"));
            tc.setRownum(rs.getInt("rownum") + "");
            tableColumns.add(tc);
        }
        this.releaseConn();
        return tableColumns;

    }

    // 分割erTu
    public static <T> List<List<T>> partList(List<T> source, int n) {
        if (source == null) {
            return null;
        }

        if (n == 0) {
            return null;
        }
        List<List<T>> result = new ArrayList<List<T>>();
        // 集合长度
        int size = source.size();
        // 余数
        int remaider = size % n;
        // 商
        int number = size / n;

        for (int i = 0; i < number; i++) {
            List<T> value = source.subList(i * n, (i + 1) * n);
            result.add(value);
        }
        if (remaider > 0) {
            List<T> subList = source.subList(size - remaider, size);
            result.add(subList);
        }
        return result;
    }


    /**
     * 导出标识符与状态
     * @param data
     * @param tableMap
     * @param document
     */
    public void genFlag(Map<String, List<TableColumn>> data, Map<String, String> tableMap, XWPFDocument document) {

        global.info("一、开始导出标识符与状态...");
        Vector<Vector<String>> tav = new Vector<>();
        data.keySet().parallelStream().forEach(a -> {
            List<TableColumn> tableColumns = data.get(a); // 获取表的字段信息
            Vector<String> rowCelVales = new Vector<>();
            rowCelVales.add(a);
            tableColumns.parallelStream().forEach(
                    x -> {
                        if ("Y".equals(x.getColumnKey())) {
                            rowCelVales.add(x.getColumnName());
                        }
                    }
            );
            if (rowCelVales.size() == 1) {
                rowCelVales.add("");
            }
            rowCelVales.add(tableMap.get(a));
            rowCelVales.add("生产使用");
            tav.add(rowCelVales);

        });

        XWPFParagraph pg1 = document.createParagraph();                // 创建标题对象
        pg1.setStyle("2");
        XWPFRun runt1 = pg1.createRun();                                 // 创建文本对象
        runt1.setText("一、标识符与状态");
        runt1.setFontFamily("宋体");
        runt1.setFontSize(16);
        runt1.setBold(true);// 字体加粗


        XWPFTable table = document.createTable(tav.size() + 1, 5);

        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.addNewTblLayout().setType(STTblLayoutType.FIXED);

        // 第一行
        table.setCellMargins(10, 10, 10, 10);
        String[] title = new String[]{"编号", "表名", "数据唯一标识", "描述", "状态"};
        int[] lattice = new int[]{1, 2, 2, 4, 3};
        // 表头
        int j = 0;
        //栅格宽
        Integer celwidw = config.getCelwidw();
        for (int i = 0; i < 5; i++) {
            XWPFTableCell cell = table.getRow(j).getCell(i);
            CTTc ctTc = cell.getCTTc();
            CTTcPr ctTcPr = ctTc.addNewTcPr();
            ctTcPr.addNewShd().setFill("cccccc");
            CTTblWidth cellw = ctTcPr.addNewTcW();
            cellw.setType(STTblWidth.DXA);
            cellw.setW(new BigInteger(lattice[i] * celwidw + ""));
            CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
            XWPFParagraph p = cell.getParagraph(ctP);
            XWPFRun crun = p.createRun();
            p.setAlignment(ParagraphAlignment.CENTER);
            crun.setText(title[i]);
            crun.setFontFamily("仿宋");
            crun.setFontSize(9);                                                 // 字体大小
            crun.setBold(true);// 字体加粗
        }
        j++;
        for (int i = 0; i < tav.size(); i++) {
            Vector<String> val = tav.get(i);
            for (int i1 = 0; i1 < 5; i1++) {
                XWPFTableCell cell = table.getRow(j).getCell(i1);
                CTTc ctTc = cell.getCTTc();
                CTTcPr ctTcPr = ctTc.addNewTcPr();
                CTTblWidth cellw = ctTcPr.addNewTcW();
                cellw.setType(STTblWidth.DXA);
                cellw.setW(new BigInteger(lattice[i1] * celwidw + ""));
                CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
                XWPFParagraph p = cell.getParagraph(ctP);
                XWPFRun crun = p.createRun();
                p.setAlignment(ParagraphAlignment.CENTER);
                crun.setFontFamily("宋体");
                crun.setFontSize(9);
                if (i1 == 0) {
                    crun.setText(i + 1 + "");
                } else {
                    crun.setText(val.get(i1 - 1));
                }
            }
            j++;

        }
    }


    /**
     * 导出er模型
     * @param data
     * @param tableMap
     * @param document
     * @param tbForeignKey
     */
    public void genErModle(Map<String, List<TableColumn>> data, Map<String, String> tableMap, XWPFDocument document, List<ForeignKey> tbForeignKey) {
        global.info("二、开始导出er模型（生成过程耗时较长请耐心等待）...");
        Set<String> tbs = data.keySet();
        List<String> tblsit = new ArrayList<>(tbs);
        List<List<String>> lists = partList(tblsit, 8);
        int inc = 1;
        String erpath = config.getReportPath() + "er\\";

        for (List<String> t : lists) {
            StringBuffer umlStr = new StringBuffer();
            umlStr.append("@startuml\n");
            t.forEach(s -> {
                List<TableColumn> tc = data.get(s);
//                String tbc = tableMap.get(s);
//                if (StringUtil.isBlank(tbc)) {
//                    tbc=s;
//                }
                umlStr.append("entity " + s + " {\n");
                Iterator<TableColumn> iterator = tc.iterator();
                boolean hasnext = iterator.hasNext();
                while (hasnext) {
                    TableColumn next = iterator.next();
                    String cm = next.getColumnName();
//                    if (StringUtil.isBlank(cm)) {
//                        cm=next.getColumnName();
//                    }
                    umlStr.append("  " + ("Y".equals(next.getColumnKey()) ? "*" : "") + " " + cm + " : " + next.getColumnType() + "\n");
                    hasnext = iterator.hasNext();
                    if (hasnext) {
                        umlStr.append("  --\n");
                    }
                }
                umlStr.append("}\n");
            });
            umlStr.append("@enduml");
            SourceStringReader reader = new SourceStringReader(umlStr.toString());


            String itemPath = erpath + "er" + inc + ".png";
            File output = new File(itemPath);
            try {
                reader.generateImage(SFile.fromFile(output));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inc++;
        }
        // 没有外键
        boolean noneFkey= tbForeignKey == null || tbForeignKey.size()==0;
        String linkPath=erpath + "link.png";
        if (!noneFkey) {
            StringBuilder tgkStr = new StringBuilder();
            tgkStr.append("@startuml\n");
            StringBuilder enStr = new StringBuilder();
            StringBuilder glStr = new StringBuilder();
            tbForeignKey.forEach(a -> {
                String tableName = a.getTableName();
                String referencedTableName = a.getReferencedTableName();
//            if (!StringUtil.isBlank(tableMap.get(tableName))) {
//                tableName=tableMap.get(tableName);
//            }
//            if (!StringUtil.isBlank(tableMap.get(referencedTableName))) {
//                referencedTableName=tableMap.get(referencedTableName);
//            }
                enStr.append("entity " + tableName + " {}\n");
                enStr.append("entity " + referencedTableName + " {}\n");

                glStr.append(tableName + " --> " + referencedTableName + " :" + a.getColumnName() + "-" + a.getReferencedColumnName() + "\n");
            });
            tgkStr.append(enStr);
            tgkStr.append(glStr);
            tgkStr.append("@enduml");

            SourceStringReader reader = new SourceStringReader(tgkStr.toString());

            File output = new File(linkPath);
            try {
                reader.generateImage(SFile.fromFile(output));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        XWPFParagraph pg1 = document.createParagraph();                // 创建标题对象
        pg1.setStyle("2");
        XWPFRun runt1 = pg1.createRun();                                 // 创建文本对象
        runt1.setText("二、ER模型");
        runt1.setFontFamily("宋体");
        runt1.setFontSize(16);
        runt1.setBold(true);// 字体加粗

        XWPFParagraph pg2 = document.createParagraph();
        XWPFRun runt2 = pg2.createRun();
        runt2.setFontFamily("宋体");
        runt2.setFontSize(9);
        runt2.setText("1、实体属性图");
        runt2.setBold(true);
        for (int i = 1; i <= inc; i++) {
            String erImgPath = erpath + "er" + i + ".png";
            if (i == inc) {

                erImgPath = linkPath;
                XWPFParagraph pg3 = document.createParagraph();
                XWPFRun runt3 = pg3.createRun();
                runt3.setFontFamily("宋体");
                runt3.setFontSize(9);
                runt3.setText("2、实体关系图");
                runt3.setBold(true);

                if (noneFkey) {
                    XWPFParagraph pg4 = document.createParagraph();
                    XWPFRun runt4 = pg4.createRun();
                    runt4.setFontFamily("宋体");
                    runt4.setFontSize(9);
                    runt4.setText("数据库中没有关联关系");

                    break;
                }
            }
            // 图片段落
            XWPFParagraph imgPg = document.createParagraph();
            XWPFRun imgRun = imgPg.createRun();
            try (FileInputStream fis = new FileInputStream(erImgPath)) {
                byte[] bs = IOUtils.toByteArray(fis);
                BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(bs));
                int width = img.getWidth();
                int height = img.getHeight();
                int picW = 400;
                int picH = 720;
                //保持图片完整展示在word中
                if (width >= height) {
                    picH = (int) (picW * ((double) height / width));
                } else {
                    picW = (int) (picH * ((double) width / height));
                }
                imgRun.addPicture(new FileInputStream(erImgPath), XWPFDocument.PICTURE_TYPE_PNG, "er.png", Units.toEMU(picW), Units.toEMU(picH));
            } catch (InvalidFormatException | IOException e) {
                throw new RuntimeException(e);
            }
            imgPg.setAlignment(ParagraphAlignment.CENTER);
        }
    }


    /**
     * 生成逻辑结构v
     * @param data
     * @param tableMap
     * @param document
     */
    public void genLogical(Map<String, List<TableColumn>> data, Map<String, String> tableMap, XWPFDocument document) {

        global.info("三、开始生成逻辑结构...");
        Vector<Vector<String>> tav = new Vector<>();
        data.keySet().parallelStream().forEach(a -> {
            List<TableColumn> tableColumns = data.get(a); // 获取表的字段信息
            Vector<String> rowCelVales = new Vector<>();
            String tn = tableMap.get(a);
            if (tn ==null || "".equals(tn)) {
                tn = a;
            }
            rowCelVales.add(tn);
            StringBuffer lest = new StringBuffer();
            tableColumns.forEach(x -> {
                lest.append(",");
                String columnComment = x.getColumnComment();
                if (columnComment ==null || "".equals(columnComment)) {
                    columnComment = x.getColumnName();
                }
                if ("Y".equals(x.getColumnKey())) {
                    columnComment += "-主键";
                }
                lest.append(columnComment);
            });
            rowCelVales.add("(" + lest.substring(1) + ")");
            tav.add(rowCelVales);

        });

        XWPFParagraph pg1 = document.createParagraph();                // 创建标题对象
        pg1.setStyle("2");
        XWPFRun runt1 = pg1.createRun();                                 // 创建文本对象
        runt1.setText("三、逻辑结构");
        runt1.setFontFamily("宋体");
        runt1.setFontSize(16);
        runt1.setBold(true);// 字体加粗

        XWPFTable table = document.createTable(tav.size() + 1, 3);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.addNewTblLayout().setType(STTblLayoutType.FIXED);


        String[] title = new String[]{"编号", "逻辑关系", "关系属性"};
        int[] lattice = new int[]{1, 3, 8};
        Integer celwidw = config.getCelwidw();
        // 表头
        int j = 0;
        for (int i = 0; i < 3; i++) {
            XWPFTableCell cell = table.getRow(j).getCell(i);
            CTTc ctTc = cell.getCTTc();
            CTTcPr ctTcPr = ctTc.addNewTcPr();
            ctTcPr.addNewShd().setFill("cccccc");
            CTTblWidth cellw = ctTcPr.addNewTcW();
            cellw.setType(STTblWidth.DXA);
            cellw.setW(new BigInteger(lattice[i] * celwidw + ""));
            CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
            XWPFParagraph p = cell.getParagraph(ctP);
            XWPFRun crun = p.createRun();
            p.setWordWrapped(true);
            p.setAlignment(ParagraphAlignment.CENTER);
            crun.setText(title[i]);
            crun.setFontFamily("仿宋");
            crun.setFontSize(9);                                                 // 字体大小
            crun.setBold(true);// 字体加粗
        }
        j++;
        for (int i = 0; i < tav.size(); i++) {
            Vector<String> val = tav.get(i);

            for (int i1 = 0; i1 < 3; i1++) {
                XWPFTableCell cell = table.getRow(j).getCell(i1);
                CTTc ctTc = cell.getCTTc();
                CTTcPr ctTcPr = ctTc.addNewTcPr();
                CTTblWidth cellw = ctTcPr.addNewTcW();
                cellw.setType(STTblWidth.DXA);
                cellw.setW(new BigInteger(lattice[i1] * celwidw + ""));
                CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
                XWPFParagraph p = cell.getParagraph(ctP);
                p.setWordWrapped(true);
                XWPFRun crun = p.createRun();
                p.setAlignment(ParagraphAlignment.CENTER);
                crun.setFontFamily("宋体");
                crun.setFontSize(9);
                if (i1 == 0) {
                    crun.setText(i + 1 + "");
                } else {
                    crun.setText(val.get(i1 - 1));
                }
            }
            j++;

        }
    }

    /**
     * 生成物理结构
     * @param data
     * @param tableMap
     * @param document
     */
    public void genPhysical(Map<String, List<TableColumn>> data, Map<String, String> tableMap, XWPFDocument document) {
        global.info("四、开始生成生成物理结构...");
        String[] title = new String[]{"编号", "名称", "数据类型", "长度", "小数位", "允许空值", "主键", "默认值", "说明"};
        int[] lattice = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 4};
        XWPFParagraph pg1 = document.createParagraph();                // 创建标题对象
        pg1.setStyle("2");
        XWPFRun runt1 = pg1.createRun();                                 // 创建文本对象
        runt1.setText("四、物理结构");
        runt1.setFontFamily("宋体");
        runt1.setFontSize(16);
        runt1.setBold(true);// 字体加粗

        Integer i = 1;
        for (String tableName : data.keySet()) {
            XWPFParagraph paragraph = document.createParagraph();                // 创建标题对象
            paragraph.setStyle("2");
            XWPFRun run = paragraph.createRun();                                 // 创建文本对象
//            run.setText((i+"、"+tableName+"    "+tableMap.get(tableName)));      // 标题名称
            run.setText(i + "、" + (tableName + "（" + tableMap.get(tableName) + ")"));
            run.setFontFamily("宋体");
            run.setFontSize(14);                                                 // 字体大小
            run.setBold(true);// 字体加粗

            int j = 0;
            XWPFTable table = document.createTable(data.get(tableName).size() + 1, 9);

            CTTblPr tblPr = table.getCTTbl().getTblPr();
            tblPr.addNewTblLayout().setType(STTblLayoutType.FIXED);
            // 第一行
            table.setCellMargins(10, 10, 10, 10);
            Integer celwidw = config.getCelwidw();
            //标题
            for (int i1 = 0; i1 < 9; i1++) {
                // 字体大小
                XWPFTableCell cell = table.getRow(j).getCell(i1);
                CTTc ctTc = cell.getCTTc();
                CTTcPr ctTcPr = ctTc.addNewTcPr();
                ctTcPr.addNewShd().setFill("cccccc");
                CTTblWidth cellw = ctTcPr.addNewTcW();
                cellw.setType(STTblWidth.DXA);
                cellw.setW(new BigInteger(lattice[i1] * celwidw + ""));
                CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
                XWPFParagraph p = cell.getParagraph(ctP);
                XWPFRun crun = p.createRun();
                p.setAlignment(ParagraphAlignment.CENTER);
                crun.setText(title[i1]);
                crun.setFontFamily("仿宋");
                crun.setFontSize(9);                                                 // 字体大小
                crun.setBold(true);// 字体加粗
            }
            j++;
            for (TableColumn tableColumn : data.get(tableName)) {
                for (int i1 = 0; i1 < 9; i1++) {
                    XWPFTableCell cell = table.getRow(j).getCell(i1);
                    CTTc ctTc = cell.getCTTc();
                    CTTcPr ctTcPr = ctTc.addNewTcPr();
                    CTTblWidth cellw = ctTcPr.addNewTcW();
                    cellw.setType(STTblWidth.DXA);
                    cellw.setW(new BigInteger(lattice[i1] * celwidw + ""));
                    CTP ctP = ctTc.sizeOfPArray() == 0 ? ctTc.addNewP() : ctTc.getPArray(0);
                    XWPFParagraph p = cell.getParagraph(ctP);
                    XWPFRun crun = p.createRun();
                    p.setAlignment(ParagraphAlignment.CENTER);
                    crun.setFontFamily("宋体");
                    crun.setFontSize(9);
                    switch (i1) {
                        case 0:
                            crun.setText(tableColumn.getRownum());
                            break;
                        case 1:
                            crun.setText(tableColumn.getColumnName());
                            break;
                        case 2:
                            crun.setText(tableColumn.getColumnType());
                            break;
                        case 3:
                            crun.setText(tableColumn.getDataLength());
                            break;
                        case 4:
                            crun.setText(tableColumn.getDpoint());
                            break;
                        case 5:
                            crun.setText(tableColumn.getIsNullable());
                            break;
                        case 6:
                            crun.setText(tableColumn.getColumnKey());
                            break;
                        case 7:
                            crun.setText(tableColumn.getDefValue());
                            break;
                        case 8:
                            crun.setText(tableColumn.getColumnComment());
                            break;

                    }
                }
                j++;

            }
            i++;
        }
    }
}
