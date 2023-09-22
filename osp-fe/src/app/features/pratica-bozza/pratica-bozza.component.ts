import { Component, OnInit } from '@angular/core';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { PraticaDto } from '@models/dto/pratica-dto';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { PraticaService } from '@services/pratica.service';
import { AuthService } from '@services/auth/auth.service';
import { environment } from 'environments/environment';
import { MessageService } from '@services/message.service';
import { GestionePraticaDialogConfig } from '@features/gestione-pratica/model/gestione-pratica-dialog-config';
import { DialogService } from 'primeng/dynamicdialog';
import { GestionePraticaComponent } from '@features/gestione-pratica/gestione-pratica.component';
import { UtilityService } from '@services/utility.service';
import { ConfirmationService } from 'primeng/api';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';

@Component({
  selector: 'app-pratica-bozza',
  templateUrl: './pratica-bozza.component.html',
  styleUrls: ['./pratica-bozza.component.css']
})
export class PraticaBozzaComponent implements OnInit {
  dataSource: PraticaDto[];
  titleTable = 'Pratiche in bozza';
  exportName = 'Pratiche in bozza';
  initSortColumn = 'dataModifica';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc

  globalFilters: any[] = [
    {value:'firmatario.nome', label:'Nome'},
    {value:'firmatario.cognome', label:'Cognome'},
    {value: "firmatario.codiceFiscalePartitaIva", label: "Cod. Fiscale/P. IVA"},
    {value:'datiRichiesta.ubicazioneOccupazione',label:'Indirizzo'},
    {value:'municipio.id', label:'N. Municipio'}
  ]

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
      field: "tipoProcesso.descrizione",
      header: "Tip. Processo",
      type: "dropdown",
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
      key: 'deletingDialog',
      icon: "fa fa-trash",
      tooltip: 'ELIMINA',
    },
  ];

  constructor(
    private spinnerService: SpinnerDialogService,
    private praticaService: PraticaService,
    private authService: AuthService,
    private messageService: MessageService,
    private dialogService: DialogService,
    private utilityService: UtilityService,
    public confirmationService: ConfirmationService,
  ) { }


  ngOnInit(): void {
    this.cercaPratiche();
  }

  onTableEvent(tableEvent: TableEvent) {
    this[tableEvent.actionKey](tableEvent.data);
  }

  cercaPratiche(){
    let pratica = new RicercaPraticaRequest();
    pratica.idsMunicipi = this.authService.getMunicipiAppartenenza();
    pratica.idStatoPratica = environment.statiPratica.bozza;

    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(pratica, this.authService.getLoggedUser()).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content.map(el => Object.setPrototypeOf(el, PraticaDto.prototype));
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err);
      }
    );
  }

  dettaglioPratica(element: PraticaDto) {
    let config: GestionePraticaDialogConfig = {
      pratica: element,
      readonly: false,
    }
    if(element.tipoProcesso.id == environment.processes.rinunciaConcessione){
      config.isRinunciaConcessione = true;
    } else if(element.tipoProcesso.id == environment.processes.prorogaConcessioneTemporanea){
      config.isRichiestaProroga = true;
    }
    let dialogRef = this.dialogService.open(GestionePraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Inserisci richiesta"));
    dialogRef.onClose.subscribe(data => {
      this.cercaPratiche();
    });
  }

  deletingDialog($event) {
    this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      header: "Eliminazione pratica",
      message: "Confermi di voler eliminare la pratica?" ,
      accept: () => {

        this.deletePratica($event.id);
      }
    });
  }

  deletePratica(idPratica: number){
      this.praticaService.eliminaPratica(idPratica).subscribe(response => {
        this.messageService.showMessage('success','Elimazione pratica',"Pratica eliminata con successo.");
        this.cercaPratiche();
      },
      error => {
        console.log(error);
        this.messageService.showErrorMessage('Elimazione pratica',error);
      }
    );
  }
}


