import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { AuthService } from './auth/auth.service';
import { Group } from '@enums/Group.enum';
import { HttpClient } from'@angular/common/http';
import { TipologiaPratica } from '@enums/TipologiaPratica.enum';
import { StatoPratica } from '@enums/StatoPratica.enum';
import { DateTime } from 'luxon';
import { FormControl, FormGroup } from '@angular/forms';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';
import { BaseService } from './base/base-service';
import CodiceFiscale  from 'codice-fiscale-js';
import { DatePipe } from '@angular/common';
import { saveAs } from 'file-saver';
import { PraticaDto } from '@models/dto/pratica-dto';
import { ProtocolloDTO } from '@models/dto/protocollo-dto';
import { environment } from 'environments/environment';
import { UoConfigurationDto } from '@models/dto/uo-configuration-dto';
import { DashboardDTO } from '@models/dto/dashboard-dto';


@Injectable({
  providedIn: 'root'
})
export class UtilityService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient,
    private router: Router,
    private authService: AuthService,
    private datePipe: DatePipe
  ) {
    super(config, http);
  }

  public n_integrazioni_massime: number = 2;
  public drawCoordinates: any;

  groupToUoMapping = {
    [Group.Admin]: undefined,
    [Group.Concessionario]: undefined,
    [Group.DestinatariOrdinanze]: undefined,
    [Group.PoliziaLocale]: {
      id: 6
    },
    [Group.PoliziaLocaleGenerale]: {
      id: 6
    },
    [Group.RipartizioneRagioneria]: {
      id: 7
    },
    [Group.RipartizioneUrbanistica]: {
      id: 8
    },
    [Group.RipartizionePatrimonio]: {
      id: 9
    },
    [Group.IVOOPPSettoreInfrastruttureARete]: {
      id: 10
    },
    [Group.IVOOPPSettoreGiardini]: {
      id: 11
    },
    [Group.IVOOPPSettoreInterventiSulTerritorio]: {
      id: 12
    },
    [Group.IVOOPPSettoreUrbanizzazioniPrimarie]: {
      id: 13
    },
    [Group.RipartizioneTributi]: {
      id: 14
    },
  };
  static getProtocolloInserimento(protocolli: Array<ProtocolloDTO>): string {
    if (protocolli != null) {
      const protocollo: ProtocolloDTO = protocolli.find((x: ProtocolloDTO) => x.idStatoPratica === environment.statiPratica.inserita);
      if (protocollo) {
        return protocollo.codiceProtocollo;
      }
    }
    return '--';
  }

  goHome(){
    this.router.navigate(['/home']);
  }

  public formatDateForBe(date: string): string {
    return this.datePipe.transform(date, 'yyyy-MM-dd');
  }
  public formatTimeForBe(time: Date): string {
    let ret = this.datePipe.transform(time, 'HH:mm');
    /*
    let ret = null;
    if (time) {
      if (time.length < 5) {
        time = '0' + time;
      }
      time += '00';
    }*/
    return ret;
  }
  public getTimeForFE(time: string): Date {
    let ret: Date = null;
    if (time) {
      let timeParts: string[] = time.split(":");
      ret = new Date();
      ret.setHours(parseInt(timeParts[0]));
      ret.setMinutes(parseInt(timeParts[1]));
    }
    return ret;
  }

  camelCaseToSpace = string => {
    return !string ? '' : string.split('')
      .map(char => {
        if (char == char.toUpperCase()) return ` ${char}`;
        return `${char}`;
      })
      .join('');
  };

  capitalize = (s) => {
    if (typeof s !== 'string') return ''
    return s.charAt(0).toUpperCase() + s.slice(1)
  }

  excludeSpecialCharacters(value: string): boolean {
    var regex = /^[A-Za-z0-9 ]+$/;
    return regex.test(value);
  }

  convertFileToBase64(file){
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = error => reject(error);
    });
  }

  configDynamicDialog(data: any, title: string): DynamicDialogConfig{
    let dialogConfig = new DynamicDialogConfig();
    dialogConfig.data = data;
    // dialogConfig.height = '95%';
    dialogConfig.width = '95%';
    dialogConfig.header = title || "";
    return dialogConfig;
  }

  configDynamicDialogFixed(data: any, title: string, width: number, height: number): DynamicDialogConfig{
    let dialogConfig = new DynamicDialogConfig();
    dialogConfig.data = data;
    dialogConfig.height = `${height}px`;
    dialogConfig.width = `${width}px`;
    dialogConfig.header = title || "";
    return dialogConfig;
  }

  configDynamicDialogFullScreen(data: any, title: string): DynamicDialogConfig{
    let dialogConfig = new DynamicDialogConfig();
    dialogConfig.data = data;
    dialogConfig.height = '95%';
    dialogConfig.width = '95%';
    dialogConfig.header = title || "";
    return dialogConfig;
  }

  getCodiceFiscale(utente): string {
    let cf: any = null;
    try {
      cf = new CodiceFiscale({
        name: utente.nome,
        surname: utente.cognome,
        gender: utente.sesso,
        day: utente.dataDiNascita.getDate(),
        month: utente.dataDiNascita.getMonth() + 1,
        year: utente.dataDiNascita.getFullYear(),
        birthplace: utente.luogoDiNascita,
        birthplaceProvincia: utente.provinciaDiNascita
      });
    } catch (error) {
      console.log("Errore CF: ", error);
    }

    return cf ? <string>cf.cf : "";
  }

  isInternalUser(id?: number): boolean{
    let groupID = id ? id : this.authService.getGroup();
    return groupID == Group.OperatoreSportello
            || groupID == Group.DirettoreMunicipio
            || groupID == Group.IstruttoreMunicipio
            || groupID == Group.PoliziaLocale
            || groupID == Group.RipartizionePatrimonio
            || groupID == Group.RipartizioneUrbanistica
            || groupID == Group.RipartizioneTributi
            || groupID == Group.RipartizioneRagioneria
            || groupID == Group.Concessionario
            || groupID == Group.IVOOPPSettoreUrbanizzazioniPrimarie
            || groupID == Group.IVOOPPSettoreGiardini
            || groupID == Group.IVOOPPSettoreInterventiSulTerritorio
            || groupID == Group.IVOOPPSettoreInfrastruttureARete
            ? true : false;
  }

  takeEmails(obj: any): Observable<any> {
    const url = '/api/utility/takeEmails';
    return this.httpPost<any>(url, obj);
  }

  getDataScadenzaPratica(date: string = null, days? : number): Date {
    let dt = !!date ? DateTime.fromISO(new Date(date).toISOString()) : DateTime.local();
    return dt.plus({ days: (days != undefined || days != null ? days : 90) }).startOf('day').toISO();
  }

  getGiorniPassatiPerIntegrazione(data_operazione){
    let dataInizioIntegrazione = DateTime.fromISO(new Date(data_operazione).toISOString()).startOf('day');
    let dateNow = DateTime.local().startOf('day');
    return  dateNow.diff(dataInizioIntegrazione, 'days').values.days;
  }

  checkDataAvvioProcesso(data_scadenza, minus_days): boolean {
    let dt = DateTime.fromISO(new Date(data_scadenza).toISOString()).startOf('day');
    let dataLimite = dt.minus({days: minus_days});
    let dateNow = DateTime.local().startOf('day');
    let days = dateNow.diff(dataLimite, 'days').values.days;
    return days > 0 ? false : true;
  }

  checkAvvioDecadenza(data_scadenza_notifica_decadenza): boolean {
    let dataLimite = DateTime.fromISO(new Date(data_scadenza_notifica_decadenza).toISOString()).startOf('day');
    let dateNow = DateTime.local().startOf('day');
    let days = dateNow.diff(dataLimite, 'days').values.days;
    return days >= 0 ? true : false;
  }


  getIdDeterminaByType(pratica) {
    let determinaType = '';

    if(pratica.stato_pratica == StatoPratica['Pratica da rigettare'])
      determinaType = 'determina_rigetto';
    else {
      switch(pratica.dati_istanza.tipologia_processo) {
        case TipologiaPratica['Concessione Temporanea']:
          determinaType = 'determina_concessione';
          break;
        case TipologiaPratica['Proroga']:
          determinaType = 'determina_concessione';
          break;
        }
    }

    return determinaType;
  }

  getDeterminaName(pratica){
    let determinaName = this.getIdDeterminaByType(pratica);
    return `${pratica.id_doc} - ${determinaName}.docx`;
  }

  checkAvvioProroga(pratica, preAvvio = false): boolean {
    let days = pratica.dati_istanza.durata_giorni_concessione;

    if(preAvvio) {
      return days < 365 ? true : false;
    }
    else {
      days += pratica.dati_istanza.link_pratica_origine.durata_giorni_concessione;
      return days <= 365 ? true : false;
    }
  }

  checkDichiarazioniTrasferimentoTitolarita(pratica): boolean {
    let allTrue = true;
    //determino se mi trovo nel caso del flusso completo o semplificato
    Object.keys(pratica.dichiarazioni_modifiche_luoghi).forEach(key => {
      if(!pratica.dichiarazioni_modifiche_luoghi[key])
        allTrue = false;
    });
    return allTrue;
  }

  markAsDirtied(group: FormGroup) {
    group.markAllAsTouched();
    Object.keys(group.controls).map((field) => {
      var control = group.get(field);
      if (control instanceof FormControl && control.valid == false) {
        control.markAsDirty()
      } else if (control instanceof FormGroup) {
        this.markAsDirtied(control);
      }
    });
  }


  private base64ToBlob(b64Data, contentType='', sliceSize=512): Blob {
    b64Data = b64Data.replace(/\s/g, ''); //IE compatibility...
    let byteCharacters = atob(b64Data);
    let byteArrays = [];
    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
        let slice = byteCharacters.slice(offset, offset + sliceSize);
        let byteNumbers = new Array(slice.length);
        for (var i = 0; i < slice.length; i++) {
            byteNumbers[i] = slice.charCodeAt(i);
        }
        let byteArray = new Uint8Array(byteNumbers);
        byteArrays.push(byteArray);
    }
    return new Blob(byteArrays, {type: contentType});
  }

  downloadFile(nomeFile: string, mimeType: string, file: string) {
    var blob: Blob = this.base64ToBlob(file, mimeType);
    saveAs(blob, nomeFile);
  }

  getProtocolloPratica(pratica: PraticaDto): string {
    let ret = null;
    if (pratica.protocolli) {
      let protocollo: ProtocolloDTO = pratica.protocolli.find((x:ProtocolloDTO)=>x.idStatoPratica === environment.statiPratica.inserita);
      if (protocollo) {
        ret = protocollo.codiceProtocollo;
      }
    }
    return ret;
  }

  getUoConfigurations(): Observable<UoConfigurationDto[]> {
    return this.httpGet<UoConfigurationDto[]>('automi-protocollo-management/automi');
  }

  getUoConfiguration(id): Observable<UoConfigurationDto> {
    return this.httpGet<UoConfigurationDto>('automi-protocollo-management/automi/' + id);
  }

  updateUoConfiguration(configuration: UoConfigurationDto): Observable<any> {
    return this.httpPut<any>('automi-protocollo-management/automi', configuration);
  }

  caricaDashboardKibana(municipio_id: number): Observable<DashboardDTO> {
    return this.httpGet<DashboardDTO>(`stats/caricaDashboardKibana/${municipio_id || ''}`);
  }

  toTitleCase(str) {
    return str.replace(
      /\w\S*/g,
      function(txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
      }
    );
  }

  accapoMessage(string) {
    return string.split('.').join('.<br>');
  }

  protocolSortFunction = (v1, v2) => {
    const d1 = v1.getProtocollo;
    const d2 = v2.getProtocollo;
    if (d1 == null && d2 != null) return -1;
    else if (d1 != null && d2 == null) return 1;
    else if (d1 == null && d2 == null) return 0;

    let value1 = this.extractProtocolOrderValue(d1);
    let value2 = this.extractProtocolOrderValue(d2);
    if (typeof value1 === 'string' && typeof value2 === 'string') return value1.localeCompare(value2);
    else return value1 < value2 ? -1 : value1 > value2 ? 1 : 0;
  }

  extractProtocolOrderValue = (value) => {
    let p = value.split('|');
    if(p.length < 2) {
      return p[0];
    } else {
      const numeroString = p[0];
      const annoString = p[1];
      const numero = parseInt(numeroString);
      const anno = parseInt(annoString);
      if(typeof numero === 'number' && typeof anno === 'number') {
        return parseInt(`${anno}${numero}`);
      } else {
        return `${numero}${anno}`
      }
    }
  }

  getLastProtocol(protocolli: ProtocolloDTO[]) {
    if(protocolli !== null && protocolli.length !== 0) {
      protocolli.sort((a, b) => (parseInt(b.numeroProtocollo) - parseInt(a.numeroProtocollo)))
      return `${protocolli[0].numeroProtocollo}|${protocolli[0]?.anno}`;
    }
    return `--|--`;
  }
  getLastProtocolByDate(protocolli: ProtocolloDTO[]): string {
    if (protocolli !== null && protocolli.length !== 0) {
      protocolli.sort(
        (a, b) => {
          const dateA = new Date(a.dataProtocollo);
          const dateB = new Date(b.dataProtocollo);
          return dateB.getTime() - dateA.getTime();
        }
      );
      return `${protocolli[0].numeroProtocollo}|${protocolli[0]?.anno}`;
    }
    return `--|--`;
  }
}
