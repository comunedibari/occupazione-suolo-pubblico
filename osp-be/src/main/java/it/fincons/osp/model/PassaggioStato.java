package it.fincons.osp.model;

import java.util.stream.Stream;

public enum PassaggioStato {
	/*
	BOZZA(0,"La bozza della pratica è stata inserita nel sistema"),
	INSERITA(1,""),
	VERIFICA_FORMALE(2,""),
	RICHIESTA_PARERI(3,""),
	NECESSARIA_INTEGRAZIONE(4,""),
	DA_RIGETTARE(5,""),
	APPROVATA(6,""),
	PREAVVISO_DINIEGO(7,""),
	ATTESA_DI_PAGAMENTO(8,""),
	PRONTO_AL_RILASCIO(9,""),
	CONCESSIONE_VALIDA(10,""),
	ARCHIVIATA(18,""),
	RIGETTATA(20,""),
	REVOCATA(21,""),
	DECADUTA(22,""),
	ANNULLATA(23,""),
	SOSPESA(24,""),
	TERMINATA(25,""),
	RINUNCIATA(26,""),*/

	INSERITA_TO_INSERITA_EGOV(1,1,"L\'istante ha modificato le date di occupazione"),
	VERIFICA_FORMALE_TO_VERIFICA_FORMALE_EGOV(2,2,"L\'istante ha modificato le date di occupazione"),
	RICHIESTA_PARERI_TO_VERIFICA_FORMALE_EGOV(3,2,"L\'istante ha modificato le date di occupazione"),

	RETTIFICA_DATE(27,27,"Si è richiesta una modifica delle date di occupazione all\'istante"),
	INSERITA_TO_RETTIFICA_DATE(1,27,"Si è richiesta una modifica delle date di occupazione all\'istante"),
	VERIFICA_FORMALE_TO_RETTIFICA_DATE(2,27,"Si è richiesta una modifica delle date di occupazione all\'istante"),
	RICHIESTA_PARERI_TO_RETTIFICA_DATE(3,27,"Si è richiesta una modifica delle date di occupazione all\'istante"),

	BOZZA_TO_INSERITA(0,1,"La pratica è stata acquisita e protocollata"),
	INSERITA_TO_VERIFICA_FORMALE(1,2,"La pratica è stata assegnata/presa in carico per la relativa istruttoria"),
	VERIFICA_FORMALE_TO_RICHIESTA_PARERI(2,3,"La pratica è stata verificata e sono stati richiesti i pareri agli Uffici di competenza"),
	VERIFICA_FORMALE_TO_NECESSARIA_INTEGRAZIONE(2,4,"La pratica è stata verificata ma è necessaria una integrazione da parte dell\'istante"),
	VERIFICA_FORMALE_TO_DA_RIGETTARE(2,5,"La pratica è stata verificata e risulta da rigettare"),
	VERIFICA_FORMALE_TO_PREAVVISO_DINIEGO(2,7,"La pratica è stata verificata ed è stato emesso il preavviso di diniego"),
	VERIFICA_FORMALE_TO_CONCESSIONE_VALIDA(2,10,""),
	VERIFICA_FORMALE_TO_APPROVATA(2,6,"La pratica è stata verificata ed è stata approvata"),
	//RICHIESTA_PARERI_TO_ESPRIMI_PARERE(3,3,"La ripartizione ha effettuato i controlli di propria competenza"),//aggiungere il ruolo prima della label al posto di "La ripartizione"
	RICHIESTA_PARERI_TO_ESPRIMI_PARERE(3,3,"%s ha effettuato i controlli di propria competenza"),//aggiungere il ruolo prima della label al posto di "La ripartizione"
	RICHIESTA_PARERI_TO_RICHIESTA_PARERI(3,560,"Sono stati richiesti ulteriori pareri agli Uffici di competenza"),
	RICHIESTA_PARERI_TO_APPROVATA(3,6,"La pratica è stata verificata ed è stata approvata"),
	RICHIESTA_PARERI_TO_DA_RIGETTARE(3,5,"La pratica è stata verificata e risulta da rigettare"),
	PREAVVISO_DINIEGO_TO_DA_RIGETTARE(7,5, "L'istante non ha risposto al preavviso diniego nei tempi prestabiliti: la pratica risulta da rigettare"),
	RICHIESTA_PARERI_TO_PREAVVISO_DINIEGO(3,7,"La pratica è stata verificata ed è stato emesso il preavviso di diniego"),
	RICHIESTA_PARERI_TO_NECESSARIA_INTEGRAZIONE(3,4,"La pratica è stata verificata ma è necessaria una integrazione da parte dell\'istante"),
	ATTESA_DI_PAGAMENTO_TO_PRONTO_AL_RILASCIO(8,9,"I tributi previsti sono stati pagati ed è possibile procedere al rilascio dell\'atto concessorio"),
	APPROVATA_TO_ATTESA_DI_PAGAMENTO(6, 8,"La determina esecutiva è stata emessa e si è in attesa del pagamento dei tributi previsti"),
	PRONTO_AL_RILASCIO_TO_CONCESSIONE_VALIDA(9,10,"L\'atto concessorio è stato rilasciato e la concessione è valida"),
	DA_RIGETTARE_TO_RIGETTATA(5,20,"La richiesta è stata rigettata e la relativa determina è stata emessa"),
	NECESSARIA_INTEGRAZIONE_TO_VERIFICA_FORMALE(4,2,"La pratica è stata integrata dall\'istante"),
	CONCESSIONE_VALIDA_TO_TERMINATA(10,25,"La concessione è scaduta: si attendono le verifiche del ripristino dei luoghi"),
	CONCESSIONE_VALIDA_TO_REVOCATA(10,21,"La determina esecutiva di revoca è stata emessa: si attendono le verifiche del ripristino dei luoghi"),
	CONCESSIONE_VALIDA_TO_DECADUTA(10,22,"La determina esecutiva di decadenza è stata emessa: si attendono le verifiche del ripristino dei luoghi"),
	CONCESSIONE_VALIDA_TO_ANNULLATA(10,23,"La determina esecutiva di annullamento è stata emessa: si attendono le verifiche del ripristino dei luoghi"),
	PREAVVISO_DINIEGO_TO_VERIFICA_FORMALE(7,2,"La pratica è stata integrata dall\'istante in risposta al preavviso del diniego"),
	RETTIFICA_DATE_TO_VERIFICA_FORMALE(27,2,"Le date di occupazione sono state posticipate dall\'istante"),
	RETTIFICA_DATE_TO_PREAVVISO_DINIEGO(27,7, "L'istante non ha risposto alla richiesta di rettifica date nei tempi prestabiliti: la pratica è passata in preavviso diniego"),

