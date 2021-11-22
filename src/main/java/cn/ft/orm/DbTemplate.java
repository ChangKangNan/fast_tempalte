package cn.ft.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.ft.annotation.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * A simple ORM wrapper for JdbcTemplate.
 *
 * @author ckn
 */
public class DbTemplate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final JdbcTemplate jdbcTemplate;

    /**
     * class -> Mapper:
     */
    private Map<Class<?>, Mapper<?>> classMapping;

    public DbTemplate(JdbcTemplate jdbcTemplate, String basePackage) {
        this.jdbcTemplate = jdbcTemplate;
        List<Class<?>> classes = scanEntities(basePackage);
        Map<Class<?>, Mapper<?>> classMapping = new HashMap<>();
        try {
            for (Class<?> clazz : classes) {
                logger.info("Found class: " + clazz.getName());
                Mapper<?> mapper = new Mapper<>(clazz);
                classMapping.put(clazz, mapper);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.classMapping = classMapping;
    }

    /**
     * Get a model instance by class type and id. EntityNotFoundException is thrown
     * if not found.
     *
     * @param <T>   Generic type.
     * @param clazz Entity class.
     * @param id    Id value.
     * @return Entity bean found by id.
     */
    public <T> T get(Class<T> clazz, Object id) {
        T t = fetch(clazz, id);
        if (t == null) {
            logger.info("未获取到数据!");
        }
        return t;
    }

    /**
     * Get a model instance by class type and id. Return null if not found.
     *
     * @param <T>   Generic type.
     * @param clazz Entity class.
     * @param id    Id value.
     * @return Entity bean found by id.
     */
    public <T> T fetch(Class<T> clazz, Object id) {
        Mapper<T> mapper = getMapper(clazz);
        String selectSQL = "SELECT * FROM " + mapper.tableName + " WHERE " + mapper.id.columnName + " = ?";
        logger.info("SQL: " + selectSQL);
        System.out.println("SQL: " + selectSQL);
        List<T> list = (List<T>) jdbcTemplate.query(selectSQL, new Object[]{id}, mapper.rowMapper);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Remove bean by id.
     *
     * @param bean The com.entity.
     */
    public <T> void delete(T bean) {
        try {
            Mapper<?> mapper = getMapper(bean.getClass());
            delete(bean.getClass(), mapper.getIdValue(bean));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove bean by id.
     */
    public <T> void delete(Class<T> clazz, Object id) {
        Mapper<?> mapper = getMapper(clazz);
        String deleteSQL = "DELETE FROM " + mapper.tableName + " WHERE " + mapper.id.columnName + " = ?";
        jdbcTemplate.update(deleteSQL, id);
    }

    @SuppressWarnings("rawtypes")
    public Select select(String... selectFields) {
        return new Select(new Criteria(this), selectFields);
    }

    public <T> From<T> from(Class<T> entityClass) {
        Mapper<T> mapper = getMapper(entityClass);
        return new From<>(new Criteria<>(this), mapper);
    }

    public  String fetchUpdateSql(Mapper<?> mapper){
        String updateSQL = "UPDATE " + mapper.tableName + " SET "
                + String.join(", ",
                mapper.properties.stream().filter(AccessibleProperty::isNotId).map(p -> p.columnName + " = ?").toArray(String[]::new))
                + " WHERE " + mapper.id.columnName + " = ?";
        return updateSQL;
    }

    public  String fetchInsertSql(Mapper<?> mapper){
        String insertSQL = "INSERT INTO " + mapper.tableName + " ("
                + String.join(", ",  mapper.properties.stream().map(p -> p.columnName).toArray(String[]::new))
                + ") VALUES (" + numOfQuestions(mapper.properties.size()) + ")";
        return insertSQL;
    }
    /**
     * ?包装返回值
     * @param n
     * @return
     */
    private String numOfQuestions(int n) {
        String[] qs = new String[n];
        return String.join(", ", Arrays.stream(qs).map((s) -> {
            return "?";
        }).toArray(String[]::new));
    }
    /**
     * Update com.entity's updatable properties by id.
     *
     * @param <T>  Generic type.
     * @param bean Entity object.
     */
    public <T> void update(T bean) {
        try {
            final Mapper<?> mapper = getMapper(bean.getClass());
            String updateSQL=fetchUpdateSql(mapper);
            int n = 0;
            int dCount = 0;
            Object[] args = new Object[mapper.properties.size()];
            for (AccessibleProperty prop : mapper.properties) {
                boolean notId = prop.isNotId();
                if (notId) {
                    Object invoke = prop.getter.invoke(bean);
                    if (ObjectUtil.isNotEmpty(invoke)) {
                        args[n] = invoke;
                        n++;
                    } else {
                        dCount++;
                        String replace = prop.columnName + " = ?,";
                        boolean contains = StrUtil.contains(updateSQL, replace);
                        String replacement = "";
                        if (contains) {
                            replacement = replace;
                        } else {
                            replacement = ", " + prop.columnName + " = ?";
                        }
                        String format = StrUtil.replace(updateSQL, replacement, "");
                        updateSQL = format;
                    }
                }
            }
            Object invoke = mapper.id.getter.invoke(bean);
            if (ObjectUtil.isEmpty(invoke)) {
                throw new RuntimeException("未传入主键!");
            }
            args[n] = invoke;
            Object[] argsFinal = new Object[mapper.properties.size() - dCount];
            for (int i = 0; i < mapper.properties.size() - dCount; i++) {
                argsFinal[i] = args[i];
            }
            if (args.length == 1) {
                throw new RuntimeException("无更新属性!");
            }
            jdbcTemplate.update(updateSQL, argsFinal);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update com.entity's updatable properties by id.
     *
     * @param <T>  Generic type.
     * @param bean Entity object.
     */
    public <T> void updateOverwrite(T bean) {
        try {
            Mapper<?> mapper = getMapper(bean.getClass());
            String updateSQL=fetchUpdateSql(mapper);
            Object[] args = new Object[mapper.properties.size()];
            int n = 0;
            for (AccessibleProperty prop : mapper.properties) {
                boolean notId = prop.isNotId();
                if (notId) {
                    args[n] = prop.getter.invoke(bean);
                    n++;
                }
            }
            args[n] = mapper.id.getter.invoke(bean);
            jdbcTemplate.update(updateSQL, args);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * sql 模板sql语句  forExample(insert into user (name,deptid) values (?,?))
     * batchArgs 参数列 forExample(new Object[]{"aaa",8})
     *
     * @param sql
     * @param batchArgs
     */
    public void batchInsertOrUpdate(String sql, List<Object[]> batchArgs) {
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 通过 sql文件运行获得返回结果
     * @param sqlPath
     * @param parameters
     * @param rowMapperClass
     * @return
     */
    public <E> List<E> selectByFile(String sqlPath,Map<String,Object> parameters, Class<E> rowMapperClass) {
       //处理参数
        Map<String,Object> parameterMap=new HashMap<>();
        if(CollUtil.isNotEmpty(parameters)){
            parameters.forEach((k,v)->{
                if(v instanceof String){
                    parameterMap.put(k,"'"+v+"'");
                }else {
                    parameterMap.put(k,v);
                }
            });
        }
        ClassPathResource resource = new ClassPathResource(sqlPath);
        String sql = IoUtil.read(resource.getStream()).toString();
        if(CollUtil.isNotEmpty(parameters)){
            for(String key:parameterMap.keySet()){
                Object o = parameterMap.get(key);
                sql= sql.replace("#{"+key+"}",o+"");
            }
        }

        RowMapper<E>  rowMapper=new BeanPropertyRowMapper<>(rowMapperClass);
        List<E>  query = jdbcTemplate.query(sql, new Object[]{}, rowMapper);
        return query;
    }

    public <T> void insert(T bean) {
        try {
            int rows;
            final Mapper<?> mapper = getMapper(bean.getClass());
            String insertSQL=fetchInsertSql(mapper);
            Object[] args = new Object[mapper.properties.size()];
            int n = 0;
            int dCount = 0;
            for (AccessibleProperty prop : mapper.properties) {
                Object invoke = prop.getter.invoke(bean);
                if (ObjectUtil.isNotEmpty(invoke)) {
                    args[n] = invoke;
                    n++;
                } else {
                    dCount++;
                    String format = "";
                    boolean contains = StrUtil.contains(insertSQL, ", " + prop.columnName);
                    String replace = "";
                    String replaceUn = "(?,";
                    if (contains) {
                        replace = ", " + prop.columnName;
                    } else {
                        replace = "(" + prop.columnName;
                    }
                    format = StrUtil.replace(insertSQL, replace, "");
                    format = StrUtil.replace(format, replaceUn, "(");
                    insertSQL = format;
                }
            }
            final int finalCount = dCount;
            if (mapper.id.isId()) {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                String finalInsertSQL = insertSQL;
                rows = jdbcTemplate.update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(finalInsertSQL,
                                Statement.RETURN_GENERATED_KEYS);
                        for (int i = 0; i < args.length - finalCount; i++) {
                            ps.setObject(i + 1, args[i]);
                        }
                        return ps;
                    }
                }, keyHolder);
                if (rows == 1) {
                    mapper.id.setter.invoke(bean, keyHolder.getKey());
                }
            } else {
                rows = jdbcTemplate.update(insertSQL, args);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    <T> Mapper<T> getMapper(Class<T> clazz) {
        Mapper<T> mapper = (Mapper<T>) this.classMapping.get(clazz);
        if (mapper == null) {
            throw new RuntimeException("Target class is not a registered com.entity: " + clazz.getName());
        }
        return mapper;
    }

    private static List<Class<?>> scanEntities(String basePackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        List<Class<?>> classes = new ArrayList<>();
        Set<BeanDefinition> beans = provider.findCandidateComponents(basePackage);
        for (BeanDefinition bean : beans) {
            try {
                classes.add(Class.forName(bean.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return classes;
    }
}
