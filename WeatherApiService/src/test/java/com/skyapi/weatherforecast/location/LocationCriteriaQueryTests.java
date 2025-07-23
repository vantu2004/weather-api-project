package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skyapi.weatherforecast.common.Location;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class LocationCriteriaQueryTests {
	@Autowired
	private EntityManager entityManager;

	@Test
	public void testCriteriaQuery() {
		/*
		 * Lấy CriteriaBuilder từ EntityManager để xây dựng query, CriteriaBuilder cung
		 * cấp các phương thức trả về Predicate/Expression
		 */
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		// Tạo một CriteriaQuery với kết quả trả về là entity Location - viết query
		CriteriaQuery<Location> criteriaQuery = criteriaBuilder.createQuery(Location.class);

		// Chỉ định bảng chính (root entity) trong truy vấn là bảng Location
		Root<Location> root = criteriaQuery.from(Location.class);

		// predicate chỉ dùng cho where
		Predicate predicate = criteriaBuilder.equal(root.get("countryCode"), "VN");
		criteriaQuery.where(predicate);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("cityName")));

		// dùng entityManager để chuyển criteriaQuery về dạng có thể thực hiện truy vấn
		TypedQuery<Location> typedQuery = entityManager.createQuery(criteriaQuery);

		// getResultList() thực hiện truy vấn và lấy kết quả
		List<Location> locations = typedQuery.getResultList();

		assertThat(locations).isNotEmpty();

		locations.forEach(System.out::println);
	}

	@Test
	public void testJPQLQuery() {
		// Viết truy vấn JPQL để lấy tất cả bản ghi từ entity Location
		String jpql = "FROM Location"; // tương đương với SELECT l FROM Location l

		// Tạo query JPQL và chỉ định kết quả trả về là kiểu Location
		TypedQuery<Location> typedQuery = entityManager.createQuery(jpql, Location.class);

		// Thực hiện truy vấn và lấy kết quả
		List<Location> locations = typedQuery.getResultList();

		assertThat(locations).isNotEmpty();

		locations.forEach(System.out::println);
	}

}
