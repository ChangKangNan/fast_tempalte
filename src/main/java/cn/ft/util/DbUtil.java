package cn.ft.util;

import cn.ft.bean.ColumnInfo;
import cn.ft.bean.FileConfig;
import cn.ft.bean.TableInfo;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author ckn
 * @date 2021/10/18
 */
public class DbUtil {
    private static DbUtil dbUtil = new DbUtil();

    private DbUtil() {
    }

    public static DbUtil getInstance() {
        return dbUtil;
    }

    /**
     * 获取需要生成代码的表信息
     *
     * @param metaData        参数
     * @param tableNames      表名称
     * @param underline2Camel 是否进行转换
     * @return 表信息
     * @throws SQLException 错误
     */
    private Map<String, TableInfo> beanTableInfoAll = new HashMap<>();

    /**
     * 获取所有表属性信息
     *
     * @param fileConfig       配置
     * @param metaData   数据库配置
     * @param tableNames 生成的表信息
     * @return 获取到的表属性映射
     * @throws Exception 异常
     */
    public List<TableInfo> getAllTables(FileConfig fileConfig, DatabaseMetaData metaData, Set<String> tableNames)
            throws Exception {
        String catalog = metaData.getConnection().getCatalog();
        boolean isIgnore=true;
        ResultSet tables = metaData.getTables(catalog, null, null, new String[]{"TABLE"});
        List<TableInfo> tableInfoList = new ArrayList<>();
        while (tables.next()){
            String table_name = tables.getString("TABLE_NAME");
            String tableDesc = tables.getString("REMARKS");
            Iterator<String> iterator = tableNames.iterator();
            while (iterator.hasNext()){
                String tableName = iterator.next();
                if(table_name.equals(tableName)){
                    TableInfo tableInfo=new TableInfo();
                    String beanName="";
                    if(isIgnore){
                        int indexOf = table_name.indexOf("_")+1;
                        beanName = table_name.substring(indexOf);
                    }else {
                        beanName=table_name;
                    }
                    tableInfo.setBeanName(StrUtil.toCamelCase(beanName));
                    tableInfo.setTableName(tableName);
                    tableInfo.setTableDesc(tableDesc);
                    String pojoName=StrUtil.toCamelCase("_"+beanName+"_pojo");
                    tableInfo.setPojoName(pojoName);
                    setAllColumns(tableInfo,metaData);
                    setPackPath(fileConfig,tableInfo);
                    tableInfoList.add(tableInfo);
                }
            }
        }
        return tableInfoList;
    }
    public  void setAllColumns(TableInfo tableInfo,DatabaseMetaData metaData) throws SQLException {
        String catalog = metaData.getConnection().getCatalog();
        String keyColumnName = primaryKeyColumnName(metaData, tableInfo.getTableName());
        ResultSet columns = metaData.getColumns(catalog, null, tableInfo.getTableName(), "%");
        String columnName;
        String columnType;
        String remarks;
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        Set<String> importPackages=new HashSet<>();
        while (columns.next()){
            columnName = columns.getString("COLUMN_NAME");
            columnType = columns.getString("TYPE_NAME");
            remarks = columns.getString("remarks");
            ColumnInfo info = new ColumnInfo();
            info.setColumnName(columnName);
            info.setColumnType(columnType);
            info.setColumnRemarks(remarks);
            String propertyName = StrUtil.toCamelCase(columnName);
            String fieldType = getFieldType(columnType, importPackages);
            info.setPropertyName(propertyName);
            info.setPropertyType(fieldType);
            info.setKey(columnName.equals(keyColumnName)?true:false);
            columnInfoList.add(info);
        }
        tableInfo.setColumns(columnInfoList);
        tableInfo.setPackages(importPackages);
    }


    /**
     * 只做单主键代码的生成
     *
     * @param metaData  参数
     * @param tableName 表名称
     * @return 主键
     * @throws SQLException 错误
     */
    public String primaryKeyColumnName(DatabaseMetaData metaData, String tableName) throws SQLException {
        String primaryKeyColumnName = null;
        String catalog = metaData.getConnection().getCatalog();
        ResultSet primaryKeyResultSet = metaData.getPrimaryKeys(catalog, null, tableName);
        while (primaryKeyResultSet.next()) {
            primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
            break;
        }
        if (primaryKeyColumnName == null) {
            primaryKeyColumnName = "id";
        }
        return primaryKeyColumnName;
    }

