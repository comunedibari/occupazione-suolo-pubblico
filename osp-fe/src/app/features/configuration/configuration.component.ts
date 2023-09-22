import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import { UoConfigurationDto } from '@models/dto/uo-configuration-dto';
import { MessageService } from '@services/message.service';
import { UtilityService } from '@services/utility.service';
import {forkJoin, Observable, Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.css']
})
export class UoConfigurationComponent implements OnInit, OnDestroy {

  private destroyed: Subject<void> = new Subject<void>();
  constructor(
    private utilityService: UtilityService,
    private messageService: MessageService,
    private fb: FormBuilder
  ) { }

  showSpinner: boolean = false;
  formGroup: FormGroup;
  private originalDataGroups = {};
  editingEnabled: boolean = false;

  ngOnInit(): void {
    this.initialize();
  }
  private initialize(): void {
    this.showSpinner = true;
    this.editingEnabled = false;
    this.utilityService.getUoConfigurations().subscribe(
      data => {
        this.originalDataGroups = data;
        const datagroups = data.reduce((prev, next) => {
          return {
            [next.id]: this.fb.group({
              id: [{value: next.id, disabled: false}, [Validators.required]],
              uoId: [{value: next.uoId, disabled: false}, [Validators.required]],
              label: [{value: next.label, disabled: false}, [Validators.required]],
              denominazione: [{value: next.denominazione, disabled: false}, [Validators.required]],
            }),
            ...prev
          };
        }, {});
        this.formGroup = this.fb.group(datagroups);
        this.formGroup.valueChanges.pipe(
          takeUntil(this.destroyed)
        ).subscribe(
          val => {
            this.updateEditing(val);
          }
        );
        this.showSpinner = false;
      },
      err => {
        this.showSpinner = false;
        this.messageService.showMessage('error','Configurazione parametri', "Errore durante il ritrovamento dei parametri di configurazione");
      });
  }

  updateConfiguration() {
    if (this.formGroup.invalid) {
      this.messageService.showMessage('error', `Campi obbligatori`, 'Compilare tutti i campi obbligatori');
    } else {
      const observables: Array<Observable<any>> = [];
      Object.keys(this.formGroup.controls).forEach((key) => {
        const configuration: UoConfigurationDto = {
          id: this.formGroup.controls[key].get('id').value,
          uoId: this.formGroup.controls[key].get('uoId').value,
          label: this.formGroup.controls[key].get('label').value,
          denominazione: this.formGroup.controls[key].get('denominazione').value,
        };
        let form = null;
        for (const originalDataGroupsKey in this.originalDataGroups) {
          if (configuration.id === this.originalDataGroups[originalDataGroupsKey].id) {
            form = this.originalDataGroups[originalDataGroupsKey];
            break;
          }
        }
        if (!this.equalsDataGroup(configuration, form)) {
          observables.push(this.utilityService.updateUoConfiguration(configuration));
        }
      });
      forkJoin(
        observables
      ).subscribe(
        resp => {
          this.messageService.showMessage('success',`Configurazione parametri`, 'Dati salvati correttamente.');
          this.initialize();
        },
        err => {
          this.messageService.showMessage('error', `Configurazione parametro.`, err?.error.message || err.message);
        }
      );
    }
  }
  getControl(fg: string, controlName: string): AbstractControl {
    return this.formGroup.get(fg).get(controlName);
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  private updateEditing(formValue): void {
    this.editingEnabled = false;
    for (const originalDataGroupsKey in this.originalDataGroups) {
      let form = null;
      for (const formValueKey in formValue) {
        if (formValue[formValueKey].id === this.originalDataGroups[originalDataGroupsKey].id) {
          form = formValue[formValueKey];
        }
      }
      if (form != null && this.originalDataGroups[originalDataGroupsKey] != null) {
        this.editingEnabled =
          this.editingEnabled ||
          !this.equalsDataGroup(form, this.originalDataGroups[originalDataGroupsKey]);
        if (this.editingEnabled) {
          break;
        }
      }
    }
  }
  private equalsDataGroup(g1, g2): boolean {
    if (g1 == null && g2 == null) {
      return true;
    } else if ((g1 == null && g2 != null) || (g1 != null && g2 == null)) {
      return false;
    }
    return (g1.uoId.toString() === g2.uoId.toString()) && (g1.denominazione.toString() === g2.denominazione.toString());
  }
}
