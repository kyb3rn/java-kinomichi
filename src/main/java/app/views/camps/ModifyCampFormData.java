package app.views.camps;

import app.models.Address;
import app.models.Camp;

public record ModifyCampFormData(Camp modifiedCamp, Address modifiedAddress) {}
