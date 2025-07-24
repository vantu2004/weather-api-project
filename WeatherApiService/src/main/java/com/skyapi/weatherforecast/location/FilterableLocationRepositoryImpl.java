package com.skyapi.weatherforecast.location;

// Import a new class
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.skyapi.weatherforecast.common.Location;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class FilterableLocationRepositoryImpl implements FilterableLocationRepository {

	@Autowired
	private EntityManager entityManager;

	// hàm này phân trang kết quả theo điều kiện nên phải làm thủ công
	@Override
	public Page<Location> listWithFilter(Pageable pageable, Map<String, Object> filterFields) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Location> criteriaQuery = criteriaBuilder.createQuery(Location.class);

		Root<Location> root = criteriaQuery.from(Location.class);

		List<Predicate> predicates = this.createPredicates(filterFields, criteriaBuilder, root);
		if (!predicates.isEmpty()) {
			/*
			 * new Predicate[0] tự động tạo mảng tương ứng và ko cần chỉ định số phần tử
			 * mảng
			 */
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
		}

		/*
		 * Order thuộc criteria
		 * 
		 * orders để chứa các tiêu chí sắp xếp để criteriaQuery tạo truy vấn, mỗi phần
		 * tử trong orders là một trường cần sắp xếp theo thứ tự cụ thể.
		 */
		List<Order> orders = new ArrayList<>();

		/*
		 * Order là nested class của Sort đại diện tiêu chí sort của 1 field truyền vào,
		 * Order gồm 2 field là property (tên thuộc tính cần sort) và direction (chiều
		 * cần sort)
		 * 
		 * pageable.getSort() trả Sort, trong sort lại có tập orders
		 */
		for (Sort.Order sortOrder : pageable.getSort()) {

			// Lấy đường dẫn tới thuộc tính cần sắp xếp
			Path<Object> path = root.get(sortOrder.getProperty());

			// asc()/desc() trả về Order đại diện cho 1 tiêu chí sắp xếp
			Order order = sortOrder.isAscending() ? criteriaBuilder.asc(path) : criteriaBuilder.desc(path);
			orders.add(order);
		}

		// Nếu có sắp xếp thì áp dụng vào truy vấn Criteria
		if (!orders.isEmpty()) {
			criteriaQuery.orderBy(orders);
		}

		// phân trang kết quả trả về từ criteriaQuery
		TypedQuery<Location> typedQuery = entityManager.createQuery(criteriaQuery);
		/*
		 * pageable.getOffset() trả về vị trí dòng đầu mỗi trang (vd: trang 1 -> offset
		 * 0, 2 -> 10, 3 -> 20)
		 */
		typedQuery.setFirstResult((int) pageable.getOffset());
		// giới hạn kết quả theo pageSize
		typedQuery.setMaxResults(pageable.getPageSize());

		List<Location> locations = typedQuery.getResultList();

		/*
		 * totalRows <=> totalElements, phục vụ cho hàm getTotalElements(),
		 * getTotalPages(),... trong PageImpl
		 */
		long totalRows = this.getTotalRows(filterFields);

		return new PageImpl<Location>(locations, pageable, totalRows);
	}

	private List<Predicate> createPredicates(Map<String, Object> filterFields, CriteriaBuilder criteriaBuilder,
			Root<Location> root) {
		List<Predicate> predicates = new ArrayList<>();

		// Duyệt từng cặp (fieldName, value) trong filterFields
		for (Map.Entry<String, Object> entry : filterFields.entrySet()) {
			String fieldName = entry.getKey();
			Object fieldValue = entry.getValue();

			if (fieldValue != null) {
				predicates.add(criteriaBuilder.equal(root.get(fieldName), fieldValue));
			}
		}

		// Only show non-trashed locations
		predicates.add(criteriaBuilder.isFalse(root.get("trashed")));

		return predicates;
	}

	/*
	 * dựa vào filterFields để đếm số row trong bảng Location thỏa điều kiện bộ lọc
	 * <=> đếm totalElements
	 */
	private long getTotalRows(Map<String, Object> filterFields) {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

		Root<Location> root = criteriaQuery.from(Location.class);

		criteriaQuery.select(criteriaBuilder.count(root));

		List<Predicate> predicates = createPredicates(filterFields, criteriaBuilder, root);
		if (!predicates.isEmpty()) {
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
		}

		TypedQuery<Long> typedQuery = this.entityManager.createQuery(criteriaQuery);

		return typedQuery.getSingleResult();

	}
}
