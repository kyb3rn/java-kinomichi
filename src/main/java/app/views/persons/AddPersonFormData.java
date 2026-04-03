package app.views.persons;

import app.models.Affiliation;
import app.models.Person;

public record AddPersonFormData(Person person, Affiliation affiliation) {}
