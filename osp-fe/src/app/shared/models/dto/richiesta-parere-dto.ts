import { ParereDTO } from "./parere-dto";

export class RichiestaParereDTO {
	id: number;
	idPratica: number;
	idUtenteRichiedente: number;
	idStatoPratica: number;
	idGruppoDestinatarioParere: number;
	codiceProtocollo: string;
	notaRichiestaParere: string;
	dataInserimento: Date;
	flagInseritaRisposta: boolean;
	parere: ParereDTO;
	riabilitaEsenzioneMarcaDaBollo: boolean;
}
