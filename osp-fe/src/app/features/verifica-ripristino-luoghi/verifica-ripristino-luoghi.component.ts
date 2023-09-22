import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';
import { MessageService } from '@services/message.service';
import { AuthService } from '@services/auth/auth.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { UtilityService } from '@services/utility.service';
import { environment } from 'environments/environment';
import { PraticaDto } from '@models/dto/pratica-dto';
import { DialogService } from 'primeng/dynamicdialog';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { TemplateDTO } from '@models/dto/template-dto';
import { TemplateService } from '@services/template.service';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { ParereDTO } from '@models/dto/parere-dto';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { ParereInsertRequest } from '@models/parere-insert-request';
import { AllegatiService } from '@services/allegati.service';


@Component({
  selector: 'app-verifica-ripristino-luoghi',
  templateUrl: './verifica-ripristino-luoghi.component.html',
  styleUrls: ['./verifica-ripristino-luoghi.component.css']
})
export class VerificaRipristinoLuoghiComponent extends BasePraticaAction implements OnInit {

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private templateService: TemplateService,
    private authService: AuthService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
  }

  pratica: PraticaDto;
  idSelectedPratica: number;

  showEsprimiParereDialog: boolean = false;

  destinazioneAllegato = DestinazioneAllegato.PARERE;
  uploadMode = Mode.MULTIPLE;
  idGruppoDestinatarioParere: number = null;

  dataSource: PraticaDto[];
  directionSortColumn = "-1"; //1=asc  0=rand   -1=desc
  titleTable = 'Verifica ripristino luoghi';
  exportName = 'Verifica ripristino luoghi';
  title = 'Verifica ripristino luoghi';

  initSortColumn = 'dataModifica';
  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva',label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'tipoProcesso.descrizione',label:'Tip. Processo'},
    {value:'utentePresaInCarico.username',label:'Istruttore'}
  ];

  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;

  newUploadedFiles: AllegatoDTO[] = [];
  isFileInCheckout: boolean = false;

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
      key: 'esprimiParereDialog',
      icon: "pi pi-comment",
      tooltip: 'ESPRIMI PARERE',
      hidden: (el: PraticaDto) => {
        return this.authService.getGroup() != environment.groups.idGruppoPoliziaLocale
      }
    },
  ];

  docsMap: Map<number, AllegatoDTO> = new Map<number, AllegatoDTO>();
  onDocUploaded(event: AllegatoDTO): void {
    this.docsMap.set(event.tipoAllegato.id, event);
  }
  onDocRemoved(event: number): void {
    this.docsMap.delete(event);
  }

  controlloNote(control: AbstractControl): { [key: string]: boolean } | null {
    if (!control.get('nota').value) {
      return {'controlloNoteRequired': true};
    }
    return null;
  }

  validParere(control: AbstractControl): { [key: string]: boolean } | null {
    if (control.value === undefined) {
      return { 'validParere': true };
    }
    return null;
  }

  isConfirmDisabledControl(): boolean {
    let note : string = this.esprimiParereForm.get('nota').value;
    if(note?.trim().length > 0 && !this.confirmDisabled ){
      return false || this.isFileInCheckout;
    }

    return true;
  }

  esprimiParereForm = new FormGroup({
    'nota': new FormControl(null),
    'parere': new FormControl(undefined, [Validators.required, this.validParere]),
    'destinatariOrdinanza': new FormControl(null)
  }, { validators: [this.controlloNote] });

  closeEsprimiPareriDialog() {
    this.esprimiParereForm.reset();
    this.showEsprimiParereDialog = false;

    if(this.newUploadedFiles.length > 0) {
      const promises = this.newUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newUploadedFiles = [];
      });
    }
  }

  parere: any[] = [
    { label: "Positivo", value: true, disabled: false },
    { label: "Negativo", value: false, disabled: false },
    { label: "Non di competenza", value: 3, disabled : false }
  ];

  generaTemplateElaborato(tipoTemplate: number): void {
    let testoNota : string = this.esprimiParereForm.get('nota').value;

    this.templateService.getTemplateElaborato(this.idSelectedPratica, tipoTemplate, testoNota).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
      },
      err => {this.messageService.showErrorMessage("Template determina", err);}
    );
  }

  getTipoTemplate(): number{
    return environment.template.relazioneDiServizio;
  }

  ngOnInit(): void {
    this.idGruppoDestinatarioParere = this.authService.getLoggedUser().groups.id;
    this.cercaPratiche();
  }

  confirmDisabled: boolean = true;
  onMandatoryDocuments(event: boolean) {
    this.confirmDisabled = !event;
  }


  cercaPratiche() {
    let praticaRichiestaPareri = new RicercaPraticaRequest();
    praticaRichiestaPareri.richiestaVerificaRipristinoLuoghi = true;
    praticaRichiestaPareri.idsMunicipi = this.authService.getMunicipiAppartenenza();
    praticaRichiestaPareri.idsStatiPratica = [
      environment.statiPratica.terminata, environment.statiPratica.rinunciata,
      environment.statiPratica.revocata, environment.statiPratica.decaduta, environment.statiPratica.annullata
    ];

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(praticaRichiestaPareri, this.authService.getLoggedUser()).subscribe(
      (data) => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      });
  }

  esprimiParereDialog(element: any) {
    this.pratica = element;
    this.idSelectedPratica = this.pratica.id;
    this.esprimiParereForm.get('parere').patchValue(false);
    this.showEsprimiParereDialog = true;
  }

  statoPostConcessione = [
    environment.statiPratica.decaduta,
    environment.statiPratica.revocata,
    environment.statiPratica.annullata,
    environment.statiPratica.terminata,
    environment.statiPratica.rinunciata
  ]


  esprimiParere() {
    this.spinnerService.showSpinner(true);
    let idUtenteParere = this.authService.getLoggedUser().userLogged.id;
    let testoNota : string = this.esprimiParereForm.get('nota').value;
    let esitoParere = this.esprimiParereForm.get('parere').value;
    if(esitoParere == 3){
      esitoParere = null;
    }

    let richiestaParere = this.pratica.richiestePareri.find(
      (x: RichiestaParereDTO) => {
        return x.flagInseritaRisposta === false && x.idGruppoDestinatarioParere === this.idGruppoDestinatarioParere;
      }
    );

    let parereReq: ParereInsertRequest = new ParereInsertRequest();

    parereReq.parere = new ParereDTO();
    parereReq.parere.idRichiestaParere = richiestaParere.id;
    parereReq.parere.idUtenteParere = idUtenteParere;
    parereReq.parere.nota = testoNota;
    parereReq.parere.esito = esitoParere;

    this.praticaService.esprimiPareri(parereReq, true).subscribe(
      (data: ParereDTO) => {
        const uploadedFilesLength = this.newUploadedFiles.length;
        this.newUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.closeEsprimiPareriDialog();
        this.messageService.showMessage('success', 'Esprimi parere', 'Il parere è stato inviato con successo');
        if(uploadedFilesLength > 0) {
          const parere = data;
          this.isProtocollata = !!parere;
          this.numProtocollo = `${parere?.codiceProtocollo || '--|--'}`;
          this.showProtocolloDialog = true;
        } else {
          this.closeProtocolloDialog();
        }
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Aggiornamento pratica', err);
        this.numProtocollo = '--|--';
        this.showProtocolloDialog = true;
      });
  }

  showUtentiOrdinanza(): boolean {
    let ret: boolean = false;
    this.docsMap.forEach((val, key) => {
        if (environment.tipologiaAllegati.ordinanza === key) {
          ret = true;
        }
    });

    return ret;
  }

    closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }

  onUploadedFile(event) {
    this.newUploadedFiles.push(event);
  }

  fileInCheckout = (status_checkout) => {
    this.isFileInCheckout = status_checkout;
  }

}
