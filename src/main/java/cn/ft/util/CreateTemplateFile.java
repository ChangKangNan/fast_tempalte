package cn.ft.util;

import cn.ft.bean.FileConfig;
import cn.ft.bean.TableInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ckn
 * @date 2021/10/18
 */
public class CreateTemplateFile {
    private static final String TEMPLATE_PATH = "src/main/resources/template";

    public static void createPOJOTemplate(FileConfig config) {
        try {
            Set<String> createTables = config.getCreateTables();
            Connection connection = DbUtil.getInstance().getConnection(config);
            List<TableInfo> tableInfos = DbUtil.getInstance().getAllTables(config, connection.getMetaData(), createTables);
            if (CollUtil.isNotEmpty(tableInfos)) {
                for (TableInfo info : tableInfos) {
                    createTemplate(info, config.getReplaceFile());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTemplate(TableInfo info, boolean replaceFile) {
        //开始生成文件
        // step1 创建freeMarker配置实例
        Configuration configuration = new Configuration(Configuration.getVersion());
        Writer out = null;
        try {
            // step2 获取模版路径
            configuration.setDirectoryForTemplateLoading(new File(TEMPLATE_PATH));
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            // step3 创建数据模型
            Map<String, Object> dataMap = new HashMap<String, Object>();
            info.setPojoName(info.getPojoName());
            dataMap.put("table", info);
            // step4 加载模版文件
            Template template = configuration.getTemplate("Pojo.ftl");
            // step5 生成数据
            String pojoFilePath = info.getPojoFilePath();
            File file = new File(pojoFilePath);
            if (!file.exists()) {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                // step6 输出文件
                template.process(dataMap, out);
            } else {
                if (replaceFile) {
                    file.delete();
                    file.createNewFile();
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                    // step6 输出文件
                    template.process(dataMap, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.flush();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
