package org.example.dbw;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.dbw.ot.EtService;
import org.example.dbw.po.ConfigPo;
import org.example.dbw.po.ForeignKey;
import org.example.dbw.po.Table;
import org.example.dbw.po.TableColumn;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ExportToWord {

    public static void genDbDesigDoc(ConfigPo conf){
        Logger global = Logger.getGlobal();
        global.info("开始连接数据库！");
        EtService etService = new EtService(conf);
        // 创建文件夹
        new File(conf.getReportPath() + "er").mkdirs();
        try {
            global.info("开始连获取元数据...");
            Map<String, List<TableColumn>> data = etService.getDbTbcData(conf.getDatabase());// 表名：表体
            List<Table> tables = etService.getTables(conf.getDatabase());         // 表体(列名、类型、注释)
            Map<String, String> tableMap = new HashMap<>();              // 表名:中文名
            List<ForeignKey> tbForeignKey = etService.getTbForeignKey(conf.getDatabase(),"");
            for (Table table : tables) {
                tableMap.put(table.getTableName(), table.getTableCommont());
            }
            // 构建文档
            XWPFDocument document = new XWPFDocument();
            // 导出标识符
            etService.genFlag(data, tableMap, document);
            // 导出er图
            etService.genErModle(data, tableMap, document,tbForeignKey);
            // 导出 逻辑结构设计
            etService.genLogical(data, tableMap, document);
            // 导出 物理结构设计
            etService.genPhysical(data, tableMap, document);
            // 文档输出
            FileOutputStream out = new FileOutputStream(conf.getReportPath() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()).toString() + "_" + conf.getDatabase() + ".docx");
            document.write(out);
            out.close();
            global.info("生成完毕，文件地址:"+conf.getReportPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }
}
