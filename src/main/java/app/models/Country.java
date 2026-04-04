package app.models;

import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.Validators;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;

import java.util.List;
import java.util.regex.Pattern;

public class Country extends Model implements CustomSerializable, CsvConvertible {

    public static final Pattern REGEX_ISO3_PATTERN = Pattern.compile("^[A-Z]{3}$");
    public static final Pattern REGEX_ISO2_PATTERN = Pattern.compile("^[A-Z]{2}$");

    // ─── Properties ─── //

    private String iso3;
    private String iso2;
    private String name;

    // ─── Getters ─── //

    public String getIso3() {
        return this.iso3;
    }

    public String getIso2() {
        return this.iso2;
    }

    public String getName() {
        return this.name;
    }

    // ─── Setters ─── //

    public void setIso3(String iso3) throws ModelException {
        this.iso3 = verifyIso3(iso3);
    }

    public void setIso2(String iso2) throws ModelException {
        this.iso2 = verifyIso2(iso2);
    }

    public void setName(String name) throws ModelException {
        try {
            this.name = Validators.validateNotNullOrEmpty(name);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelException("Le nom d'un pays ne peut pas être vide ou nul", e);
        }
    }

    // ─── Utility methods ─── //

    public static String verifyIsoBase(String value, int size) throws ModelException {
        if (size != 2 && size != 3) {
            throw new IllegalArgumentException("L'ISO d'un pays doit toujours faire 2 ou 3 caractères");
        }

        try {
            value = Validators.validateNotNullOrEmpty(value);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("L'ISO " + size + " d'un pays ne peut pas être vide ou nul", e);
        }

        if (value.length() != size) {
            throw new ModelVerificationException("L'ISO " + size + " d'un pays doit être long de " + size + " caractères");
        }

        value = value.toUpperCase();

        Pattern pattern = size == 2 ? REGEX_ISO2_PATTERN : REGEX_ISO3_PATTERN;

        if (!pattern.matcher(value).matches()) {
            throw new ModelVerificationException("L'ISO " + size + " d'un pays doit être composé de " + size + " lettres majuscules");
        }

        return value;
    }

    public static String verifyIso2(String value) throws ModelException {
        return verifyIsoBase(value, 2);
    }

    public static String verifyIso3(String value) throws ModelException {
        return verifyIsoBase(value, 3);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void parseLine(String[] columns) throws ParserException {
        if (columns.length != 3) {
            throw new StringParserException("Nombre d'arguments invalide (attendu: 3, obtenu: %s)".formatted(columns.length));
        }

        try {
            this.setIso3(columns[0]);
            this.setIso2(columns[1]);
            this.setName(columns[2]);
        } catch (ModelException e) {
            throw new ParserException(e);
        }
    }

    @Override
    public List<String[]> toLines() {
        return List.<String[]>of(new String[]{
                this.getIso3(),
                this.getIso2(),
                this.getName()
        });
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(this.name, this.iso3);
    }

    @Override
    public boolean isValid() {
        return this.iso3 != null && this.iso2 != null && this.name != null;
    }

}
