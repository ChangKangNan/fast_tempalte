package cn.ft.util;

import cn.ft.bean.FileConfig;
import cn.ft.bean.TableInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.template.TemplateException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kangnan.chang
 */
public class TableFileCreateUtils {

    /**
     * 生成文件
     * @param codeParameter 重要:请参考参数设置
     */
    public static void create(FileConfig codeParameter){
        try {
            //表信息
            List<TableInfo> tableInfos = getTableInfos(codeParameter);
            //生成文件
            createFile(codeParameter, tableInfos);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 需要生成代码的表
     *
     * @param conf 配置
     * @return 表信息
     * @throws ClassNotFoundException 错误
     * @throws SQLException           错误
     */
    private static List<TableInfo> getTableInfos(FileConfig conf) throws ClassNotFoundException, SQLException {
        boolean underline2Camel = conf.getUnderline2CamelStr();
        Connection connection = DbUtils.getInstance().getConnection(conf);
        DatabaseMetaData metaData = DbUtils.getInstance().getMetaData(connection);

        Set<String> tableNames = conf.getCreateTables();
        if (CollUtil.isEmpty(tableNames)) {
            tableNames = CollUtil.newHashSet("all");
        }
        return DbUtils.getInstance().getAllTables(conf, metaData, tableNames);
    }

    /**
     * @param conf       配置
     * @param tableInfos 表信息
     * @throws IOException       错误
     * @throws TemplateException 错误
     */
    private static void createFile(FileConfig conf, List<TableInfo> tableInfos) throws IOException, TemplateException, freemarker.template.TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setClassLoaderForTemplateLoading(TableFileCreateUtils.class.getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        Set<String> modules = conf.getNeedModules();
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("conf", conf);
        for (TableInfo tableInfo : tableInfos) {
            root.put("table", tableInfo);
            for (String module : modules) {
                if (FileConfig.CodeCreateModule.Pojo.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("Pojo.ftl");
                    createFile(conf, tableInfo.getPojoFilePath(), root, temp);
                } else if (FileConfig.CodeCreateModule.FastPojo.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("FastPojo.ftl");
                    createFile(conf, tableInfo.getFastPojoFilePath(), root, temp);
                }else if (FileConfig.CodeCreateModule.PojoFastDao.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("PojoFastDao.ftl");
                    createFile(conf, tableInfo.getPojoFastDaoFilePath(), root, temp);
                } else if (FileConfig.CodeCreateModule.Service.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("IService.ftl");
                    createFile(conf, tableInfo.getIserviceFilePath(), root, temp);
                } else if (FileConfig.CodeCreateModule.Dto.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("Dto.ftl");
                    createFile(conf, tableInfo.getDtoFilePath(), root, temp);
                }  else if (FileConfig.CodeCreateModule.Dao.codeModule.equals(module)) {
                    Template temp = cfg.getTemplate("Dao.ftl");
                    createFile(conf, tableInfo.getDaoFilePath(), root, temp);
                }
            }
        }
    }

    /**
     * @param filePath 文件路径
     * @param root     data
     * @param temp     模板
     * @throws IOException       文件创建是吧
     * @throws TemplateException 模板读取失败
     */
    private static void createFile(FileConfig conf, String filePath, Map<String, Object> root, Template temp) throws IOException, TemplateException, freemarker.template.TemplateException {
        String separator = File.separator;
        boolean replaceFile = conf.getReplaceFile();
        String fileName = filePath.substring(filePath.lastIndexOf(separator) + 1);
        String subPath;
        subPath = filePath.substring(0, filePath.lastIndexOf(separator));
        File directory = new File(subPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(filePath);
        boolean needCreatFile = false;
        if (!file.exists()) {
            file.createNewFile();
            needCreatFile = true;
        } else {
            if (replaceFile) {
                file.delete();
                file.createNewFile();
                needCreatFile = true;
            }
        }
        if (needCreatFile) {
            OutputStream os = new FileOutputStream(file);
            Writer out = new OutputStreamWriter(os);
            temp.process(root, out);
        }
    }

}