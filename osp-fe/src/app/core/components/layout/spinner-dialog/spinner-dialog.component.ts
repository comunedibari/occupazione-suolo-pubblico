import { Component, OnInit } from '@angular/core';
import { SpinnerDialogService } from './services/spinner-dialog.service';

@Component({
  selector: 'app-spinner-dialog',
  templateUrl: './spinner-dialog.component.html',
  styleUrls: ['./spinner-dialog.component.css']
})
export class SpinnerDialogComponent implements OnInit {
  showSpinner: boolean = false;

  constructor(private spinnerService: SpinnerDialogService) { }

  ngOnInit(): void {
    this.spinnerService.onShowSpinner().subscribe(
      (show: boolean) => {
        setTimeout(() => {
          this.showSpinner = show;
        });
      }
    )
  }

}