    /**
     * 文件路径设置
     *
     * @param conf      配置信息
     * @param tableInfo 表信息
     */
    public void setPackPath(FileConfig conf, TableInfo tableInfo) {
        String separator = File.separator;
        String resourcesBasePath = new StringBuilder().append(System.getProperty("user.dir")).append(separator).append("src").append(separator).append("main").append(separator).append("resources").append(separator).toString();
        String javaBasePath = new StringBuilder().append(System.getProperty("user.dir")).append(separator).append(conf.getChildModuleName()).append(separator).append("src").append(separator).append("main").append(separator).append("java").append(separator).toString();
        String pojoPackagePath = conf.getBasePackage() + "." + "pojo";
        tableInfo.setPojoPackagePath(pojoPackagePath);
        tableInfo.setPojoFilePath(javaBasePath + tableInfo.getPojoPackagePath().replace(".", separator) + separator + tableInfo.getPojoName() + ".java");
    }

    /**
     * 返回一个与特定数据库的连接
     *
     * @param fileConfig 配置
     * @return 数据库连接
     * @throws ClassNotFoundException 错误
     */
    public Connection getConnection(FileConfig fileConfig) throws ClassNotFoundException {
        Connection connection = null;
        try {
            String driverClass = fileConfig.getDriverClass();
            String url = fileConfig.getUrl();
            String user = fileConfig.getUser();
            String password = fileConfig.getPassword();
            Class.forName(driverClass);
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 设置字段类型 MySql数据类型
     *
     * @param columnType 列类型字符串
     * @param packages   封装包信息
     * @return 数据类型
     */
    public static String getFieldType(String columnType, Set<String> packages) {
        columnType = columnType.toLowerCase();
        if ("varchar".equals(columnType) || "nvarchar".equals(columnType) || columnType.equals("char")
                || columnType.equals("text") || columnType.equals("mediumtext"))
        {
            return "String";
        } else if (columnType.equals("tinyblob") || columnType.equals("blob") || columnType.equals("mediumblob")
                || columnType.equals("longblob")) {
            return "byte[]";
        } else if (columnType.equals("datetime") || columnType.equals("date") || columnType.equals("timestamp")
                || columnType.equals("time") || columnType.equals("year")) {
            packages.add("import java.util.Date;");
            return "Date";
        } else if (columnType.equals("bit")) {
            return "Boolean";
        } else if (columnType.equals("bit") || columnType.equals("int") || columnType.equals("tinyint")
                || columnType.equals("smallint")) {
            return "Integer";
        } else if (columnType.equals("int unsigned")) {
            return "Integer";
        } else if (columnType.equals("bigint unsigned") || columnType.equals("bigint")) {
            return "Long";
        } else if (columnType.equals("float")) {
            return "Float";
        } else if (columnType.equals("double")) {
            return "Double";
        } else if (columnType.equals("decimal")) {
            packages.add("import java.math.BigDecimal;");
            return "BigDecimal";
        }
        return "ErrorType";
    }

    /**
     * 判断字段是否是id等信息
     *
     * @param propertyName 属性名
     * @return 是否包含
     */
    public boolean excludeInsertProperties(String propertyName) {
        return "id".equals(propertyName) || "createTime".equals(propertyName) || "updateTime".equals(propertyName)
                || "delFlag".equals(propertyName);
    }
    /**
     * 设置类标题注释
     *
     * @param packages  参数
     * @param className 类名
     */
    public static void getTitle(StringBuilder packages, String className) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        packages.append("\r\n/**\r\n");
        packages.append("*\r\n");
        packages.append("* 标题: " + className + "<br/>\r\n");
        packages.append("* 说明: <br/>\r\n");
        packages.append("*\r\n");
        packages.append("* 作成信息: DATE: " + format.format(new Date()) + " NAME: author\r\n");
        packages.append("*\r\n");
        packages.append("* 修改信息<br/>\r\n");
        packages.append("* 修改日期 修改者 修改ID 修改内容<br/>\r\n");
        packages.append("*\r\n");
        packages.append("*/\r\n");
    }

}
