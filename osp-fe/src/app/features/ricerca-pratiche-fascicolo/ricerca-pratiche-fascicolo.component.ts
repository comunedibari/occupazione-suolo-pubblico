import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { DialogService } from 'primeng/dynamicdialog';
import { AuthService } from '@services/auth/auth.service';
import { PraticaDto } from '@models/dto/pratica-dto';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { Page } from '@models/page';
import { DettaglioPraticaDialogConfig } from '@features/dettaglio-pratica/model/dettaglio-pratica-dialog-config';
import { DettaglioPraticaComponent } from '@features/dettaglio-pratica/dettaglio-pratica.component';
import { environment } from 'environments/environment';
import { MappaComponent } from '@shared-components/mappa/mappa.component';
import { GestionePraticaDialogConfig } from '@features/gestione-pratica/model/gestione-pratica-dialog-config';
import { GestionePraticaComponent } from '@features/gestione-pratica/gestione-pratica.component';
import { CiviliarioResponse } from '@models/civiliario-response';
import { CiviliarioService } from '@services/civiliario.service';
import { TipologicheService } from '@services/tipologiche.service';
import { Observable } from 'rxjs';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';


@Component({
  selector: 'app-ricerca-pratiche-fascicolo',
  templateUrl: './ricerca-pratiche-fascicolo.component.html',
  styleUrls: ['./ricerca-pratiche-fascicolo.component.css']
})
export class RicercaPraticheFascicoloComponent implements OnInit {

  pratica: PraticaDto;
  dataSource: PraticaDto[];
  uploadedFiles: any[] = [];
  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;

  showNotificaRettificaDialog: boolean = false;
  noteNotificaRettifica: string = '';

  showDichiarazioniModificheStatoLuoghiDialog: boolean = false;
  selectedCondizioniTrasferimentoTitolarita: any[] = [];

  showDataFineLavoriDialog: boolean = false;
  data_scadenza_fine_lavori: any = null;

  optionsStatoPratica$: Observable<TipologicaDTO[]>;
  optionsTipoProcesso: TipologicaDTO[];

  //Autocomplete civico
  civilarioResults: CiviliarioResponse[] = [];

  private isAddressSelected: boolean;

  private mappaElements: PraticaDto[] = [];

  condizioniTrasferimentoTitolaritaSchema: any[] = [
    {
      label: 'Non sono intercorse modifiche dello stato dei luoghi',
      value: 'no_modifiche_stato_luoghi'
    },
    {
      label: "Non sono intercorse modifiche nella destinazione d'uso",
      value: 'no_modifiche_destinazione_uso'
    },
    {
      label: 'Non è tecnicamente possibile procedere alla regolarizzazione del passo carrabile ai sensi dell\'art. 46 D.P.R.495/1992',
      value: 'no_regolarizzazione'
    }
  ];

  initSortColumn = 'dataModifica';
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable: string = 'Lista pratiche cittadino';
  exportName = 'Pratiche Cittadino';
  globalFilters: any[] = [{ value: 'datiRichiesta.ubicazioneOccupazione', label: 'Indirizzo' }]

  statiVerificaOccupazione = [
    environment.statiPratica.verificaFormale,
    environment.statiPratica.richiestaPareri
  ];

  statiRettificaDate = [
    environment.statiPratica.inserita,
    environment.statiPratica.verificaFormale,
    environment.statiPratica.richiestaPareri,
  ];

  statiRettificaPratica = [
    environment.statiPratica.attesaPagamento,
    environment.statiPratica.prontaRilascio,
    environment.statiPratica.concessioneValida,
  ]

