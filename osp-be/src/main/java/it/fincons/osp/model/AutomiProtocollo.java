package it.fincons.osp.model;

import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_AUTOMI_PROTOCOLLO", uniqueConstraints = {
		@UniqueConstraint(name = "UQ_AUTOMI_PROTOCOLLO_UO_ID", columnNames = "UO_ID") })
@Data
public class AutomiProtocollo implements Serializable {

	private static final long serialVersionUID = -1734237787812205647L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AUTOMI_PROTOCOLLO")
	@SequenceGenerator(name = "SEQ_AUTOMI_PROTOCOLLO", sequenceName = "SEQ_AUTOMI_PROTOCOLLO", allocationSize = 1, initialValue = 1)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(name = "UO_ID", nullable = false)
	private String uoId;

	@OneToOne(orphanRemoval = false)
	@JoinColumn(name = "ID_MUNICIPIO", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_AUTOMA_MUNICIPIO"), nullable = true)
	private Municipio municipio;

	@Size(max = 100)
	@Column(name = "DENOMINAZIONE", nullable = false)
	private String denominazione;

	@Size(max = 100)
	@Column(name = "LABEL", nullable = true)
	private String label;

	@Column(name = "DATA_INSERIMENTO", nullable = false)
	private LocalDateTime dataInserimento;

	@Column(name = "DATA_MODIFICA")
	private LocalDateTime dataModifica;



}
