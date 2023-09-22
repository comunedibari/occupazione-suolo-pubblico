import { TipoAllegatoDTO } from "./tipo-allegato-dto";

export class AllegatoDTO {
	id: number; 
	idPratica: number;
	idParere: number;
	idIntegrazione: number;
	idRichiestaParere: number;
	idRichiestaIntegrazione: number;	
	tipoAllegato: TipoAllegatoDTO;
	dataInserimento: Date;
	nota: string;
	nomeFile: string;
	mimeType: string;
	revisione: number;
	codiceProtocollo: string;
	fileAllegato: string;
}