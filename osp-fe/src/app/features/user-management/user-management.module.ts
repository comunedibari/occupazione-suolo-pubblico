import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { UserManagementComponent } from './user-management.component';
import { SharedModule } from 'app/shared/shared.module';
import { PrimeNGModule } from 'app/shared/modules/primeng.module';
import { SpinnerDialogComponent } from '@core-components/layout/spinner-dialog/spinner-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';


@NgModule({
  declarations: [
    UserManagementComponent,
    UserProfileComponent,
  ],
  imports: [
    CommonModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,    
    SharedModule,
    PrimeNGModule
  ], 
  exports: [
    UserManagementComponent,
    UserProfileComponent
  ]

})
export class UserManagementModule { }
