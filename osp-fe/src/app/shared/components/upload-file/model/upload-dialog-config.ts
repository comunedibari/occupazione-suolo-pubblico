import { EventEmitter } from "@angular/core";
import { PraticaDto } from "@models/dto/pratica-dto";
import { DestinazioneAllegato } from "../enums/destinazione-allegato.enum";
import { Mode } from "../enums/mode.enum";

export interface UploadDialogConfig {
    pratica: PraticaDto;
    mode: Mode;
    readonly: boolean;
    destinazioneAllegato: DestinazioneAllegato;
    idGruppoDestinatarioParere?: number;
    filtroStatoPratica?: number;
    dettaglioPratica?: boolean;
    onDocUploaded: EventEmitter<any>;
}
