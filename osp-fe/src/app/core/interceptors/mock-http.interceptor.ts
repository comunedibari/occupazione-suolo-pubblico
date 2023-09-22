import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { LoginInfo } from '@models/login/login-info';
import { UtenteDTO } from '@models/utente-dto';
import { environment } from 'environments/environment';

const loginOperatore: LoginInfo = {
  idUser: 1,
  auth: true,
  token: 'testToken',
  username: 'operatore.1',
  groups: {
    id: 2,
    auth: {
      inserisciRichiesta: true,
      concessioniValide: true,
      fascicolo: true,
      praticaInserita: false,
      verificaPratiche: false,
      richiestaPareri: false,
      necessariaIntegrazione: false,
      praticheApprovate: false,
      attesaPagamento: false,
      pronteRilascio: false,
      praticheDaRigettare: false,
      pratichePreavvisoDiniego: false,
      praticheArchiviate:false,
      praticheRigettate: false,
      gestioneProfilo: true,
      gestioneUtenti: false,
    },
  },
  userLogged: {   
    id: 2,
    codicefiscale: 'BDOPPP23L10H501Z',
    dataDiNascita: new Date(),
    nome: 'Operatore',
    cognome: '1',
    ragioneSociale: null,
    municipio_ids: [1],
    lastLogin: new Date(),
    email: 'operatore.1@xxxyyy.it',
    uoId: 'xxx',
  },
}

const loginAdmin: LoginInfo = {
  idUser: 1,
  auth: true,
  token: 'testToken',
  username: 'admin',
  groups: {
    id: 1,
    auth: {
      inserisciRichiesta: false,
      concessioniValide: false,
      fascicolo: false,
      praticaInserita: false,
      verificaPratiche: false,
      richiestaPareri: false,
      necessariaIntegrazione: false,
      praticheApprovate: false,
      attesaPagamento: false,
      pronteRilascio: false,
      praticheDaRigettare: false,
      pratichePreavvisoDiniego: false,
      praticheArchiviate:false,
      praticheRigettate: false,
      gestioneProfilo: true,
      gestioneUtenti: true,
    },
  },
  userLogged: {    
    id: 1,
    codicefiscale: 'BDOPPP23L10H501Z',
    dataDiNascita: new Date(),
    nome: 'Admin',
    cognome: 'Admin',
    ragioneSociale: null,
    municipio_ids: null,
    lastLogin: new Date(),
    email: 'admin@xxxyyy.it',
    uoId: null,
  },
}

const errorLogin = {
  status: 404, statusText: 'Not Found',
  error: {
    auth: false,
    token: null,
    message: 'Username o Password Errata' 
  }
}

const logoutResponse = {
  auth: false,
  token: null,
  message: "Logout utente registrato." 
}

const utenteAdmin: UtenteDTO = {
  id: 1,
	idGruppo: 1,
	idsMunicipi: null,
	username: 'pippo.baudo',
	password: null,
	nome: 'Pippo',
	cognome: 'Baudo',
	sesso: 'M',
  dataDiNascita: new Date(),
	luogoDiNascita: 'Palermo',
	provinciaDiNascita: 'PA',
	codiceFiscale: 'BDAPPP22A20G273V',
	ragioneSociale: null,
  indirizzo: null,
	email: 'pippo.baudo@comunebari.it',
	numTel: null,
  enabled: true,
	dateCreated: new Date(),
	lastLogin: new Date(),
  uoId: null,
  skipCheckConcessionario: false
}

@Injectable()
export class MockHttpInterceptor implements HttpInterceptor {


  constructor() {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
  if(environment.useMoks){
console.log("MockHttpInterceptor. Method <" + request.method + "> URL <" + request.url+ ">");  
    if (request.method === "POST" && request.url.endsWith('api/osp/auth/login')) {
      if (request.body["username"] === "operatore") {
        return of(new HttpResponse({ status: 200, body: loginOperatore }));
      }
      if (request.body["username"] === "admin") {
        return of(new HttpResponse({ status: 200, body: loginAdmin }));
      }
      else {
        let error: HttpErrorResponse = new HttpErrorResponse(errorLogin);
        return throwError(error);
      }
    }
    else if (request.method === "POST" && request.url.endsWith('api/osp/auth/logout')) {
      return of(new HttpResponse({ status: 200, body: logoutResponse }));
      //let error: HttpErrorResponse = new HttpErrorResponse({status: 400, statusText: 'Bad Request', error: {message: 'Errore durante salvataggio del logout.'}});
      //return throwError(error);
    }
    else if (request.method === "POST" && request.url.endsWith('api/osp/auth/recupero-password')) {
      return of(new HttpResponse({ status: 200, body: "Nuova password inviata all'indirizzo associato all'account"}));
    }
    else if (request.method === "GET" && request.url.endsWith('api/osp/user-management/user')) {
      return next.handle(request);
    }
    else if (request.method === "GET" && request.url.includes('api/osp/user-management/user')) {
      return of(new HttpResponse({ status: 200, body: utenteAdmin }));
    }
  }  
    return next.handle(request);
  }
}
