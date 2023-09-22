import { Component, OnInit } from '@angular/core';
import { Group } from '@enums/Group.enum';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { UtilityService } from '@services/utility.service';
import { UserService } from '@services/user.service';
import { ConfirmationService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { TableEvent } from '@shared-components/table-prime-ng/models/table-event';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { LoginInfo } from '@models/login/login-info';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { UtenteDTO } from '@models/utente-dto';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { TipologicheService } from '@services/tipologiche.service';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { ColumnSchema } from '@shared-components/table-prime-ng/models/column-schema';
import { Action } from '@shared-components/azioni-pratica/models/action';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  dataSource: any[];

  initSortColumn = 'nome';
  directionSortColumn = "1"; //1=asc  0=rand   -1=desc
  exportName = 'Lista Utenti';
  globalFilters: any[] = [
    {value:'nome', label:'Nome'},
    {value:'cognome', label:'Cognome'},
    {value:'ragioneSociale', label:'Denominazione'},
    {value:'email', label:'Email'},
    {value:'username', label:'Username'},
    {value:'idGruppo', label: 'Profilo'}
  ];
  tipoGruppi: GruppoDTO[];
  numeroMunicipi: TipologicaDTO[];
  uoConfigurations: any;

  showRagioneSociale = (value, data) => {
    const group = data.idGruppo;
    const uo = this.uoConfigurations[data.uoId]
    if(
      uo &&
      (
        group === environment.groups.idGruppoConcessionario ||
        group === environment.groups.idGruppoDestinatariOrdinanze
      )
    ) {
      return uo.denominazione
    } else {
      return value;
    }
  }

  columnSchema: ColumnSchema[] = [
    {
      field: "nome",
      header: "Nome",
      type:"text"
    },
    {
      field: "cognome",
      header: "Cognome",
      type:"text"
    },
    {
      field: "username",
      header: "Username",
      inactive: true,
      type:"text"
    },
    {
      field: "ragioneSociale",
      header: "Denominazione",
      type:"text",
      show: this.showRagioneSociale
    },
    {
      field: "email",
      header: "Email",
      type:"text"
    },
    {
      field: "idGruppo",
      header: "Profilo",
      type:"dropdown",
      show: (el) => {
        return this.getDescrizioneGruppo(el);
      }
    },
    {
      field: "idsMunicipi",
      header:"N. Municipio",
      type: "dropdown"
    },
    {
      field: "enabled",
      header: "Stato Utenza",
      type:"dropdown",
      show: (el) => {
        return el == true ? 'Abilitata' : 'Disabilitata';
      }
    }
  ];

  actions: Action[] = [
    {
      key: "modifyDialog",
      icon: "fa fa-pencil",
      tooltip: "MODIFICA UTENTE"

    },
    {
      key: "deletingDialog",
      icon: "fa fa-trash",
      tooltip: "ELIMINA UTENTE"
    }
  ];

  inserisciFeature: string = 'inserisciUtente';
  titleTable: string = 'Lista utenti a sistema';

  constructor(
    private userService: UserService,
    private utilityService: UtilityService,
    private authService: AuthService,
    private messageService: MessageService,
    public dialogService: DialogService,
    public confirmationService: ConfirmationService,
    private spinnerDialogService: SpinnerDialogService,
    private tipologicaService: TipologicheService
  ) { }

  ngOnInit(): void {
    this.spinnerDialogService.showSpinner(true);

    this.tipologicaService.getGruppi().subscribe(
      (data : GruppoDTO[]) =>{
        this.tipoGruppi = data;
      }
    );

    this.tipologicaService.getMunicipi().subscribe(
      (data : TipologicaDTO[]) =>{
        this.numeroMunicipi = data;
      }
    );

    this.utilityService.getUoConfigurations().subscribe(
      (res) => {
        this.uoConfigurations = res.reduce((prev, next) => {
          return {
            ...prev,
            [next.uoId]: next
          }
        }, {});
        this.getUsers();
      },
      (err) => {
        this.getUsers();
      }
    )
  }

  private getDescrizioneGruppo(id: number) : string{
    return  this.tipoGruppi.find(x => x.id == id).descrizione;
  }

  getUsers(){
    this.userService.getUsers().subscribe(data => {
      this.spinnerDialogService.showSpinner(false);

      let groupID = this.authService.getGroup();
      data.content = data.content.filter(x => x.idGruppo != environment.groups.idGruppoAdmin && x.idGruppo != environment.groups.idGruppoEGov);

      if(groupID != environment.groups.idGruppoAdmin && groupID != environment.groups.idGruppoEGov){
        data.content = data.content.filter(x => !this.utilityService.isInternalUser(x.idGruppo));
      }
      this.dataSource = data.content;
    });
  }

  onTableEvent(tableEvent: TableEvent) {
    this[tableEvent.actionKey](tableEvent.data);
  }

  inserisciUtente(){
     let dialogRef = this.dialogService.open(UserProfileComponent, this.utilityService.configDynamicDialog(null,"Registrazione nuovo utente"));
     dialogRef.onClose.subscribe( resp => {
      if(resp) {
        this.dataSource.push(resp);
        this.dataSource = [...this.dataSource];
      }
      this.getUsers();
    });
  }

  modifyDialog(user: LoginInfo){
    let dialogRef  = this.dialogService.open(UserProfileComponent, this.utilityService.configDynamicDialog(user,`Modifica dati utente: ${user.username}`));

    dialogRef.onClose.subscribe( msg => {
      if(msg == 'updated')
        this.dataSource = [...this.dataSource];
      else {
        this.spinnerDialogService.showSpinner(true);
        setTimeout(() => {
          this.getUsers();
        }, 1000);
      }
    });
  }

  deletingDialog(user: UtenteDTO){
    this.confirmationService.confirm({
      icon: "pi pi-exclamation-triangle",
      acceptLabel: "Conferma",
      rejectLabel: "Annulla",
      acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
      header: "Eliminazione utente",
      message: "Confermi di voler eliminare l'utente '" + user.username + "'?",
      accept: () => {
        this.deleteUser(user);
      }
  });
}

  deleteUser(user: UtenteDTO) {
    this.userService.eliminaUtente(user).subscribe(response => {
        this.messageService.showMessage('success','Elimazione utente',"Utente eliminato con successo.");
        this.dataSource = this.dataSource.filter(x => x.username != user.username);
      },
      error => {
        this.messageService.showErrorMessage('Elimazione utente', error);
      }
    );
  }


}
