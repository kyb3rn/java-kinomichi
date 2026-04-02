package app.views.camps;

import app.models.Address;
import app.models.Camp;

public record AddCampFormData(Camp.Data campData, Address.Data addressData) {}
