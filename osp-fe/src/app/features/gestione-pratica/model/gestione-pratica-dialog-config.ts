import { PraticaDto } from "@models/dto/pratica-dto";

export interface GestionePraticaDialogConfig {
    pratica: PraticaDto;
    readonly: boolean;
    isDettaglio?: boolean;
    isRichiestaProroga?: boolean;
    isRinunciaConcessione?: boolean;
    isAvviaRettifica?: boolean;
}
