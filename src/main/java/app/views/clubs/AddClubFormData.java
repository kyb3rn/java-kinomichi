package app.views.clubs;

import app.models.Address;
import app.models.Club;

public record AddClubFormData(Club.Data clubData, Address.Data addressData) {}
