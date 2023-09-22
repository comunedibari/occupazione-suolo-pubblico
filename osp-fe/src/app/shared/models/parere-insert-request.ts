import { ParereDTO } from "./dto/parere-dto";

export class ParereInsertRequest {
    parere: ParereDTO;
    listaIdUtentiEmail: number[];
    flagPec: boolean;
}
