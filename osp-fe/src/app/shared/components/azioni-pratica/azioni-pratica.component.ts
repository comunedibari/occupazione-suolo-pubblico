import { Component, Input, OnInit } from '@angular/core';
import { GestionePraticaComponent } from '@features/gestione-pratica/gestione-pratica.component';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { PraticaService } from '@services/pratica.service';
import { UtilityService } from '@services/utility.service';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { ConfirmationService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { environment } from 'environments/environment';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from './models/action';
import { UserService } from '@services/user.service';
import { PraticaDto } from '@models/dto/pratica-dto';
import { GestionePraticaDialogConfig } from '@features/gestione-pratica/model/gestione-pratica-dialog-config';

@Component({
  selector: 'app-azioni-pratica',
  templateUrl: './azioni-pratica.component.html',
  styleUrls: ['./azioni-pratica.component.css']
})
export class AzioniPraticaComponent implements OnInit {

  @Input() tipoAzione: string;
  @Input() columnsSchema: ColumnSchema[];
  @Input() actions: Action[];
  @Input() initSortColumn: string = '';
  @Input() globalFilters: any[] = [];

  inputEvent : any;
  title: string = '';
  titleTable: string = '';

  private statoFilter: number;

  showSpinner: boolean = false;
  dataSource: any[];
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc 
  exportName: string = ''; 

  constructor(
    private praticaService: PraticaService,
    public authService: AuthService,
    private utilityService: UtilityService,
    private messageService: MessageService,
    public dialogService: DialogService,
    public confirmationService: ConfirmationService,
    private spinnerService: SpinnerDialogService,
    public userService: UserService,
  ) { }

  onTableEvent(tableEvent: TableEvent) {    
    this[tableEvent.actionKey](tableEvent.data);
  }

  ngOnInit(): void {
    if(this.tipoAzione == 'bozza'){
      this.titleTable = 'Pratiche in bozza';
      this.exportName = 'Pratiche in bozza'; 
      this.title = 'Pratiche in Bozza';
      this.statoFilter = environment.statiPratica.bozza;
    }

    if(this.tipoAzione == 'verificaFormale'){
      this.titleTable = 'Pratiche da validare';
      this.exportName = 'Pratiche da validare'; 
      this.title = 'Pratiche da validare';
      this.statoFilter = environment.statiPratica.verificaFormale;
    }

    //Aggiungere un Title appropriato
    if(this.tipoAzione == 'richiestaPareri'){
      this.titleTable = 'Pratiche';
      this.exportName = 'Pratiche'; 
      this.title = 'Pratiche';
      this.statoFilter = environment.statiPratica.richiestaPareri;
    }

    if(this.tipoAzione == 'necessariaIntegrazione'){
      this.titleTable = 'Necessaria integrazione';
      this.exportName = 'Necessaria integrazione'; 
      this.title = 'Necessaria integrazione';
      this.statoFilter = environment.statiPratica.necessariaIntegrazione;
    }

    if(this.tipoAzione == 'praticaDaRigettare'){
      this.titleTable = 'Pratiche da rigettare';
      this.exportName = 'Pratiche da rigettare'; 
      this.title = 'Pratiche da rigettare';
      this.statoFilter = environment.statiPratica.praticaDaRigettare;
    }

    if(this.tipoAzione == 'concessioneValida'){
      this.titleTable = 'Concessioni valide';
      this.exportName = 'Concessioni valide'; 
      this.title = 'Concessioni valide';
      this.statoFilter = environment.statiPratica.concessioneValida;
    }

    if(this.tipoAzione == 'approvata'){
      this.titleTable = 'Pratiche approvate';
      this.exportName = 'Pratiche approvate'; 
      this.title = 'Pratiche approvate';
      this.statoFilter = environment.statiPratica.approvata;
    }

    if(this.tipoAzione == 'preavvisoDiniego'){
      this.titleTable = 'Pratiche in preavviso diniego';
      this.exportName = 'Pratiche in preavviso diniego'; 
      this.title = 'Pratiche in preavviso diniego';
      this.statoFilter = environment.statiPratica.preavvisoDiniego;
    }

    if(this.tipoAzione == 'sospesa'){
      this.titleTable = 'Pratiche in sospeso';
      this.exportName = 'Pratiche in sospeso'; 
      this.title = 'Pratiche in sospeso';
      this.statoFilter = environment.statiPratica.attesaPagamento;
    }

    if(this.tipoAzione == 'prontaRilascio'){
      this.titleTable = 'Pratiche pronte al rilascio';
      this.exportName = 'Pratiche pronte al rilascio'; 
      this.title = 'Pratiche pronte al rilascio';
      this.statoFilter = environment.statiPratica.prontaRilascio;
    }

    if(this.tipoAzione == 'archiviata'){
      this.titleTable = 'Pratiche archiviate';
      this.exportName = 'Pratiche archiviate'; 
      this.title = 'Pratiche archiviate';
      this.statoFilter = environment.statiPratica.archiviata;
    }

    if(this.tipoAzione == 'rigettata'){
      this.titleTable = 'Pratiche rigettate';
      this.exportName = 'Pratiche rigettate'; 
      this.title = 'Pratiche rigettate';
      this.statoFilter = environment.statiPratica.rigettata;
    }

    this.cercaPratiche();
  }

  cercaPratiche(){
    let pratica = new RicercaPraticaRequest();
    pratica.idsStatiPratica = [];
    pratica.idsStatiPratica.push(this.statoFilter);
    
    this.spinnerService.showSpinner(true);
    this.praticaService.getPratiche(pratica, this.authService.getLoggedUser()).subscribe(
      data => {
        this.spinnerService.showSpinner(false);
        this.dataSource = data.content;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage('Ricerca pratiche', err); 
      });
  }

  dettaglioPratica(element: PraticaDto) {
    let config: GestionePraticaDialogConfig = {
      pratica: element,
      readonly: false
    }
    let dialogRef = this.dialogService.open(GestionePraticaComponent,  this.utilityService.configDynamicDialogFullScreen(config, "Inserisci richiesta"));
    dialogRef.onClose.subscribe((data: any) => {
      this.cercaPratiche();
    });  
  }

  deletingDialog($event){
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
        this.dataSource = this.dataSource.filter(x => x.id != idPratica);
      },
      error => {
        console.log(error);
        this.messageService.showErrorMessage('Elimazione pratica',error);
      }    
    );
  }


}
