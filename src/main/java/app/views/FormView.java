package app.views;

import app.models.Model;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.utils.ThrowingConsumer;
import app.utils.helpers.KinomichiFunctions;
import app.views.persons.ModifyPersonView;
import utils.helpers.Functions;
import utils.io.commands.exceptions.CommandResponseException;
import utils.io.tables.Table;

import java.util.HashMap;
import java.util.Scanner;

public abstract class FormView extends View {

    protected static void promptField(Scanner scanner, HashMap<FormViewField, FieldHandler> fieldHandlers, FormViewField field) throws CommandResponseException, UnimplementedFieldException {
        FieldHandler fieldHandler = fieldHandlers.get(field);
        if (fieldHandler != null) {
            System.out.println();
            System.out.println(fieldHandler.label);
            KinomichiFunctions.promptFieldWithExitAndBackCommands(scanner, fieldHandler.inputConsumer);
        } else {
            throw new UnimplementedFieldException(field);
        }
    }

    protected static Table getModelTable(Model model) {
        try {
            return ModelTableFormatter.forDetail(model);
        } catch (EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table détaillée du modèle car le modèle envoyé est nul"));
            return null;
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table détaillée du modèle car aucun ModelTable n'est défini pour ce modèle"));
            return null;
        } catch (ModelTableInstanciationException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table détaillée du modèle car l'instanciation du ModelTable a échoué"));
            return null;
        }
    }

    public record FieldHandler(String label, ThrowingConsumer<String> inputConsumer) {}

    public interface FormViewField {
    }

    protected static class UnimplementedFieldException extends Exception {

        private final FormViewField field;

        public UnimplementedFieldException(FormViewField field) {
            this.field = field;
        }

        public FormViewField getField() {
            return this.field;
        }

    }

}
