package cn.ft.util;

import cn.ft.bean.FileConfig;
import cn.ft.bean.TableInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.TemplateException;
import cn.hutool.json.JSONUtil;
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
    private static final String TEMPLATE_PATH = "src/main/resources/template";
    private static final String CLASS_PATH = "src/main/java/cn/ft/pojo";


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


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        FileConfig config = new FileConfig();
        //数据库连接
        config.setDBInfo("jdbc:mysql://kaifa.mysql.guo-kai.com:3306/gk-ims?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useInformationSchema=true","gkims-kaifa","PGrsByizeD357ajR","com.mysql.jdbc.Driver");
        //选择生成的文件
        config.setNeedModules(FileConfig.CodeCreateModule.Base);
        //是否生成表前缀
        config.setPrefix(true,false,null);
        //是否使用lombok插件
        config.setUseLombok(true);
        //是否下划线转大小写,默认true
        config.setUnderline2CamelStr(true);
        config.setUseDTOSwagger2(true);
        //是否覆盖原文件,默认false
        config.setReplaceFile(true);
        //文件生成的包路径
        config.setBasePackage("com.gk.ims.funding");
        //项目多模块空间
        config.setChildModuleName("gk-ims-funding-v2-service");
        config.setCreateTables("test_user");
        createPOJOTemplate(config);

    }

    public static  void createPOJOTemplate(FileConfig config){
        try {
            //表信息
            List<TableInfo> tableInfos = getTableInfos(config);
            //生成文件
            Set<String> createTables = config.getCreateTables();
            for (TableInfo info : tableInfos) {
                String tableName = info.getTableName();
                for (String table : createTables) {
                    if(tableName.equals(table)){
                        System.out.println(JSONUtil.toJsonStr(info));
                        //开始生成文件
                        // step1 创建freeMarker配置实例
                        Configuration configuration = new Configuration(Configuration.getVersion());
                        Writer out = null;
                        try {
                            // step2 获取模版路径
                            configuration.setDirectoryForTemplateLoading(new File(TEMPLATE_PATH));
                            // step3 创建数据模型
                            Map<String, Object> dataMap = new HashMap<String, Object>();
                            info.setPojoName(info.getPojoName()+"Pojo");
                            dataMap.put("table", info);
                            dataMap.put("package", "cn.ft.pojo");
                            Map<String, String> primaryKey = info.getPrimaryKey();
                            String key="";
                            for(String k:primaryKey.keySet()){
                                key=k;
                            }
                            info.setKey(key);
                            String infoTableName =StrUtil.toCamelCase(info.getTableName()) ;
                            // step4 加载模版文件
                            Template template = configuration.getTemplate("Pojo.ftl");
                            // step5 生成数据
                            File docFile = new File(CLASS_PATH + "\\" + info.getPojoName()+".java");
                            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
                            // step6 输出文件
                            template.process(dataMap, out);
                            System.out.println(infoTableName+"Pojo.java"+"文件创建成功 !");
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
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param conf       配置
     * @param tableInfos 表信息
     * @throws IOException       错误
     * @throws TemplateException 错误
     */
    private static void createFile(FileConfig conf, List<TableInfo> tableInfos) throws IOException, TemplateException, freemarker.template.TemplateException {
        Configuration cfg = new Configuration(Configuration.getVersion());
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