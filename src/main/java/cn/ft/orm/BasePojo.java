package cn.ft.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
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
    protected Map<String, List> conditions;
    protected List<AccessibleProperty> accessibleProperties;
    protected RowMapper<T> rowMapper;

    /**
     * 初始化
     */
    protected void init() {
        dbTemplate = SpringUtil.getBean(DbTemplate.class);
        jdbcTemplate = SpringUtil.getBean("jdbcTemplate");
        conditions = new HashMap<>(168);
        Map<String, Mapper<?>> classMappingPojo = dbTemplate.classMappingPojo;
        Mapper<T> mapper = (Mapper<T>) classMappingPojo.get(className);
        accessibleProperties = mapper.properties;
        id = mapper.id.columnName;
        rowMapper = mapper.rowMapper;
    }

    public String packageConditionSql() {
        String selectSql = sql();
        if (CollUtil.isNotEmpty(conditions)) {
            selectSql += " WHERE 1";
            Set<String> set = conditions.keySet();
            for (String key : set) {
                List list = conditions.get(key);
                selectSql += " AND ";
                if (list.size() == 1) {
                    Object o = list.get(0);
                    if (o instanceof String) {
                        selectSql += key + "= '" + list.get(0) + "'";
                    } else {
                        selectSql += key + "= " + list.get(0);
                    }
                } else {
                    selectSql += key +
                            " in (" +
                            String.join(", ", (String[]) list.stream().map(p -> {
                                if (p instanceof String) {
                                    return "'" + p + "'";
                                } else {
                                    return "" + p;
                                }
                            })
                                    .toArray(String[]::new))
                            + ")";
                }
            }
        }
        return selectSql;
    }

    public List<T> findAll() {
        String selectSql = packageConditionSql();
        List<T> list = jdbcTemplate.query(selectSql, new Object[]{}, rowMapper);
        return list;
    }

    public List<T> findOne() {
        String selectSql = packageConditionSql();
        selectSql += " limit 0,1";
        List<T> list = jdbcTemplate.query(selectSql, new Object[]{}, rowMapper);
        return list;
    }

    public List<T> limit(Integer pageIndex, Integer pageSize) {
        String selectSql = packageConditionSql();
        selectSql += " limit " + (pageIndex - 1) * pageSize + "," + pageSize;
        List<T> list = jdbcTemplate.query(selectSql, new Object[]{}, rowMapper);
        return list;
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
