package cn.ft.orm;

import cn.ft.pojo.User;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ckn
 * @date 2021/11/22
 */
public class BasePojo<T> {
    JdbcTemplate jdbcTemplate;
    DbTemplate dbTemplate;

    protected String tableName;
    protected String className;
    protected String id;
    protected HashMap<String, List> conditions;
    protected List<AccessibleProperty> accessibleProperties;
    protected RowMapper<T> rowMapper;

    /**
     * 初始化
     */
    public  void init(){
        dbTemplate = SpringUtil.getBean(DbTemplate.class);
        Map<String, Mapper<?>> classMappingPojo= dbTemplate.classMappingPojo;
        Mapper<T> mapper = (Mapper<T>) classMappingPojo.get(className);
        accessibleProperties= mapper.properties;
        id=mapper.id.columnName;
        rowMapper= mapper.rowMapper;
        jdbcTemplate = SpringUtil.getBean("jdbcTemplate");
    }

    public List<T> findAll() {
         init();
         String selectSql = sql();
        if(CollUtil.isNotEmpty(conditions)){
            selectSql+=" WHERE ";
            Set<String> set = conditions.keySet();
            for (String key : set) {
                List list = conditions.get(key);
                if(list.size()==1){
                    Object o = list.get(0);
                    if(o instanceof  String){
                        selectSql+=key+"= '"+list.get(0)+"'";
                    }else {
                        selectSql+=key+"= "+list.get(0);
                    }
                }else {
                    selectSql+=key+" in ("+ String.join(", ",(String[])list.stream().map(p -> {
                        if (p instanceof String) {
                            return "'" + p + "'";
                        } else {
                            return "" + p;
                        }
                    }).toArray(String[]::new))+")";
                }
            }
        }
        System.out.println(selectSql);
        System.out.println(jdbcTemplate==null);
        List<T> list = jdbcTemplate.query(selectSql, new Object[]{},rowMapper);
        return list;
    }

    public List<T> findOne() {
        init();
        String selectSql = sql();
        if(CollUtil.isNotEmpty(conditions)){

        }
        return null;
    }
    public List<T> limit(Integer index,Integer count) {
        init();
        String selectSql = sql();
        if(CollUtil.isNotEmpty(conditions)){

        }
        return null;
    }

    String sql() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT ");
        sb.append("*");
        sb.append(" FROM ").append(tableName);
        String s = sb.toString();
        return s;
    }
}
