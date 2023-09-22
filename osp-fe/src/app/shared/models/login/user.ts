export interface User {
    id: number,
    password?: string,
    codicefiscale: string,
    dataDiNascita: Date,
    nome: string,
    cognome: string,
    ragioneSociale: string,
    municipio_ids: number[],
    lastLogin: Date,
    email: string,
    uoId: string
}
