import { Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { LoginInfo } from '@models/login/login-info';
import { Groups } from '@models/login/groups';
import { BaseService } from '@services/base/base-service';
import { TokenStorage } from '@services/token.storage.service';
import { SessionService } from '@services/session.service';
import { OccupazioneSuoloPubblicoApiConfiguration } from '@services/base/occupazione-suolo-pubblico-api-configuration';
import { tap } from 'rxjs/operators';



@Injectable({
  providedIn: 'root'
})
export class AuthService extends BaseService {
  static AUTH_INSERISCI_RICHIESTA: string = 'inserisciRichiesta';
  static AUTH_CONCESSIONI_VALIDE: string = 'concessioniValide';
  static AUTH_FASCICOLO: string = 'fascicolo';
  static AUTH_INSERITA: string = 'praticaInserita';
  static AUTH_PRESA_IN_CARICO: string = 'presaInCarico';
  static AUTH_VERIFICA_PRATICHE: string = 'verificaPratiche';
  static AUTH_RICHIESTA_PARERI: string = 'richiestaPareri';
  static AUTH_VERIFICA_RIPRISTINO_LUOGHI: string = 'verificaRipristinoLuoghi';
  static AUTH_NECESSARIA_INTEGRAZIONE: string = 'necessariaIntegrazione';
  static AUTH_PRATICHE_APPROVATE: string = 'praticheApprovate';
  static AUTH_PRATICHE_ATTESA_PAGAMENTO: string = 'attesaPagamento';
  static AUTH_PRATICHE_PRONTE_RILASCIO: string = 'pronteRilascio';
  static AUTH_PRATICHE_DA_RIGETTARE: string = 'praticheDaRigettare';
  static AUTH_PRATICHE_ARCHIVIATE: string = 'praticheArchiviate';
  static AUTH_PRATICHE_RIGETTATE: string = 'praticheRigettate';
  static AUTH_IL_MIO_PROFILO: string = 'gestioneProfilo';
  static AUTH_GESTIONE_UTENTI: string = 'gestioneUtenti';

  static SESSION_LOGGED_USER: string = 'LoggedUser';

  private loggedSubject = new Subject<boolean>();

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient,
    private token: TokenStorage,
    private router: Router,
    private sessionService: SessionService,
  ) {
    super(config, http);
  }

  onUserLogged(): Observable<boolean> {
    return this.loggedSubject.asObservable();
  }

  login(username: string, password: string): Observable<LoginInfo> {
    return this.httpPost<LoginInfo>('auth/login', { username: username, password: password }).pipe(
      tap((data)=> {
        this.loggedSubject.next(true);
      })
    );
  }

  updateLastLogin(): Observable<any> {
    let username = this.getUsername();
    return this.httpPost<any>("auth/logout", { username: username });
  }

  signOut() {
    this.loggedSubject.next(false);
    this.resetTokenAndStorage();
    this.router.navigate(['/login']);
  }

  resetTokenAndStorage(){
    this.token.removeToken();
    this.sessionService.clear();
  }

  public saveToken(token: string) {
    this.token.saveToken(token);
  }

  public saveLoggedUser(user: LoginInfo) {
    this.sessionService.setSessionValue(AuthService.SESSION_LOGGED_USER, user);
    this.loggedSubject.next(true);
  }

  public getLoggedUser(): LoginInfo {
    return this.sessionService.getSessionValue(AuthService.SESSION_LOGGED_USER);
  }

  public checkGroups(groupName: string) {
    let ret = false;
    let loggedUser: LoginInfo = this.getLoggedUser();
    let pacGoups: Groups = loggedUser.groups;
    if (pacGoups.auth) {
      ret = pacGoups.auth[groupName] === true;
    }
    return ret;
  }

  public getToken(): string {
    return this.token.getToken();
  }

  public getGroup(): any {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let pacGoups: Groups = loggedUser?.groups;
    if (loggedUser && pacGoups) {
      return pacGoups.id;
    } else {
      this.signOut();
    }
  }

  public getMunicipio(): number | null {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let municipio_id: number = loggedUser?.userLogged.municipio_ids[0];

    return municipio_id;
  }

  public getMunicipiAppartenenza(): number[] | null {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let municipi_id: number[] = loggedUser?.userLogged.municipio_ids;

    return municipi_id;
  }

  public getUsername(): string {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let ret: string = loggedUser?.username;
    if (loggedUser && ret) {
      return ret;
    } else {
      this.signOut();
    }
  }

  public getCodFiscale(): string {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let ret: string = loggedUser?.userLogged.codicefiscale;
    if (loggedUser && ret) {
      return ret;
    } else {
      this.signOut();
    }
  }

  public getNomeUtente(): string {
    let loggedUser: LoginInfo = this.getLoggedUser();
    return loggedUser.userLogged.nome;
  }

  public getCognomeUtente(): string {
    let loggedUser: LoginInfo = this.getLoggedUser();
    return loggedUser.userLogged.cognome;
  }

  public getRagioneSociale(): string {
    let loggedUser: LoginInfo = this.getLoggedUser();
    return loggedUser.userLogged.ragioneSociale;
  }

  public getInfoUtente() {
    if (this.getNomeUtente() && this.getCognomeUtente()) {
      return `${this.getNomeUtente()} ${this.getCognomeUtente()}`;
    } else if(this.getRagioneSociale() && !this.getNomeUtente()){
      return this.getRagioneSociale() || '';
    } else {
      this.signOut();
    }
  }

  public getLastLogin(): any {
    let loggedUser: LoginInfo = this.getLoggedUser();
    return loggedUser?.userLogged.lastLogin;
  }

  public getEmail(): any {
    let loggedUser: LoginInfo = this.getLoggedUser();
    let email = loggedUser?.userLogged.email;
    if (loggedUser && email) {
      return email;
    } else {
      this.signOut();
    }
  }

  checkAuth(): boolean {
    if (this.token.getToken()) {
      return true;
    } else {
      return false;
    }
  }

  recuperoPassword(username: string): Observable<any> {
    let credentials = { username: username };
    return this.httpPost<any>('auth/recupero-password', credentials);
  }


  isActiveLinksSistema(): boolean {
    let ret:boolean = this.checkGroups(AuthService.AUTH_GESTIONE_UTENTI)
           || this.checkGroups(AuthService.AUTH_IL_MIO_PROFILO);
    return ret;
  }

  isActiveLinkGestioneRichieste(): boolean {
    return this.checkGroups(AuthService.AUTH_PRESA_IN_CARICO)
           || this.checkGroups(AuthService.AUTH_INSERISCI_RICHIESTA)
           || this.checkGroups(AuthService.AUTH_CONCESSIONI_VALIDE)
           || this.checkGroups(AuthService.AUTH_FASCICOLO);
  }

  isActiveLinkPraticheDaLavorare(): boolean {
    return this.checkGroups(AuthService.AUTH_INSERITA)
      || this.checkGroups(AuthService.AUTH_PRESA_IN_CARICO)
      || this.checkGroups(AuthService.AUTH_VERIFICA_PRATICHE)
      || this.checkGroups(AuthService.AUTH_RICHIESTA_PARERI)
      || this.checkGroups(AuthService.AUTH_VERIFICA_RIPRISTINO_LUOGHI)
      || this.checkGroups(AuthService.AUTH_NECESSARIA_INTEGRAZIONE)
      || this.checkGroups(AuthService.AUTH_PRATICHE_ATTESA_PAGAMENTO)
      || this.checkGroups(AuthService.AUTH_PRATICHE_PRONTE_RILASCIO)
      || this.checkGroups(AuthService.AUTH_PRATICHE_DA_RIGETTARE)
      || this.checkGroups(AuthService.AUTH_PRATICHE_APPROVATE);
  }

  isActiveLinkPraticheConcluse(): boolean {
    return this.checkGroups(AuthService.AUTH_PRATICHE_ARCHIVIATE)
      || this.checkGroups(AuthService.AUTH_PRATICHE_RIGETTATE);
  }
}
