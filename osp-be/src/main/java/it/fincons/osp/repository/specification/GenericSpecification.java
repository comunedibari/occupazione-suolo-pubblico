package it.fincons.osp.repository.specification;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public interface GenericSpecification<T> {

	default Specification<T> addSpecificationToConditionListAnd(Optional<Specification<T>> condition,
			Specification<T> completeCondition) {
		if (condition.isPresent()) {
			return completeCondition.and(condition.get());
		}
		return completeCondition;
	}

	default Specification<T> addSpecificationToConditionListOr(Optional<Specification<T>> condition,
			Specification<T> completeCondition) {
		if (condition.isPresent()) {
			return completeCondition.or(condition.get());
		}
		return completeCondition;
	}

	default Specification<T> inizialize() {
		return (root, query, builder) -> {
			query.distinct(true);
			return builder.greaterThan(root.get("id"), 0);
		};
	}

	default Optional<Specification<T>> equals(Object field, String fieldName) {
		return field == null ? Optional.empty()
				: Optional.of((root, query, builder) -> builder.equal(root.get(fieldName), field));
	}

	default Optional<Specification<T>> notEquals(Object field, String fieldName) {
		return field == null ? Optional.empty()
				: Optional.of((root, query, builder) -> builder.notEqual(root.get(fieldName), field));
	}

	default Optional<Specification<T>> contains(String field, String fieldName) {
		return StringUtils.isEmpty(field) ? Optional.empty()
				: Optional.of((root, query, builder) -> builder.like(builder.lower(root.get(fieldName)),
						"%" + field.toLowerCase() + "%"));
	}

	default Specification<T> alwaysFalse() {
		return (root, query, builder) -> builder.lessThan(root.get("id"), 0);
	}

	default Optional<Specification<T>> equalsDateTimeWithoutSecondsAndMilli(LocalDateTime field, String fieldName) {
		if (field == null)
			return Optional.empty();

		Specification<T> s1 = (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(fieldName),
				field.truncatedTo(ChronoUnit.MINUTES));
		Specification<T> s2 = (root, query, builder) -> builder.lessThan(root.get(fieldName),
				field.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1));

		return Optional.of(s1.and(s2));
	}
}