package app.views.persons;

import app.models.Affiliation;
import app.models.Person;

public record ModifyPersonFormData(Person person, Affiliation affiliation, boolean wasAffiliated, boolean isAffiliated) {}
