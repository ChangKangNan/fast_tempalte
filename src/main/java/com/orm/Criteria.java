package com.orm;

import cn.hutool.core.collection.CollUtil;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold criteria query information.
 *
 * @param <T> Entity type.
 * @author ckn
 */
public class Criteria<T> {

    DbTemplate db;
    Mapper<T> mapper;
    Class<T> clazz;
    List<String> select = null;
    String table = null;
    String where = null;
    List<Object> whereParams = null;
    List<String> orderBy = null;
    List<String> leftJoinSql=new ArrayList<>();
    int offset = 0;
    int maxResults = 0;

    Criteria(DbTemplate db) {
        this.db = db;
    }

    String sql() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT ");
        sb.append((select == null ? "*" : String.join(", ", select)));
        sb.append(" FROM ").append(mapper.tableName);
        if(CollUtil.isNotEmpty(leftJoinSql)){
            for (String leftSql : leftJoinSql) {
                sb.append(leftSql);
            }
        }
        if (where != null) {
            sb.append(" WHERE ").append(String.join(" ", where));
        }
        if (orderBy != null) {
            sb.append(" ORDER BY ").append(String.join(", ", orderBy));
        }
        if (offset >= 0 && maxResults > 0) {
            sb.append(" LIMIT ?, ?");
        }
        String s = sb.toString();
        return s;
    }

    Object[] params() {
        List<Object> params = new ArrayList<>();
        if (where != null) {
            for (Object obj : whereParams) {
                if (obj == null) {
                    params.add(null);
                } else {
                    params.add(obj);
                }
            }
        }
        if (offset >= 0 && maxResults > 0) {
            params.add(offset);
            params.add(maxResults);
        }
        return params.toArray();
    }

     List<Object> list(Class<?> clazz) {
        String selectSql = sql();
        Object[] selectParams = params();
        RowMapper rowMapper=new BeanPropertyRowMapper<>(clazz);
        return db.jdbcTemplate.query(selectSql, selectParams,rowMapper);
    }

    List<T> list() {
        String selectSql = sql();
        Object[] selectParams = params();
        return db.jdbcTemplate.query(selectSql, selectParams, mapper.rowMapper);
    }

    T first() {
        this.offset = 0;
        this.maxResults = 1;
        String selectSql = sql();
        Object[] selectParams = params();
        List<T> list = db.jdbcTemplate.query(selectSql, selectParams, mapper.rowMapper);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    T unique() {
        this.offset = 0;
        this.maxResults = 2;
        String selectSql = sql();
        Object[] selectParams = params();
        List<T> list = db.jdbcTemplate.query(selectSql, selectParams, mapper.rowMapper);
        if (list.isEmpty()) {
            throw new RuntimeException("Expected unique row but nothing found.");
        }
        if (list.size() > 1) {
            throw new RuntimeException("Expected unique row but more than 1 rows found.");
        }
        return list.get(0);
    }
}
