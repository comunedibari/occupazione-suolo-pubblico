package it.fincons.osp.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import it.fincons.osp.model.Richiedente;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class RichiedenteSpecification implements GenericSpecification<Richiedente> {

	public Specification<Richiedente> isDestinatario() {
		return (root, query, builder) -> builder.isTrue(root.get("flagDestinatario"));
	}

}