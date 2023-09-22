import { DatiRichiestaDTO } from "./dto/dati-richiesta-dto";
import { RichiedenteDTO } from "./dto/richiedente-dto";

export class PraticaRequest {
	id: number;
	idPraticaOriginaria?: number;
	idUtente: number;
	idMunicipio: number;
	firmatario: RichiedenteDTO;
	destinatario: RichiedenteDTO;
	datiRichiesta: DatiRichiestaDTO;
	idTipoProcesso: number;
	codiceDeterminaRettifica?: string;
	dataEmissioneDeterminaRettifica?: string;
}
