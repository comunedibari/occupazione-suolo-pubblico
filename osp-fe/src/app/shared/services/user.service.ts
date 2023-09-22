import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { User } from '@models/login/user';
import { ModificaPasswordRequest } from '@models/modifica-password-request';
import { UtenteDTO } from '@models/utente-dto';
import { Observable } from 'rxjs';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class UserService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient,
  ) {
    super(config, http);
  }

  public getUser(userId: number): Observable<UtenteDTO> {
    return this.httpGet<any>(`user-management/user/${userId}`);
  }

  public getUsers(): Observable<any> {
    return this.httpGet<any>('user-management/user?size=10000');
  }

  public getGruppoMunicipio(idGruppo : number, idMunicipio : number) : Observable<any>{
    let params = this.defaultParams;

    params = params.append('idGruppo', ''+ idGruppo);
    params = params.append('idMunicipio', ''+ idMunicipio);

    return this.httpGet<any>('user-management/user/gruppo-municipio', params);
  }

  public getUsersGruppo(idGruppo: number): Observable<UtenteDTO[]> {
    let params = this.defaultParams;
    params = params.append('idGruppo', ''+idGruppo);
    return this.httpGet<UtenteDTO[]>('user-management/user/gruppo', params);
  }

  inserisciUtente(nuovoutente: UtenteDTO): Observable<any> {
    return this.httpPost<any>('user-management/user', nuovoutente);
  }

  modifyUser(user: UtenteDTO): Observable<any> {
    return this.httpPut<any>("user-management/user", user);
  }

  cambiaPasswordUtente(username: string, password: string, oldPassword: string): Observable<any> {
    let request: ModificaPasswordRequest = new ModificaPasswordRequest();
    request.oldPassword = oldPassword;
    request.password = password;
    request.username = username;
    return this.httpPut<any>('user-management/user/password', request);
  }

  eliminaUtente(user: UtenteDTO) {
    return this.httpDelete<any>(`user-management/user/${user.id}`);
  }
}
