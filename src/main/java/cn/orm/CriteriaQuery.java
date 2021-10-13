package cn.orm;

/**
 * Base criteria query.
 * 
 * @author ckn
 * 
 * @param <T> Generic type.
 */
abstract class CriteriaQuery<T> {

	protected final Criteria<T> criteria;

	CriteriaQuery(Criteria<T> criteria) {
		this.criteria = criteria;
	}

	String sql() {
		return criteria.sql();
	}

}
