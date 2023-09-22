import { Component, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { MessageService } from '@services/message.service';
import { environment } from 'environments/environment';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { UtilityService } from '@services/utility.service';
import { PraticaDto } from '@models/dto/pratica-dto';
import { AuthService } from '@services/auth/auth.service';
import { DialogService } from 'primeng/dynamicdialog';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { IntegrazioneDTO } from '@models/dto/integrazione-dto';
import { AllegatiService } from '@services/allegati.service';
import { AllegatoIntegrazioneInsertRequest } from '@models/allegato-integrazione-insert-request';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { DateTime } from 'luxon';

@Component({
  selector: 'app-necessaria-integrazione',
  templateUrl: './necessaria-integrazione.component.html',
  styleUrls: ['./necessaria-integrazione.component.css']
})
export class NecessariaIntegrazioneComponent extends BasePraticaAction implements OnInit {

  constructor(
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    private authService: AuthService,
    private praticaService: PraticaService,
    protected utilityService: UtilityService,
    private allegatiService: AllegatiService,
    protected dialogService: DialogService,
  ) {
    super(dialogService, utilityService);
  }

  showRettificaDate: boolean = false;

  destinazioneAllegato = DestinazioneAllegato.INTEGRAZIONE;
  uploadMode = Mode.MULTIPLE;

  uploadedFiles: AllegatoDTO[] = [];
  isNewDocUploaded: boolean = false;

  pratica: PraticaDto;
  showIntegrazioneDialog: boolean = false;

  noteRispostaDiniego: string = '';

  dataSource: PraticaDto[];
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable = 'Necessarie integrazioni';
  exportName = 'Necessarie integrazioni';
  title = 'Necessarie integrazioni';

  initSortColumn = 'dataModifica';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  dataInizioRettifica: Date;
  dataFineRettifica: Date;
  oraInizioRettifica: Date;
  oraFineRettifica: Date;
  limitData: Date = new Date();
  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;

  columnSchema: ColumnSchema[] = [
    {
      field: "firmatario.codiceFiscalePartitaIva",
      header: "Cod. Fiscale/P. IVA",
      type: "text"
    },
    {
      field: "datiRichiesta.ubicazioneOccupazione",
      header: "Indirizzo",
      inactive: true,
      type: "text"
    },
    {
      field: "municipio.id",
      header: "N. Municipio",
      type: "dropdown"
    },
    {
      field: 'protocollo',
      header: 'N. Protocollo',
      type: 'text',
      /*show: (el) => {
        return UtilityService.getProtocolloInserimento(el);
      },*/
      customSortFunction: this.utilityService.protocolSortFunction
    },
    {
      field: "tipoProcesso.descrizione",
      header: "Tip. Processo",
      type: "dropdown",
    },
    {
      field: "utentePresaInCarico.username",
      header: "Istruttore",
      type: "text",
    },
    {
      field: "statoPratica.descrizione",
      header: "Stato",
      type: "dropdown",
    },
    {
      field: "dataModifica",
      header: "Data operazione",
      type: "date",
      pipe: "date"
    }
  ];

  actions: Action[] = [
    {
      key: 'dettaglioPratica',
      icon: "pi pi-search",
      tooltip: 'DETTAGLIO',
    },
    {
      key: 'integrazioneDialog',
      icon: "pi pi-paperclip",
      tooltip: 'INTEGRAZIONE DOCUMENTI',
      hidden: (el) => {
        return el.statoPratica.id == environment.statiPratica.rettificaDate || this.authService.getGroup() == environment.groups.idGruppoDirettoreMunicipio;
      }
    },
    {
      key: 'rettificaDateDialog',
      icon: "pi pi-calendar-plus",
      tooltip: 'RETTIFICA DATE',
      hidden: (el) => {
        return el.statoPratica.id != environment.statiPratica.rettificaDate || this.authService.getGroup() == environment.groups.idGruppoDirettoreMunicipio;
      }
    }
  ];

  confirmDisabled: boolean = true;
  onMandatoryDocuments(event: boolean) {
    this.confirmDisabled = !event;
  }

  isConfirmDisabled(): boolean {
  let ret: boolean = (this.isPreavvisoDiniego() && (this.noteRispostaDiniego.trim().length === 0)) || this.confirmDisabled || this.uploadedFiles.length < 1;
    return ret;
  }

  isPreavvisoDiniego(){
    return this.pratica?.statoPratica.id == environment.statiPratica.preavvisoDiniego
  }

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche() {
    let praticaIntegrazione = new RicercaPraticaRequest();
    praticaIntegrazione.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaIntegrazione.idsStatiPratica = [];
    praticaIntegrazione.idsStatiPratica.push(environment.statiPratica.necessariaIntegrazione,
    environment.statiPratica.preavvisoDiniego, environment.statiPratica.rettificaDate);

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaIntegrazione, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  integrazioneDialog(element: any) {
    this.pratica = element;
    this.showIntegrazioneDialog = true;
  }

  closeIntegrazioneDialog(){
    this.showIntegrazioneDialog = false;
    this.noteRispostaDiniego = '';
    if(this.uploadedFiles.length > 0) {
      const promises = this.uploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.uploadedFiles = [];
      });
    }
  }

  onUploadedFile(event) {
    this.uploadedFiles.push(event);
  }

  aggiornaStatoPratica(){
    this.spinnerService.showSpinner(true);
    let lastRichiestaIntegrazione = this.pratica.richiesteIntegrazioni[this.pratica.richiesteIntegrazioni.length-1];

    let noteRichiesta: string = null;
    if(this.pratica.statoPratica.id == environment.statiPratica.preavvisoDiniego){
      noteRichiesta = this.noteRispostaDiniego;
    }

    this.praticaService.statoIntegrazione(lastRichiestaIntegrazione.id, this.authService.getLoggedUser().userLogged.id,
      noteRichiesta).subscribe(
      (data : IntegrazioneDTO) => {
        this.uploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.closeIntegrazioneDialog();
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La pratica è passata in stato Verifica formale');
        const parere = data;
        this.isProtocollata = !!parere.codiceProtocollo && !!parere.anno;
        this.numProtocollo = `${parere?.numeroProtocollo || '--'}|${parere?.anno || '--'}`;
        this.showProtocolloDialog = true;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  rettificaDateDialog(element: PraticaDto) {
    this.pratica = element;
    this.showRettificaDate = true;
  }

  inviaRettificaDate() {
    this.spinnerService.showSpinner(true);
    let lastRichiestaIntegrazione = this.pratica.richiesteIntegrazioni[this.pratica.richiesteIntegrazioni.length-1];
    let dataInizioForBE = this.utilityService.formatDateForBe('' + this.dataInizioRettifica);
    let dataFineForBE = this.utilityService.formatDateForBe('' + this.dataFineRettifica);
    let oraInizioForBE = this.utilityService.formatTimeForBe(this.oraInizioRettifica);
    let oraFineForBE = this.utilityService.formatTimeForBe(this.oraFineRettifica);

    this.praticaService.concludiRettificaDate(lastRichiestaIntegrazione.id, lastRichiestaIntegrazione.idUtenteRichiedente,
      dataInizioForBE, oraInizioForBE, dataFineForBE, oraFineForBE).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.closeRettificaDateDialog();
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La rettifica è stata eseguita con successo.');
        this.cercaPratiche();
        const parere = data;
        this.isProtocollata = !!parere.codiceProtocollo && !!parere.anno;
        this.numProtocollo = `${parere?.numeroProtocollo || '--'}|${parere?.anno || '--'}`;
        this.showProtocolloDialog = true;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
    });
  }


  orarioDisabled: boolean = false;

  onPeriodOccupazioneChange() {
    if (this.dataInizioRettifica && this.dataFineRettifica) {
      let days = this.getDateDiff(this.dataFineRettifica, this.dataInizioRettifica);
      if (days == 0) {
        this.orarioDisabled = true;
      } else {
        this.orarioDisabled = false
      }
    }
  }

  onOrarioChange() {
    if (this.oraInizioRettifica && this.oraFineRettifica && this.orarioDisabled == true) {
      this.orarioDisabled = false;
    }
  }

  calculateYearRange(): string {
    let min = new Date().getFullYear() - 100;
    let max = new Date().getFullYear();
    return min.toString() + ":" + max.toString();
  }

  private getDateDiff(date1: Date, date2: Date): number {
    let inizio = DateTime.fromISO(date1.toISOString()).startOf('day');
    let fine = DateTime.fromISO(date2.toISOString()).startOf('day');
    return fine.diff(inizio, 'days').values.days;
  }

  private getHourDiff(date1: Date, date2: Date): number {
    date1.setSeconds(0);
    date2.setSeconds(0);
    let inizio = DateTime.fromISO(date1.toISOString()).startOf('minute');
    let fine = DateTime.fromISO(date2.toISOString()).startOf('minute');
    return fine.diff(inizio, 'minutes').values.minutes;
  }

  validDateRange() {
      if (this.dataInizioRettifica && this.dataFineRettifica) {
        let days = this.getDateDiff(this.dataFineRettifica, this.dataInizioRettifica);
        if (days > 0) {
          return true;
        }
        return false;
      }
  }

  validOrariRange() {
      if (this.oraInizioRettifica && this.oraFineRettifica && this.dataInizioRettifica && this.dataFineRettifica) {
        let days = this.getDateDiff(this.dataFineRettifica, this.dataInizioRettifica);
        if (days == 0) {
          let minutes = this.getHourDiff(this.oraInizioRettifica, this.oraFineRettifica);
          if (minutes < environment.diffMininimaOreOccupazione ) {
            return true;
          }
          return false;
        }
      }
    }

  validPreviousInterval() {
    if (this.dataInizioRettifica) {
      let previousDateInizioOccupazione = new Date(`${this.pratica.datiRichiesta.dataInizioOccupazione}`);
      previousDateInizioOccupazione.setHours(0);
      let dataInizio = this.dataInizioRettifica;
      if(this.pratica.datiRichiesta.oraInizioOccupazione) {
        previousDateInizioOccupazione = new Date(`${this.pratica.datiRichiesta.dataInizioOccupazione} ${this.pratica.datiRichiesta.oraInizioOccupazione}`);
      }
      if(this.oraInizioRettifica) {
        dataInizio = new Date(`${this.dataInizioRettifica.toDateString()} ${this.oraInizioRettifica.toLocaleTimeString()}`);
      }
      let seconds = dataInizio.getTime() - previousDateInizioOccupazione.getTime();
      if (seconds >= 3600 * 1000) {
        return true;
      }
    }
    return false;
  }

  closeRettificaDateDialog () {
    this.showRettificaDate = false;
    this.dataInizioRettifica = null;
    this.dataFineRettifica= null;
    this.oraInizioRettifica= null;
    this.oraFineRettifica= null;
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }
}
