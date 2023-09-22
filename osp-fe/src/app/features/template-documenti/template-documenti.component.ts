import { Component, OnInit } from '@angular/core';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { TemplateDTO } from '@models/dto/template-dto';
import { UtenteDTO } from '@models/utente-dto';
import { FormatDatePipe } from '@pipes/format-date.pipe';
import { AuthService } from '@services/auth/auth.service';
import { MessageService } from '@services/message.service';
import { TemplateService } from '@services/template.service';
import { UtilityService } from '@services/utility.service';
import { forkJoin} from 'rxjs';

@Component({
  selector: 'app-template-documenti',
  templateUrl: './template-documenti.component.html',
  styleUrls: ['./template-documenti.component.css']
})
export class TemplateDocumentiComponent implements OnInit {
  listaTemplate: TemplateDTO[] = [];
  //tipiTemplate: TipologicaDTO[];

  constructor(
    private spinerService: SpinnerDialogService,
    //private tipologicheService: TipologicheService,
    private templateService: TemplateService,
    private messageService: MessageService,
    private formateDatePipe: FormatDatePipe,
    private utilityService: UtilityService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadTemplates();
  }

  private loadTemplates() {
    this.spinerService.showSpinner(true);

    forkJoin([
      //this.tipologicheService.getTipoTemplate(),
      this.templateService.getAllTemplates()
    ]).subscribe(
      next=> {
        this.listaTemplate = next[0];
        /* Gestione template vuoti per primi: soppressa in fase di review
        this.tipiTemplate = next[0];
        let emptyTemplates: TemplateDTO[] = [];
        this.tipiTemplate.forEach(
          (elem: TipologicaDTO) => {
            if (!this.listaTemplate.find((x:TemplateDTO)=> x.tipoTemplate.id === elem.id)) {
              let template: TemplateDTO = new TemplateDTO();
              template.tipoTemplate = elem;
              emptyTemplates.push(template);
            }
          }
        );
        if (emptyTemplates.length > 0) {
          this.listaTemplate.concat(emptyTemplates);
        }*/
        this.spinerService.showSpinner(false);
      },
      err => {
        this.spinerService.showSpinner(false);
        this.messageService.showErrorMessage("Caricamento template", err);
      }

    );
  }

  getDataModifica(template: TemplateDTO) {
    return this.formateDatePipe.transform(template.dataModifica, false);
  }

  onDownloadClicked(template: TemplateDTO) {
    this.spinerService.showSpinner(true);
    this.templateService.getTemplate(template.tipoTemplate.id).subscribe(
      (data: TemplateDTO) => {
        this.utilityService.downloadFile(data.nomeFile, data.mimeType, data.fileTemplate);
        this.spinerService.showSpinner(false);
      },
      err => {
        this.spinerService.showSpinner(false);
        this.messageService.showErrorMessage("Errore durante il donwload del template", err);
      }
    );
  }

  async onUploadClicked(event, template: TemplateDTO) {
    this.spinerService.showSpinner(true);

    let file = event.target.files[0];
    template.nomeFile = file.name;
    template.mimeType = file.type;
    let utenteModifica: UtenteDTO = new UtenteDTO();
    utenteModifica.id = (this.authService.getLoggedUser()).userLogged.id;
    template.utenteModifica = utenteModifica;
    let blob:string = (await this.utilityService.convertFileToBase64(file)) as string;
    blob = blob.substring(blob.indexOf(",")+1);
    template.fileTemplate = blob;

    if (template.id) {
      this.templateService.updateTemplate(template).subscribe(
        (data: TemplateDTO) => {
          this.spinerService.showSpinner(false);
          this.messageService.showMessage('success','Upload File', 'Template caricato con successo');    
          this.loadTemplates();
        },
        err => {
          this.spinerService.showSpinner(false);
          this.messageService.showErrorMessage("Errore durante l'aggiornamento del template", err);
        }
      );
    }
    else {
      this.templateService.insertTemplate(template).subscribe(
        (data: TemplateDTO) => {
          this.spinerService.showSpinner(false);
          this.messageService.showMessage('success','Upload File', 'Template caricato con successo');   
          this.loadTemplates();
        },
        err => {
          this.spinerService.showSpinner(false);
          this.messageService.showErrorMessage("Errore durante l'inserimento del template", err);
        }
      );
    }
    console.log("onUploadClicked template: ", template);
  }

}
