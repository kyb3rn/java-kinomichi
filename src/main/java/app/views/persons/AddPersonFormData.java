package app.views.persons;

import app.models.Affiliated;
import app.models.Person;

public record AddPersonFormData(Person person, Affiliated.Data affiliatedData) {}
