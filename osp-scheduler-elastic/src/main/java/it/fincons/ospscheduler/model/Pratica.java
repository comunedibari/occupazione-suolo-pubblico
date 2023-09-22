package it.fincons.ospscheduler.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "T_PRATICA", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_PRATICA_DATI_RICHIESTA", columnNames = "ID_DATI_RICHIESTA")})
@Data
public class Pratica implements Serializable {

    private static final long serialVersionUID = -1734237787812205647L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PRATICA")
    @SequenceGenerator(name = "SEQ_PRATICA", sequenceName = "SEQ_PRATICA", allocationSize = 1, initialValue = 1)
    private Long id;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "ID_DATI_RICHIESTA", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PRATICA_DATI_RICHIESTA"), nullable = false)
    private DatiRichiesta datiRichiesta;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "ID_RICHIEDENTE_FIRMATARIO", foreignKey = @ForeignKey(name = "FK_PRATICA_RICH_FIRMATARIO"), nullable = false)
    private Richiedente firmatario;

    @ManyToOne
    @JoinColumn(name = "ID_MUNICIPIO", foreignKey = @ForeignKey(name = "FK_PRATICA_MUNICIPIO"))
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "ID_UTENTE_PRESA_IN_CARICO", foreignKey = @ForeignKey(name = "FK_PRATICA_UTENTE_PC"))
    @JoinColumn(name = "ID_UTENTE_PRESA_IN_CARICO", nullable = true)
    private Utente utentePresaInCarico;

    @Column(name = "DATA_INSERIMENTO")
    private LocalDateTime dataInserimento;

    @ManyToOne
    @JoinColumn(name = "ID_TIPO_PROCESSO", foreignKey = @ForeignKey(name = "FK_PRATICA_TIPO_PROCESSO"), nullable = false)
    private TipoProcesso tipoProcesso;

    @ManyToOne
    @JoinColumn(name = "ID_STATO_PRATICA", foreignKey = @ForeignKey(name = "FK_PRATICA_STATO_PRATICA"), nullable = false)
    private StatoPratica statoPratica;

}
