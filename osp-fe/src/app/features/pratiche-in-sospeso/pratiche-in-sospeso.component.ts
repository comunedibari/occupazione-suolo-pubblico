import { Component, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { DestinazioneAllegato } from '@shared-components/upload-file/enums/destinazione-allegato.enum';
import { Mode } from '@shared-components/upload-file/enums/mode.enum';
import { environment } from 'environments/environment';
import { DialogService } from 'primeng/dynamicdialog';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { BasePraticaAction } from 'app/shared/classes/base-pratica-action';
import { AllegatiService } from '@services/allegati.service';


@Component({
  selector: 'app-pratiche-in-sospeso',
  templateUrl: './pratiche-in-sospeso.component.html',
  styleUrls: ['./pratiche-in-sospeso.component.css']
})
export class PraticheInSospesoComponent extends BasePraticaAction implements OnInit {
  dataSource: PraticaDto[];
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  titleTable = 'Pratiche in attesa di pagamento';
  exportName = 'Pratiche in attesa di pagamento';
  title = 'Pratiche in attesa di pagamento';
  initSortColumn = 'dataModifica';
  filtroStatoPratica = environment.statiPratica.attesaPagamento;
  destinazioneAllegato = DestinazioneAllegato.PRATICA;
  uploadMode = Mode.MULTIPLE;
  numProtocollo: string = '';
  showProtocolloDialog: boolean = false;
  isProtocollata: boolean = false;

  newUploadedFiles: AllegatoDTO[] = [];

  globalFilters: any[] = [
    {value:'firmatario.codiceFiscalePartitaIva', label:'Cod. Fiscale/P. IVA'},
    {value:'datiRichiesta.ubicazioneOccupazione', label:'Indirizzo'},
    {value:'municipio.id', label:'N. Municipio'},
    {value:'utentePresaInCarico.username', label:'Istruttore'}
  ];

  columnsSchema: ColumnSchema[] = [
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
      field: "tipoProcesso.descrizione",
      header: "Tipo Processo",
      type: "dropdown"
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
      key: 'uploadRicevute',
      icon: "pi pi-credit-card",
      tooltip: 'UPLOAD RICEVUTE PAGAMENTO',
      hidden: (el) => {
        return (
          this.authService.getGroup() != environment.groups.idGruppoOperatoreSportello &&
          this.authService.getGroup() != environment.groups.idGruppoIstruttoreMunicipio
        );
      }
    },
  ];

  pratica: PraticaDto = null;
  showUploadRicevuteDialog: boolean = false;

  constructor(
    private authService: AuthService,
    private spinnerService: SpinnerDialogService,
    private messageService: MessageService,
    protected dialogService: DialogService,
    protected utilityService: UtilityService,
    private praticaService: PraticaService,
    private allegatiService: AllegatiService
  ) {
    super(dialogService, utilityService);
   }

  ngOnInit(): void {
    this.cercaPratiche();
  }

  cercaPratiche(){
    this.spinnerService.showSpinner(true);
    let ricercaPratiche = new RicercaPraticaRequest();
    ricercaPratiche.idsMunicipi = this.authService.getMunicipiAppartenenza();
    ricercaPratiche.idsStatiPratica = [this.filtroStatoPratica];

    this.praticaService.getPratiche(ricercaPratiche, this.authService.getLoggedUser()).subscribe(
      data => {
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
        this.spinnerService.showSpinner(false);
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      }
    );
  }

  statoProntoRilascio(){
    this.spinnerService.showSpinner(true);
    this.praticaService.statoProntoRilascio(this.pratica.id, this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: PraticaDto) => {
        const uploadedFilesLength = this.newUploadedFiles.length;
        this.newUploadedFiles = [];
        this.spinnerService.showSpinner(false);
        this.showUploadRicevuteDialog = false;
        this.messageService.showMessage('success', 'Aggiornamento pratica', 'La pratica è passata in stato Pronto al Rilascio');
        if(data && data.protocolli) {
          const pratica = data;
          this.isProtocollata = pratica.protocolli.length > 0 ;
          this.numProtocollo = this.utilityService.getLastProtocol(pratica.protocolli);
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

  uploadRicevute(pratica: PraticaDto): void {
    this.pratica = pratica;
    this.showUploadRicevuteDialog = true;
  }

  confirmDisabled: boolean = true;

  isConfirmDisabled(): boolean {
    return this.confirmDisabled;
  }

  closeUploadRicevuteDialog(): void {
    this.showUploadRicevuteDialog = false;
    this.allegati.clear();
    if(this.newUploadedFiles.length > 0) {
      const promises = this.newUploadedFiles.map((allegato, index) => {
        return this.allegatiService.deleteAllegato(allegato.id).toPromise();
      });
      Promise.all(promises).finally(() => {
        this.newUploadedFiles = [];
      });
    }
  }

  onUploadedFile(event) {
    this.newUploadedFiles.push(event);
  }

  closeProtocolloDialog(event?) {
    this.showProtocolloDialog = false;
    this.isProtocollata = false;
    this.numProtocollo = '';
    this.cercaPratiche();
  }

  confermaUploadRicevute(): void {
    this.statoProntoRilascio();
    //this.cercaPratiche();
  }

  private allegati: Map<number, AllegatoDTO> = new Map();

  onDocUploaded(allegato: AllegatoDTO) {
    this.allegati.set(allegato.tipoAllegato.id, allegato);
  }

  onMandatoryDocuments(event: boolean) {
    this.confirmDisabled = !event;
  }
}
