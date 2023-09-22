export class UtenteDTO {
	id: number;
	idGruppo: number;
	idsMunicipi: number[];
	username: string;
	password: string;
	nome: string;
	cognome: string;
	sesso: string;
    dataDiNascita: Date;
	luogoDiNascita: string;
	provinciaDiNascita: string;
	codiceFiscale: string;
	ragioneSociale: string;
	indirizzo: string;
	email: string;
	numTel: string;
	uoId: number;
    enabled: boolean;
	dateCreated: Date;
	lastLogin: Date;
	flagEliminato?:boolean;
	dataEliminazione?:Date;
	skipCheckConcessionario: boolean;
}
