import { PraticaDto } from "@models/dto/pratica-dto";

export interface DettaglioPraticaDialogConfig {
    pratica: PraticaDto;
    isPraticaOrigine: boolean;
    showStorico: boolean;
}
