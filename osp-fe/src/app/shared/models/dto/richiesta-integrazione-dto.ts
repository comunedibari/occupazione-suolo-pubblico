import { IntegrazioneDTO } from "./integrazione-dto";

export class RichiestaIntegrazioneDTO {
	id: number;
	idPratica: number;
	idUtenteRichiedente: number;
	idStatoPratica: number;
	codiceProtocollo: string;
	tipopRichiesta: string;
	motivoRichiesta: string;
	dataInserimento: Date;
	dataScadenza: Date;
	integrazione: IntegrazioneDTO;
	flagEsenzioneMarcaDaBollo: boolean;
	idAllegato: number;	
}
