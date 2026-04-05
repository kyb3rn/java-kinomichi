package app.views.clubs;

import app.models.Address;
import app.models.Club;

public record ModifyClubFormData(Club modifiedClub, Address modifiedAddress) {}
