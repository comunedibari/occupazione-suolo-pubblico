import { TipoAllegatoDTO } from "./tipo-allegato-dto";

export class TipoAllegatoGruppoStatoProcessoDTO {
	flagObbligatorio: boolean;
	flagTestoLibero: boolean;
	idGruppo: number;
	idStatoPratica: number;
	idTipoProcesso: number;
	tipoAllegato: TipoAllegatoDTO;
}
