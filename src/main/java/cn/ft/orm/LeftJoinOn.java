package cn.ft.orm;

import java.util.List;

/**
 * @author ckn
 * @date 2021/9/28
 */
public class LeftJoinOn<T> extends CriteriaQuery<T> {

    public LeftJoinOn(Criteria<T> criteria, Mapper<?> mapper,String filedFrom,String filedRight) {
        super(criteria);
        this.criteria.leftJoinSql.add(" LEFT JOIN "+mapper.tableName
                + " ON "+criteria.table+"."+filedFrom
                +"="+mapper.tableName+"."+filedRight);
    }

    /**
     * Left join
     * @param entityClass
     * @return
     */
    public LeftJoinOn<T> leftJoin(Class<T> entityClass,String filedFrom,String fieldLeft) {
        return new LeftJoinOn<T>(this.criteria, this.criteria.db.getMapper(entityClass),filedFrom,fieldLeft);
    }


    /**
     * Add where clause.
     *
     * @param clause Clause like "name = ?".
     * @param args   Arguments to match clause.
     * @return CriteriaQuery object.
     */
    public Where<T> where(String clause, Object... args) {
        return new Where<>(this.criteria, clause, args);
    }

    /**
     * Add order by clause.
     *
     * @param orderBy Field of order by.
     * @return CriteriaQuery object.
     */
    public OrderBy<T> orderBy(String orderBy) {
        return new OrderBy<>(this.criteria, orderBy);
    }

    /**
     * Add limit clause.
     *
     * @param maxResults The max results.
     * @return CriteriaQuery object.
     */
    public Limit<T> limit(int maxResults) {
        return limit(0, maxResults);
    }

    /**
     * Add limit clause.
     *
     * @param offset     The offset.
     * @param maxResults The max results.
     * @return CriteriaQuery object.
     */
    public Limit<T> limit(int offset, int maxResults) {
        return new Limit<>(this.criteria, offset, maxResults);
    }

    /**
     * Get all results as list.
     *
     * @return List of object T.
     */
    public <E> List<E> list(Class<E> clazz) {
        return this.criteria.list(clazz);
    }

    /**
     * Get all results as list.
     *
     * @return list.
     */
    public List<T> list() {
        return this.criteria.list();
    }

    /**
     * Get first row of the query, or null if no result found.
     *
     * @return Object T or null.
     */
    public T first() {
        return this.criteria.first();
    }

    /**
     * Get unique result of the query. Exception will throw if no result found or
     * more than 1 results found.
     *
     */
    public T unique() {
        return this.criteria.unique();
    }
}
