import { Component, OnInit } from '@angular/core';
import { HelperService } from '../../utils/services/helper.service';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent implements OnInit {

  constructor(public helperService: HelperService) { }

  ngOnInit(): void {
  }

  regresar(): void {
    this.helperService.onClickBack();
  }

}
