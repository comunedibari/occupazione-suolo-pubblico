package it.fincons.ospscheduler.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Table(name = "T_UTENTE")
@Data
public class Utente implements Serializable {

	private static final long serialVersionUID = 6907098283035861853L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTENTE")
	@SequenceGenerator(name = "SEQ_UTENTE", sequenceName = "SEQ_UTENTE", allocationSize = 1, initialValue = 1)
	private Long id;

	@Size(max = 50)
	@Column(name = "NOME")
	private String nome;

	@Size(max = 50)
	@Column(name = "COGNOME")
	private String cognome;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "utentePresaInCarico")
	private List<Pratica> pratiche;

}
