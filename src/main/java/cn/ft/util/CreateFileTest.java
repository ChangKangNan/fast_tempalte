package cn.ft.util;

import cn.ft.bean.FileConfig;

/**
 * @author ckn
 * @date 2021/10/18
 */
public class CreateFileTest {
    public static void main(String[] args) throws Exception{
        FileConfig config = new FileConfig();
        //数据库连接
        config.setDBInfo("jdbc:mysql://kaifa.mysql.guo-kai.com:3306/gk-ims?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useInformationSchema=true","gkims-kaifa","PGrsByizeD357ajR","com.mysql.jdbc.Driver");
        //是否生成表前缀
        config.setIgnorePrefix(true);
        //是否覆盖原文件,默认false
        config.setReplaceFile(true);
        //文件生成的包路径
        config.setBasePackage("cn.ft");
        //项目多模块空间
        config.setChildModuleName("");
        config.setCreateTables("test_user");
        CreateTemplateFile.createPOJOTemplate(config);
    }
}
