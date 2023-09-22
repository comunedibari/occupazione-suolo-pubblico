import { UtenteDTO } from "@models/utente-dto";
import { TipologicaDTO } from "./tipologica-dto";

export class TemplateDTO {
	id: number;
	tipoTemplate: TipologicaDTO;
	nomeFile: string;
	mimeType: string;
	dataInserimento: Date;
	dataModifica: Date;
	utenteModifica: UtenteDTO;
    fileTemplate: string;
}