	/*RETTIFICA_PRATICA*/
	RETTIFICA_PRATICA(-1, -1,"La concessione ha subito un processo di rettifica per la risoluzione di errori materiali e la relativa determina è stata adottata"),
	ATTESA_DI_PAGAMENTO_TO_ATTESA_DI_PAGAMENTO(8, 8,"La concessione ha subito un processo di rettifica per la risoluzione di errori materiali e la relativa determina è stata adottata"),
	PRONTO_AL_RILASCIO_TO_PRONTO_AL_RILASCIO(9, 9,"La concessione ha subito un processo di rettifica per la risoluzione di errori materiali e la relativa determina è stata adottata"),
	CONCESSIONE_VALIDA_TO_CONCESSIONE_VALIDA(10, 10,"La concessione ha subito un processo di rettifica per la risoluzione di errori materiali e la relativa determina è stata adottata"),
	/*RETTIFICA_PRATICA*/

	/*ESENZIONE_CUP*/
	ESENZIONE_CUP(-1,-1,"Dichiarazione di esenzione dal pagamento del CUP"),
	VERIFICA_FORMALE_TO_VERIFICA_FORMALE(2,2,"Dichiarazione di esenzione dal pagamento del CUP"),
	/*ESENZIONE_CUP*/

	REVOCATA_TO_ARCHIVIATA(21,18,"Le verifiche del ripristino dei luoghi della concessione revocata sono state effettuate"),
	DECADUTA_TO_ARCHIVIATA(22,18,"Le verifiche del ripristino dei luoghi della concessione decaduta sono state effettuate"),
	ANNULLATA_TO_ARCHIVIATA(23,18,"Le verifiche del ripristino dei luoghi della concessione annullata sono state effettuate"),
	TERMINATA_TO_ARCHIVIATA(25,18,"Le verifiche del ripristino dei luoghi della concessione terminata sono state effettuate"),
	RINUNCIATA_TO_ARCHIVIATA(26,18,"Le verifiche del ripristino dei luoghi della concessione rinunciata sono state effettuate"),
	APPROVATA_TO_RINUNCIATA(6,26,"La determina esecutiva di rinuncia è stata emessa"),
	CONCESSIONE_VALIDA_TO_ARCHIVIATA(10,18,"La concessione è stata archiviata"),
	;

	private final Integer idStatoPraticaSrc;
	private final Integer idStatoPraticaDest;

	private final String descStatoPratica;

	private PassaggioStato(Integer idStatoPraticaSrc, Integer idStatoPraticaDest, String descStatoPratica){
		this.idStatoPraticaSrc=idStatoPraticaSrc;
		this.idStatoPraticaDest=idStatoPraticaDest;
		this.descStatoPratica=descStatoPratica;
	}

	public String getDescStatoPratica() {
		return descStatoPratica;
	}

	public Integer getIdStatoPraticaSrc() {
		return idStatoPraticaSrc;
	}

	public Integer getIdStatoPraticaDest() {
		return idStatoPraticaDest;
	}

	public static String getDescStatoPratica(Integer idStatoPraticaSrc, Integer idStatoPraticaDest) {
		PassaggioStato passaggioStato= Stream.of(PassaggioStato.values())
				.filter(d -> (d.getIdStatoPraticaSrc().intValue()==idStatoPraticaSrc) && (d.getIdStatoPraticaDest().intValue()==idStatoPraticaDest) )
				.findAny()
				.orElse(null);

		return passaggioStato==null?idStatoPraticaSrc+"-"+idStatoPraticaDest:passaggioStato.getDescStatoPratica();
	}

}