  columnSchema: ColumnSchema[] = [
    {
      field: "firmatario.codiceFiscalePartitaIva",
      header: "Cod. Fiscale/P. IVA",
      type: "text"
    },
    {
      field: "tipoProcesso.descrizione",
      header: "Tip. Processo",
      type: "dropdown",
    },
    {
      field: "datiRichiesta.ubicazioneOccupazione",
      header: "Indirizzo",
      inactive: true,
      type: "text"
    },
    {
      field: "datiRichiesta.idMunicipio",
      header: "N. Municipio",
      type: "dropdown"
    },
    {
      field: "statoPratica.descrizione",
      header: "Stato",
      type: "dropdown",
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
      field: "dataModifica",
      header: "Ultima modifica",
      type: "date",
      pipe: "date"
    },
  ];

  actions: Action[] = [
    {
      key: 'dettaglioPratica',
      icon: "pi pi-search",
      tooltip: 'DETTAGLIO',
    },
    {
      key: 'onApriMappaClicked',
      icon: "pi pi-map-marker",
      tooltip: 'VERIFICA OCCUPAZIONE',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio
        || el.datiRichiesta.coordUbicazioneDefinitiva != null
        || !this.statiVerificaOccupazione.includes(el.statoPratica.id);
      }
    },
    {
      key: 'confermaRettificaDateDialog',
      icon: "pi pi-calendar-plus",
      tooltip: 'AVVIA RETTIFICA DATE',
      hidden: (el) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio
        || !this.statiRettificaDate.includes(el.statoPratica.id) || el.tipoProcesso.id != environment.processes.concessioneTemporanea;
      }
    },
    {
      key: 'onAvviaRettificaClicked',
      icon: "pi pi-pencil",
      tooltip: 'AVVIA RETTIFICA PRATICA',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio
        || !this.statiRettificaPratica.includes(el.statoPratica.id);
      }
    },
  ];

  fascicoloForm: FormGroup;
  notePerCittadino: string = '';

  constructor(
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    private utilityService: UtilityService,
    public dialogService: DialogService,
    private praticaService: PraticaService,
    public authService: AuthService,
    private civilarioService: CiviliarioService,
    private tipologicheService: TipologicheService,
    private fb: FormBuilder
  ) { }

  showConfermaRettificaDate: boolean = false;

  ngOnInit(): void {
    this.fascicoloForm = this.fb.group({
      nome: [''],
      cognome: [''],
      denominazioneRagSoc: [''],
      codFiscalePIva: [''],
      numProtocollo: [''],
      numProvvedimento: [''],
      idStatoPratica: [null],
      indirizzo: [''],
      tipologiaProcesso: [null],
    });

    this.optionsStatoPratica$ = this.tipologicheService.getStatiPratiche();
    this.tipologicheService.getTipiProcesso().subscribe(data => {
      this.optionsTipoProcesso = data.filter(el => [1, 2, 3].includes(el.id));
    });
  }

  private getProtocollo(el: PraticaDto): string {
    let ret = this.utilityService.getProtocolloPratica(el);
    if (ret==null) {
      ret = "--"
    }
    return ret;
  }

  get isFormGroupEmpty() {
    return !this.fascicoloForm.get('nome').value
      && !this.fascicoloForm.get('cognome').value
      && !this.fascicoloForm.get('denominazioneRagSoc').value
      && !this.fascicoloForm.get('codFiscalePIva').value
      && !this.fascicoloForm.get('numProtocollo').value
      && !this.fascicoloForm.get('numProvvedimento').value
      && !this.fascicoloForm.get('idStatoPratica').value
      && this.fascicoloForm.get('idStatoPratica').value !== 0
      && !this.fascicoloForm.get('tipologiaProcesso').value
      && !this.isAddressSelected
      ? true : false;
  }

  cercaPratiche() {
    if(this.fascicoloForm.get('indirizzo').value !== '' && !this.isAddressSelected) {
      this.messageService.showErrorMessage('Ricerca indirizzo', 'Errore durante il ritrovamento delle pratiche');
      return;
    }
    this.spinnerService.showSpinner(true);
    let ricerca: RicercaPraticaRequest = new RicercaPraticaRequest();
    ricerca = this.fascicoloForm.value;
    ricerca.indirizzo = this.fascicoloForm.get('indirizzo').value.indirizzo;
    ricerca.idsMunicipi = this.authService.getMunicipiAppartenenza();

    this.praticaService.getPratiche(ricerca, this.authService.getLoggedUser()).subscribe(
      (data: Page) => {
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
        this.spinnerService.showSpinner(false);
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      }
    );
  }

  onTableEvent(tableEvent: TableEvent) {
    this[tableEvent.actionKey](tableEvent.data);
  }

  dettaglioPratica(element: PraticaDto) {
    let config: DettaglioPraticaDialogConfig = {
      pratica: element,
      isPraticaOrigine: false,
      showStorico: true
    }

    this.dialogService.open(DettaglioPraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Pratica cittadino"));
  }

  onApriMappaClicked(el): void {
    this.mappaElements.push(el);

    let data = {
      elements: this.mappaElements,
      fullScreen: true,
      enableHeatmap: true,
      showDetailButton: false
    }

    let dialogRef = this.dialogService.open(MappaComponent, this.utilityService.configDynamicDialogFullScreen(data, "Ubicazione pratica cittadino"));

    dialogRef.onClose.subscribe((element) => {
      this.mappaElements = [];
      this.cercaPratiche();
    });
  }

  confermaRettificaDateDialog(element: PraticaDto) {
    this.pratica = element;
    this.showConfermaRettificaDate = true;
  }

  avvioRettificaDate() {
    this.spinnerService.showSpinner(true);
    this.praticaService.inizioRettificaDate(this.pratica.id, this.authService.getLoggedUser().userLogged.id, this.notePerCittadino).subscribe(
      (data: RichiestaIntegrazioneDTO) => {
        this.spinnerService.showSpinner(false);
        this.showConfermaRettificaDate = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La richiesta è stata inviata con successo.');
        const pratica = data;
        this.isProtocollata = !!pratica.codiceProtocollo;
        this.numProtocollo = pratica.codiceProtocollo;
        this.showProtocolloDialog = true;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  onAvviaRettificaClicked(el): void {
    let config: GestionePraticaDialogConfig = {
      pratica: el,
      readonly: false,
      isAvviaRettifica: true
    }
    let dialogRef = this.dialogService.open(GestionePraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Avvia rettifica"));
    dialogRef.onClose.subscribe((data: any) => {
      this.cercaPratiche();
      //this.closeProcessiPostConcessioneDialog();
    });
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }

  onAddressSelect(event) {
    this.isAddressSelected = true;
  }

  handleAddressChange(event) {
    if(event.target.value === '') {
      this.isAddressSelected = false;
      this.fascicoloForm.get('indirizzo').reset('');
    };
  }

  clearFilter(tipologia){
    this.fascicoloForm.get(tipologia).patchValue(null);
  }

  addressLoseFocus(event) {
    if(!this.isAddressSelected) {
      this.fascicoloForm.get('indirizzo').reset('');
    }
  }

  searchCivilario(event) {
    if(event.query === '') {
      this.fascicoloForm.get('indirizzo').reset('');
    }
    this.isAddressSelected = false;
    return this.civilarioService.civico(event.query, this.authService.getMunicipio())
      .toPromise()
      .then((data: CiviliarioResponse[]) => {
        if(data && data.length) {
          this.civilarioResults = data.map(
            (dato: CiviliarioResponse) => {
              dato.indirizzo = this.civilarioService.addressListFormatter(dato);
              dato.municipio = dato.municipio?dato.municipio.replace(/\D/g, ''): null;
              return dato;
              /*
              return {
                indirizzo: this.addressListFormatter(dato),
                location: { lat: dato.lat, lon: dato.lon },
                municipio_id: dato.municipio ? parseInt(dato.municipio.replace(/\D/g, '')) : null,
                localita: dato.localita
              }*/
            });
        }
        else {
          this.civilarioResults = [];
          this.messageService.showMessage('warn', 'Ricerca indirizzo', 'Indirizzo non presente. Rivolgersi alla toponomastica per il censimento'); // o inserirlo manualmente
        }
    });
  }

}
