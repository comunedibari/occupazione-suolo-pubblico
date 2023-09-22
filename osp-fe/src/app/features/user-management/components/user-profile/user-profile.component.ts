import { Component, Inject, Input, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Group } from '@enums/Group.enum';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { LoginInfo } from '@models/login/login-info';
import { UtenteDTO } from '@models/utente-dto';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { TipologicheService } from '@services/tipologiche.service';
import { UserService } from '@services/user.service';
import { UtilityService } from '@services/utility.service';
import { environment } from 'environments/environment';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';
import { UoConfigurationDto } from '@models/dto/uo-configuration-dto';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: UtenteDTO = new UtenteDTO();
  txtInsertRePassword: string = '';
  txtOldPassword: string = '';
  showMunicipiDialog: boolean = false;
  newMunicipio: number;
  newMunicipi: number[];
  tipoMunicipi$:  Observable<TipologicaDTO[]>;
  tipoGruppi: GruppoDTO[];
  limitDataNascita: Date = new Date();
  tipoDenominazioneUo$: Observable<UoConfigurationDto[]>;
  confirmPasswordError: string
  passwordError: string

  private municipioChanged: boolean = false;


  @Input() editUser: UtenteDTO;

  get isEditMode(): boolean {
    return this.editUser != null;
  }

  get isMyProfile(): boolean {
    return this.router.url.endsWith('/user');
  }

  get isOperatoreSportello(): boolean {
    return this.authService.getGroup() == environment.groups.idGruppoOperatoreSportello;
  }

  get userRole(): string {
    let ret: string = "";
    if (this.user.idGruppo && this.tipoGruppi) {
      if (this.user.idGruppo == environment.groups.idGruppoAdmin) {
        ret = "Admin";
      } else {
        ret = this.tipoGruppi.find(x => x.id == this.user.idGruppo).descrizione;
      }
    }
    return ret;
  }

  get isPersonaGiuridica(): boolean {
    return this.user.ragioneSociale && this.user.ragioneSociale != '' ? true : false;
  }

  get isPoliziaLocale(): boolean{
    return (this.user.idGruppo == environment.groups.idGruppoPoliziaLocale);
  }

  get isSettoreUrbanizzazioniPrimarie(): boolean{
    return (this.user.idGruppo == environment.groups.idGruppoSettoreUrbanizzazioniPrimarie);
  }

  get isAdmin(): boolean {
    return (this.user.idGruppo == environment.groups.idGruppoAdmin);
  }

  get isConcessionario(): boolean {
    return (this.user.idGruppo == environment.groups.idGruppoConcessionario || this.user.idGruppo == environment.groups.idGruppoDestinatariOrdinanze);
  }

  constructor(
    private userService: UserService,
    private router: Router,
    private authService: AuthService,
    public utilityService: UtilityService,
    private messageService: MessageService,
    private dialogRef: DynamicDialogRef,
    private tipologicheService: TipologicheService,
    private spinnerService: SpinnerDialogService,
    private confirmationService: ConfirmationService,

    @Inject(DynamicDialogConfig) data: any) {
      this.editUser = data.data;
    }

    resetSeConcessionarioDestOrdinanze(){
    if (this.user.idGruppo == environment.groups.idGruppoConcessionario ||
       this.user.idGruppo == environment.groups.idGruppoDestinatariOrdinanze) {
        this.user.nome = null;
        this.user.cognome = null;
        this.user.sesso = null;
        this.user.dataDiNascita = null;
        this.user.luogoDiNascita = null;
        this.user.provinciaDiNascita = null;
        this.user.uoId = null;
    }
  }
  rimuoviRuoliDiNonCompetenza(indexes){
    //rimuovo ruolo Admin
    indexes.splice(indexes.indexOf(Group.Admin.toString()), 1);
  }

  ngOnInit() {
    this.tipologicheService.getGruppi().subscribe(
      (data: GruppoDTO[]) => {
        this.tipoGruppi = data.filter(x=> x.id != environment.groups.idGruppoEGov);
      }
    );
    this.tipoMunicipi$ = this.tipologicheService.getMunicipi();

    if (this.isMyProfile) {
      let loggedUser: LoginInfo = this.authService.getLoggedUser();
      this.userService.getUser(loggedUser.userLogged.id).subscribe(
        (response: UtenteDTO) => {
        this.user = response
        this.user.dataDiNascita = new Date(this.user.dataDiNascita);
      });
    }
    else {
      this.user = this.isEditMode ? this.editUser : new UtenteDTO();

      if(this.isPoliziaLocale || this.isSettoreUrbanizzazioniPrimarie){
        this.newMunicipi = this.user.idsMunicipi;
      } else{
        this.newMunicipio = this.user.idsMunicipi?  this.user.idsMunicipi[0] : null ;
      }

      if(this.user.dataDiNascita != undefined || null){
        this.user.dataDiNascita = new Date(this.user.dataDiNascita);
      }
    }
    this.tipoDenominazioneUo$ = this.utilityService.getUoConfigurations();
  }

  onCheckedGroup(event: any) {
    if(this.user.idGruppo != Group.OperatoreSportello &&
      this.user.idGruppo != Group.DirettoreMunicipio &&
      this.user.idGruppo != Group.IstruttoreMunicipio
    ) {
      this.setUoByGroup(this.user.idGruppo);
    }

    if(this.user.idGruppo == Group.OperatoreSportello
      || this.user.idGruppo == Group.DirettoreMunicipio
      || this.user.idGruppo == Group.IstruttoreMunicipio
      || this.user.idGruppo == Group.PoliziaLocale
      || this.user.idGruppo == Group.IVOOPPSettoreUrbanizzazioniPrimarie
    ) {
        this.showMunicipiDialog = true;
    }
  }

  submitDatiUtente(skipCheckConcessionario?: boolean): void {
    this.user.provinciaDiNascita = this.user.provinciaDiNascita?.toUpperCase() || '';
    let userClone = JSON.parse(JSON.stringify(this.user));
    userClone.dataDiNascita = this.utilityService.formatDateForBe(userClone.dataDiNascita);
    if(skipCheckConcessionario) {
      userClone.skipCheckConcessionario = true;
    } else {
      userClone.skipCheckConcessionario = false;
    }


    let observable: Observable<any> =
      this.isEditMode || this.isMyProfile ? this.userService.modifyUser(userClone)
        : this.userService.inserisciUtente(userClone);

    if(!this.isEditMode && !this.isMyProfile && this.user.password != this.txtInsertRePassword){
      this.messageService.showMessage('error',this.isEditMode || this.isMyProfile ? 'Modifica utente' : 'Inserisci utente','Attenzione: le due password non coincidono');
    }
    else {
      this.spinnerService.showSpinner(true);
      observable.subscribe(response => {
        this.spinnerService.showSpinner(false);
        if (!this.isMyProfile) {
            let event = this.isEditMode ? 'updated' : response;
            this.closeDialog(event);
        }
        this.messageService.showMessage('success', this.isEditMode || this.isMyProfile ? 'Modifica utente' : 'Inserisci utente',"Operazione eseguita correttamente.");
      }, (err) => {
        if(err.error.code === "E32") {
          let usernameList = err.error.message.split(', ');
          let message = usernameList.length < 2 ? "L'utente " : "Gli utenti "
          message += err.error.message;
          message += usernameList.length < 2 ? " è già abilitato come Concessionario. <br>" : " sono già abilitati come Concessionario. <br>"
          message += "Procedendo con l'operazione ";
          message += usernameList.length < 2 ? "verrà disabilitato. <br>" : "verranno disabilitati. <br>";
          message += "Procedere comunque con la creazione dell'utente Concessionario?";

          this.confirmationService.confirm({
            icon: "pi pi-exclamation-triangle",
            acceptLabel: "Conferma",
            rejectLabel: "Annulla",
            acceptButtonStyleClass: "btn-custom-style btn-dialog-confirm",
            rejectButtonStyleClass: "btn-custom-style btn-dialog-confirm",
            header: "Inserimento Concessionario",
            message,
            accept: () => {
              this.submitDatiUtente(true);
            }
          });
        } else {
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage(this.isEditMode || this.isMyProfile ? 'Modifica utente' : 'Inserisci utente',err);
        }
      });
    }
  }

  cambiaPasswordUtente(cambioPasswordForm: NgForm): void {
    if (this.txtOldPassword == this.user.password)
      this.messageService.showMessage('warn','Modifica password','La nuova password è uguale a quella precedente.');
    else {
      if (this.user.password != this.txtInsertRePassword)
        this.messageService.showMessage('error','Modifica password','Attenzione: le due password non coincidono');
      else if(this.user.password.length < 6) {
        this.messageService.showMessage('error','Modifica password','La lunghezza minima della password è di 6 caratteri.');
      } else {
        this.userService.cambiaPasswordUtente(this.user.username, this.user.password, this.txtOldPassword).subscribe(
          response => {
              this.messageService.showMessage('success','Modifica password',"La password è stata modificata con successo.");

              this.user.password = null;
              this.txtInsertRePassword = null;
              this.txtOldPassword = null;
              cambioPasswordForm.resetForm();
            },
            error => {
              this.messageService.showErrorMessage('Modifica password',error);
            }
        );
      }
    }
  }
  
  validatePassword(event) {
    if(this.user.password.length < 6) {
      this.passwordError = "La lunghezza minima della password è di 6 caratteri.";
    } else {
      this.passwordError = "";
    }
  }

  calculateCF() {
    this.user.provinciaDiNascita = this.user.provinciaDiNascita?.toUpperCase() || '';

    if(this.user.provinciaDiNascita.length == 2 && this.user.nome
        && this.user.cognome && this.user.sesso
        && this.user.dataDiNascita && this.user.luogoDiNascita) {
          this.user.codiceFiscale = this.utilityService.getCodiceFiscale(this.user);
          if(!this.user.codiceFiscale) {
            this.messageService.showMessage('warn', 'Codice fiscale', 'Attenzione: provincia e comune inseriti non corrispondono');
          }
        }
      else
        this.user.codiceFiscale = '';
  }

  closeDialog(event?) {
    this.dialogRef.close(event);
  }

  onHideMunicipiDialog() {
    if(this.municipioChanged){
      this.user.idsMunicipi = [];
      if(this.isPoliziaLocale || this.isSettoreUrbanizzazioniPrimarie){
        this.user.idsMunicipi = this.newMunicipi;
      } else {
        this.user.idsMunicipi.push(this.newMunicipio);
        this.setUoByGroup(null, this.newMunicipio);
      }
    }
  }

  setUoByGroup(groupId, municipioId?) {
    let automaId;
    if(municipioId) {
      automaId = {
        id: municipioId
      };
    } else {
      automaId = this.utilityService.groupToUoMapping[groupId];
    }
    if(automaId !== undefined) {
      this.tipoDenominazioneUo$.subscribe(res => {
        const automa = res.filter(el => el.id === automaId.id);
        this.user.uoId = automa[0].uoId;
        if(!this.isConcessionario) {
          this.user.ragioneSociale = automa[0].denominazione;
        }
      });
    } else {
      this.user.uoId = undefined;
      this.user.ragioneSociale = '';
    }
  }

  closeMunicipioDialog(event?){
    if(event == "annulla") {
      this.municipioChanged = false;
    }
    else {
      this.municipioChanged = true;
    }

    this.showMunicipiDialog = false;
  }

  //selectButton primeNG
  sessoUsers: any[] = [
    {label: "Maschio", code: "M"},
    {label: "Femmina", code: "F"}
  ]

  //yearRange per p-calendar
  calculateYearRange():string {
    let min = new Date().getFullYear()-100;
    let max = new Date().getFullYear()+1;
    return min.toString() + ":" + max.toString();
  }

  onEmailKeyup(event) {
    if(!this.isEditMode && !this.isMyProfile) {
      const splitted = event.target.value.split('@');
      if(splitted.length > 0) {
        this.user.username = splitted[0];
      }
    }
  }
  
  onNameKeyup(event?) {
    const id =  event.target.attributes.id.value;
    const value = event.target.value;
    switch(id) {
      case("txtinputNome"): {
        this.user.nome = this.utilityService.toTitleCase(value);
        break;
      }
      case("txtinputCognome"): {
        this.user.cognome = this.utilityService.toTitleCase(value);
        break;
      }
    }
  }
}
