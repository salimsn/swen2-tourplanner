import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Tour } from "../models/tour.model";

@Injectable({
  providedIn: "root"
})
export class TourService {

  private apiUrl = "http://localhost:8080/api/tours";

  constructor(private http: HttpClient) {}

  getTours() {
    return this.http.get<Tour[]>(this.apiUrl);
  }

  createTour(tour: Tour) {
    return this.http.post(this.apiUrl, tour);
  }

}
