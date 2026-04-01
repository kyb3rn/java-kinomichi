package utils.io.menus;

import app.middlewares.Middleware;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Menu {

    // ─── Properties ─── //

    protected ArrayList<Middleware> middlewares = new ArrayList<>();
    protected final ArrayList<MenuOption> options = new ArrayList<>();
    private Supplier<MenuResponse> beforeUseHook;
    private Supplier<MenuResponse> beforeDisplayHook;
    private Supplier<MenuResponse> afterDisplayHook;
    private Supplier<MenuResponse> beforeInputHook;
    private Function<String, MenuResponse> afterEveryInputHook;
    private BiFunction<String, Integer, MenuResponse> afterValidInputHook;
    private Runnable beforeUseExitHook;

    // ─── Setters ─── //

    public void setBeforeUseHook(Supplier<MenuResponse> beforeUseHook) {
        this.beforeUseHook = beforeUseHook;
    }

    public void setBeforeDisplayHook(Supplier<MenuResponse> beforeDisplayHook) {
        this.beforeDisplayHook = beforeDisplayHook;
    }

    public void setAfterDisplayHook(Supplier<MenuResponse> afterDisplayHook) {
        this.afterDisplayHook = afterDisplayHook;
    }

    public void setBeforeInputHook(Supplier<MenuResponse> beforeInputHook) {
        this.beforeInputHook = beforeInputHook;
    }

    public void setAfterEveryInputHook(Function<String, MenuResponse> afterEveryInputHook) {
        this.afterEveryInputHook = afterEveryInputHook;
    }

    public void setAfterValidInputHook(BiFunction<String, Integer, MenuResponse> afterValidInputHook) {
        this.afterValidInputHook = afterValidInputHook;
    }

    public void setBeforeUseExitHook(Runnable beforeUseExitHook) {
        this.beforeUseExitHook = beforeUseExitHook;
    }

    // ─── Utility methods ─── //

    public abstract void display();

    protected MenuResponse beforeUse() {
        return (this.beforeUseHook != null) ? this.beforeUseHook.get() : null;
    }

    protected MenuResponse beforeDisplay() {
        return (this.beforeDisplayHook != null) ? this.beforeDisplayHook.get() : null;
    }

    protected MenuResponse afterDisplay() {
        return (this.afterDisplayHook != null) ? this.afterDisplayHook.get() : null;
    }

    protected MenuResponse beforeInput() {
        return (this.beforeInputHook != null) ? this.beforeInputHook.get() : null;
    }

    protected MenuResponse afterEveryInput(String input) {
        return (this.afterEveryInputHook != null) ? this.afterEveryInputHook.apply(input) : null;
    }

    protected MenuResponse afterValidInput(String input, int choice) {
        return (this.afterValidInputHook != null) ? this.afterValidInputHook.apply(input, choice) : null;
    }

    protected void beforeUseExit() {
        if (this.beforeUseExitHook != null) this.beforeUseExitHook.run();
    }

    protected MenuResponse askingValidInputLoopHandle(AtomicReference<String> validInput, AtomicReference<OrderedMenuOption> selectedOption) {
        Scanner scanner = new Scanner(System.in);

        MenuResponse hookResponse = this.beforeInput();
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        System.out.print("> ");
        String input = scanner.nextLine();

        hookResponse = this.afterEveryInput(input);
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        validInput.set(input);

        return null;
    }

    public MenuResponse use() {
        MenuResponse hookResponse;

        hookResponse = this.beforeUse();
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        hookResponse = this.beforeDisplay();
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        System.out.println();

        this.display();

        hookResponse = this.afterDisplay();
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        AtomicReference<OrderedMenuOption> selectedOption = new AtomicReference<>();
        AtomicReference<String> validInput = new AtomicReference<>();

        hookResponse = this.askingValidInputLoopHandle(validInput, selectedOption);
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        hookResponse = this.afterValidInput(validInput.get(), selectedOption.get().getOrder());
        if (hookResponse != null) {
            this.beforeUseExit();
            return hookResponse;
        }

        this.beforeUseExit();
        return new MenuResponse(selectedOption.get().getResponse());
    }

}
