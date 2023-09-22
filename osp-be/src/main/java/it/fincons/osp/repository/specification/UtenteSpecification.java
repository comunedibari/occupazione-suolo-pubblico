package it.fincons.osp.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import it.fincons.osp.model.Utente;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class UtenteSpecification implements GenericSpecification<Utente> {

	public Specification<Utente> isNotEliminato() {
		return (root, query, builder) -> builder.isFalse(root.get("flagEliminato"));
	}

}