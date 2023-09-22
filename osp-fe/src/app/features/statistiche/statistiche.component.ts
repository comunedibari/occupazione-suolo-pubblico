import { Component, OnInit } from '@angular/core';
import { AuthService } from '@services/auth/auth.service';
import { UtilityService } from '@services/utility.service';
import { MessageService } from '@services/message.service';

@Component({
  selector: 'app-statistiche',
  templateUrl: './statistiche.component.html',
  styleUrls: ['./statistiche.component.css']
})
export class StatisticheComponent implements OnInit {
  
  showSpinner: boolean = false;
  kibanaDashboard: string = "";

  constructor(
    private utilityService: UtilityService,
    private messageService: MessageService,
    public authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.showSpinner = true;
    this.utilityService.caricaDashboardKibana(this.authService.getMunicipio()).subscribe(
      data => {
        this.showSpinner = false;
        this.kibanaDashboard = data.dashboard;
      },
      err => {
        this.showSpinner = false;
        this.messageService.showMessage('error','Caricamento dashboard', "Errore durante il caricamento della dashboard delle statistiche"); 
      });
  }

}
