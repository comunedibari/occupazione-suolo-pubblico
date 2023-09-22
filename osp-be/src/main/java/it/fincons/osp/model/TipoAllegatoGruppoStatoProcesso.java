package it.fincons.osp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;

@Entity
@Table(name = "A_TIPO_ALLEGATO_GRUP_STAT_PROC", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_A_TIPO_ALLEGATO", columnNames = { "ID_TIPO_ALLEGATO", "ID_STATO_PRATICA",
				"ID_TIPO_PROCESSO", "ID_GRUPPO" }) })
@Data
public class TipoAllegatoGruppoStatoProcesso implements Serializable {

	private static final long serialVersionUID = 2708528817591540908L;

	@Id
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_ALLEGATO", foreignKey = @ForeignKey(name = "FK_A_TIPO_ALL"), nullable = false)
	private TipoAllegato tipoAllegato;

	@Column(name = "FLG_OBBLIGATORIO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagObbligatorio;

	@Column(name = "FLG_TESTO_LIBERO", nullable = false, columnDefinition = "boolean default false")
	private boolean flagTestoLibero;

	@ManyToOne
	@JoinColumn(name = "ID_STATO_PRATICA", foreignKey = @ForeignKey(name = "FK_A_TIPO_ALL_STATO_PRATICA"), nullable = false)
	private StatoPratica statoPratica;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_PROCESSO", foreignKey = @ForeignKey(name = "FK_A_TIPO_ALL_TIPO_PROCESSO"), nullable = false)
	private TipoProcesso tipoProcesso;

	@ManyToOne
	@JoinColumn(name = "ID_GRUPPO", foreignKey = @ForeignKey(name = "FK_A_TIPO_ALL_GRUPPO"), nullable = false)
	private Gruppo gruppo;
}
