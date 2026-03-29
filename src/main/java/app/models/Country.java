package app.models;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParser;
import utils.data_management.parsing.StringParserException;

import java.util.ArrayList;
import java.util.List;
import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.TableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextStyle;

import java.util.regex.Pattern;

public class Country extends Model implements CustomSerializable, CsvConvertible {

    /** Properties **/

    private String name;
    private String iso2;
    private String iso3;

    /** Getters **/

    @TableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 1)
    public String getName() {
        return this.name;
    }

    @TableDisplay(name = "ISO 3166-1 alpha-2", format = @TableDisplayFormattingOptions(alignment = TextAlignement.CENTER), order = 2)
    public String getIso2() {
        return this.iso2;
    }

    @TableDisplay(name = "ISO 3166-1 alpha-3", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class, alignment = TextAlignement.CENTER), order = 3)
    public String getIso3() {
        return this.iso3;
    }

    /** Setters **/

    public void setName(String name) throws ModelException {
        if (name == null || name.isBlank()) {
            throw new ModelException("Le nom d'un pays ne peut pas être vide");
        }

        this.name = name.strip();
    }

    public void setIso2(String iso2) throws ModelException {
        if (iso2 == null || iso2.isBlank()) {
            throw new ModelException("L'ISO2 d'un pays ne peut pas être vide");
        }

        iso2 = iso2.strip();

        if (iso2.length() != 2) {
            throw new ModelException("L'ISO2 d'un pays doit être long de 2 caractères");
        }

        iso2 = iso2.toUpperCase();

        Pattern iso2Pattern = Pattern.compile("^[A-Z]{2}$");
        if (!iso2Pattern.matcher(iso2).matches()) {
            throw new ModelException("L'ISO2 d'un pays doit être composé uniquement de lettres");
        }

        this.iso2 = iso2;
    }

    public void setIso3(String iso3) throws ModelException {
        if (iso3 == null || iso3.isBlank()) {
            throw new ModelException("L'ISO3 d'un pays ne peut pas être vide");
        }

        iso3 = iso3.strip();

        if (iso3.length() != 3) {
            throw new ModelException("L'ISO3 d'un pays doit être long de 3 caractères");
        }

        iso3 = iso3.toUpperCase();

        Pattern iso3Pattern = Pattern.compile("^[A-Z]{3}$");
        if (!iso3Pattern.matcher(iso3).matches()) {
            throw new ModelException("L'ISO3 d'un pays doit être composé uniquement de lettres");
        }

        this.iso3 = iso3;
    }

    @Override
    public void parseLine(String[] columns) throws ParserException {
        if (columns.length != 3) {
            throw new StringParserException("Nombre d'arguments invalide (attendu: 3, obtenu: %s)".formatted(columns.length));
        }

        try {
            this.setName(columns[0]);
            this.setIso2(columns[1]);
            this.setIso3(columns[2]);
        } catch (ModelException e) {
            throw new ParserException(e);
        }
    }

    @Override
    public List<String[]> toLines() {
        return List.<String[]>of(new String[]{
                this.getName(),
                this.getIso2(),
                this.getIso3()
        });
    }

    @Override
    public boolean isValid() {
        return this.name != null && this.iso2 != null && this.iso3 != null;
    }

}
