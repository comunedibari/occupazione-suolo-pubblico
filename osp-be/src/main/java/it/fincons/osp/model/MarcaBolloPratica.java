package it.fincons.osp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "T_MARCA_BOLLO_PRATICA")
@Data
public class MarcaBolloPratica implements Serializable {

	private static final long serialVersionUID = -4860285842646448439L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MARCA_BOLLO_PRATICA")
	@SequenceGenerator(name = "SEQ_MARCA_BOLLO_PRATICA", sequenceName = "SEQ_MARCA_BOLLO_PRATICA", allocationSize = 1, initialValue = 1)
	private Long id;

	@OneToOne(mappedBy = "marcaBolloPratica")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Pratica pratica;

	@Column(name = "IUV")
	private String iuv;

	@Column(name = "IMPRONTA_FILE")
	private String improntaFile;

	@Column(name = "IMPORTO_PAGATO")
	private double importoPagato;

	@Column(name = "CAUSALE_PAGAMENTO")
	private String causalePagamento;

	@Column(name = "ID_RICHIESTA")
	private String idRichiesta;

	@Column(name = "DATA_OPERAZIONE")
	private LocalDate dataOperazione;

}
