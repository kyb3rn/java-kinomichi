package app.utils.menus;

import app.models.Model;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import utils.io.commands.Command;
import utils.io.commands.exceptions.UnhandledCommandException;
import utils.helpers.Functions;
import utils.io.tables.Table;
import utils.io.menus.OrderedMenu;

public abstract class ModelMenu<M extends Model> extends OrderedMenu {

    // ─── Properties ─── //

    protected Table generatedModelTable = null;

    // ─── Utility methods ─── //

    protected abstract Object commandHandler(String input, Command command) throws UnhandledCommandException;

    public boolean generateTable() {
        try {
            this.generateTableWithThrows();
            return true;
        } catch (EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car la liste est vide ou nul"));
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car aucun ModelTable n'est défini pour ce modèle"));
        } catch (ModelTableInstanciationException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'afficher la table listée des modèles car l'instanciation du ModelTable a échoué"));
        }

        return false;
    }

    public abstract void generateTableWithThrows() throws UnimplementedModelTableException, ModelTableInstanciationException, EmptyContentModelTableFormatterException;

    // ─── Overrides & inheritance ─── //

    @Override
    public void display() {
        if (this.generatedModelTable != null) {
            this.generatedModelTable.display();
        }
    }

}
