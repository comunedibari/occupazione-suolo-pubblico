import { Component, OnInit } from '@angular/core';
import { LoginInfo } from '@models/login/login-info';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { UtilityService } from '@services/utility.service';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  //credenziali
  username: string = '';
  password: string = '';
  recuperaPasswordModal: boolean = false;
  recuperaPasswordSpinner: boolean = false;

  constructor(
    private authService: AuthService,
    private utilityService: UtilityService,
    private messageService: MessageService,
    private spinnerService: SpinnerDialogService
  ) {
  }

  ngOnInit(){
    this.authService.resetTokenAndStorage();
  }

  login() {
      this.spinnerService.showSpinner(true);
      this.authService.login(this.username, this.password).subscribe(
        (data: LoginInfo) => {
          this.spinnerService.showSpinner(false);
          if (data.auth) {
            this.authService.saveToken(data.token);
            this.authService.saveLoggedUser(data);
            this.utilityService.goHome();
          } 
        },
        err => {
          this.spinnerService.showSpinner(false);
          if(err.error.code == 'E25' || err.error.code == 'E26' || err.error.code == 'E27'){
            this.messageService.showErrorMessage("Login utente", err);
          } else {
            this.messageService.showErrorMessage("Errore", err);
          }
        }
      );
  }

  recuperaPasswordDialog(){
    this.recuperaPasswordModal = true;
  }

  recuperaPassword(){
    this.recuperaPasswordSpinner = true;
    this.authService.recuperoPassword(this.username).subscribe(
      data => {
        this.recuperaPasswordModal = false;
        this.recuperaPasswordSpinner = false;
        this.messageService.showMessage('success', 'Recupero password', "Richiesta recupero password effettuata con successo.");  
      },
      err => {
        this.recuperaPasswordSpinner = false;
        this.recuperaPasswordModal = false;
        this.messageService.showMessage('warn', 'Recupera password', err.error.message);
      }
    ); 
  }
}
