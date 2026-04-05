> Dernière mise à jour : 2026-04-05 20:45

# Projet

Application CLI d'administration pour le Kinomichi. Java 21, Maven, terminal ANSI.

Packages principaux :
- `app/` — code applicatif (models, controllers, views, events, middlewares, routing, utils)
- `utils/` — bibliotheque reutilisable (I/O, formatage, data management, time)

# Architecture

## Boucle principale (Main.java)

`Main.java` contient la boucle centrale de l'application :

1. Initialise les `DataManagers` via `DataManagers.initAll()` qui appelle `initAndResolveReferencesOf(...)` (pass 1 + pass 2)
2. Charge les commandes via `CommandManager.loadCommands()`
3. Instancie un `Router` et enregistre toutes les `Route` (nom + regex de path + action de controller)
4. Boucle : `push path dans NavigationHistory` → `router.dispatch(path)` → recoit un `Event` → determine le prochain path (ou `null` pour quitter)
5. A la sortie de la boucle : `DataManagers.exportAll()` sauvegarde automatiquement tous les DataManagers ayant des modifications non enregistrees

Le switch sur les events :
- `CallUrlEvent` → navigue vers l'URL contenue
- `GoBackEvent` → `AppState.navigationHistory.goBack()`
- `GoBackBackEvent` → `AppState.navigationHistory.goBackUntilDifferentRoute(router)` (remonte jusqu'a une route differente)
- `ExitProgramEvent` → `null` (sort de la boucle)

## Routing (package `app.routing`)

- `Router` : registre de `Route`, matche un path contre les regex enregistrees, dispatch vers le controller
- `Route` : associe un nom (`{domaine}.{action}`), un pattern regex de path, une `ControllerAction`, et des `Middleware` optionnels
- `Request` : resultat d'un match, contient la route matchee + parametres nommes (groupes regex) + parametres indexes
- `ControllerAction` : interface fonctionnelle `(Request) -> Event`
- `RouteNotFoundException` : lancee si aucune route ne matche le path

Convention de nommage des routes : `{domaine}.{action}` (ex : `"camps.list"`, `"camps.add"`, `"camps.manage.camp"`, `"data_managers.reinit"`). Validee par regex `[a-z_]+(\.[a-z_]+)*`.

Parametres extraits des groupes regex nommes dans le path (ex : `(?<sort>.+)`, `(?<id>\\d+)`, `(?<manager>.+)`), accessibles via `request.getParameter("nom")`.

## Events (package `app.events`)

Les events sont le mecanisme de retour entre les views/controllers et la boucle principale :

- `Event` (abstract) — classe de base
- `CallUrlEvent(String url)` — navigue vers une URL
- `GoBackEvent` — revient en arriere dans l'historique
- `GoBackBackEvent` — remonte dans l'historique jusqu'a une route differente de la courante (declenche par `!bb`)
- `ExitProgramEvent` — quitte l'application
- `FormResultEvent<T>(T result)` — transporte le resultat d'un formulaire (ex : `AddCampFormData`, un `Integer` pour un ID selectionne)

## Controllers (package `app.controllers`)

- `Controller` (abstract) — classe de base, fournit `parseSortParameter(Request)` et `sortModels(...)`
- Chaque controller a des methodes publiques `Event xxx(Request request)` referencees comme `ControllerAction`
- Pattern typique d'une action `list` :
  1. Recuperer le DataManager via `DataManagers.get(XxxDataManager.class)` (catch → GoBackEvent)
  2. Parser les sort params depuis la request
  3. Trier les modeles
  4. Creer et rendre une `View`, retourner l'`Event` resultat

Controllers existants : `MainController`, `ExploreController`, `PersonController`, `CampController`, `ClubController`, `AffiliationController`, `DinnerController`, `LodgingController`, `SessionController`, `DataManagerController`.

## Views (package `app.views`)

- `View` (abstract) — classe de base, methode abstraite `Event render()`
- `FormView` (abstract, extends View) — base pour les formulaires, fournit `promptField(Scanner, HashMap<FormViewField, FieldHandler>, FormViewField)` et `getModelTable(Model)`. Inner record `FieldHandler(String label, ThrowingConsumer<String> inputConsumer)`, inner interface `FormViewField` (marqueur pour les enums de champs)
- `ModelView<M>` (extends View) — vue generique de detail d'un modele, utilise `ModelDetailMenu`
- `ModelListView<M>` — vue generique de liste tabulaire pour n'importe quel Model
- Les views construisent des menus (`KinomichiStandardMenu`, `ModelListMenu`, `ModelDetailMenu`) ou gerent des formulaires via `FormView`
- `render()` retourne un `Event` qui remonte au controller puis a la boucle principale

Views existantes :
- `MainView` — menu principal avec options dynamiques selon l'etat des DataManagers
- `ExploreDataView` — menu d'exploration de toutes les donnees
- `persons/` — `PersonsDashboardView`, `AddPersonView` + `AddPersonFormData`, `ModifyPersonView` + `ModifyPersonFormData`, `SelectPersonView`, `DeletePersonView`
- `camps/` — `SelectCampView`, `ManageCampView`, `AddCampView` + `AddCampFormData`, `ModifyCampView` + `ModifyCampFormData`, `DeleteCampView`
- `clubs/` — `ClubsDashboardView`, `AddClubView` + `AddClubFormData`, `ModifyClubView` + `ModifyClubFormData`, `SelectClubView`, `DeleteClubView`
- `affiliations/` — `AffiliationsDashboardView`, `AddAffiliationView`, `ModifyAffiliationView`, `SelectAffiliationView`, `DeleteAffiliationView`
- `dinners/` — `ManageDinnersView`, `AddDinnerView`, `ModifyDinnerView` + `ModifyDinnerFormData`, `SelectDinnerView`, `DeleteDinnerView`, `ManageDinnerReservationsView`, `AddDinnerReservationView`, `DeleteDinnerReservationView`
- `lodgings/` — `ManageLodgingsView`, `AddLodgingView`, `ModifyLodgingView` + `ModifyLodgingFormData`, `SelectLodgingView`, `DeleteLodgingView`, `ManageLodgingReservationsView`, `AddLodgingReservationView`, `DeleteLodgingReservationView`
- `invitations/` — `ManageInvitationsView`, `AddInvitationView`, `DeleteInvitationView`
- `sessions/` — `ManageSessionsView`, `AddSessionView`, `ModifySessionView` + `ModifySessionFormData`, `SelectSessionView`, `DeleteSessionView`, `ManageSessionTrainersView`, `AddSessionTrainerView`, `DeleteSessionTrainerView`, `ManageSessionRegistrationsView`, `AddSessionRegistrationView`, `DeleteSessionRegistrationView`
- `data_managers/` — `ReInitDataManagersView`, `SaveDataManagersView`

Pattern CRUD complet des vues par domaine : `Dashboard` (menu), `Add` (formulaire), `Modify` (select → formulaire), `Delete` (select → confirmation), `Select` (liste avec selection par ID). Les `SelectView` retournent un `FormResultEvent<Integer>` avec l'ID selectionne.

Les domaines camp-scoped (dinners, lodgings, sessions) suivent un pattern supplementaire : un `ManageXxxView` (menu) accessible depuis `ManageCampView`, un `SelectXxxView` pour naviguer vers le sous-domaine (reservations, trainers, registrations), et des vues `ManageXxxYyyView` pour le CRUD du sous-domaine.

## Middleware (package `app.middlewares`)

- `Middleware` (abstract) — methode abstraite `CallUrlEvent verify()`
- `verify()` retourne `null` (passe) ou un `CallUrlEvent` (redirection)
- Attaches a une `Route` via son constructeur, executees par le `Router` avant le dispatch au controller

## Systeme de menus (packages `utils.io.menus` + `app.utils.menus`)

Hierarchie des menus (dans `utils/`) :
- `Menu` (abstract) — base, gere les hooks (`beforeUse`, `beforeDisplay`, `afterDisplay`, `beforeInput`, `afterEveryInput`, `afterValidInput`, `beforeUseExit`), methode `use()` qui orchestre le cycle complet et retourne `MenuResponse`
- `OrderedMenu` (abstract, extends Menu) — menu a choix numerotes, gere la boucle d'input avec parsing des commandes, validation numerique, `CommandHandler`, `unoptionedRows`, `sectionSeparationIndexes`
- `StandardMenu` (extends OrderedMenu) — rendu en table avec titre colore (magenta bold), options "Retour" et "Quitter" ajoutees automatiquement dans `beforeDisplay()` (protege par flag `navigationOptionsAdded` contre les appels multiples)

Surcouches applicatives (dans `app/utils/menus/`) :
- `KinomichiStandardMenu` (extends StandardMenu) — preconfigure le `CommandHandler` pour mapper `BackCommand` → `GoBackEvent`, `ExitCommand` → `ExitProgramEvent`
- `ModelMenu<M>` (abstract, extends OrderedMenu) — base pour les menus qui affichent des modeles en table, gere la generation de table via `ModelTableFormatter` et le rendu dans `display()`
- `ModelListMenu<M>` (extends ModelMenu) — affiche une liste de modeles, gere la commande `!sort`
- `ModelDetailMenu<M>` (extends ModelMenu) — affiche le detail d'un seul modele, gere `BackCommand` → `GoBackEvent`, `BackBackCommand` → `GoBackBackEvent`, `ExitCommand` → `ExitProgramEvent`

`MenuResponse` encapsule la reponse (un `Event`, une `Command`, ou autre objet) qui remonte au code appelant.

## Systeme de commandes (package `utils.io.commands`)

Les commandes utilisateur sont prefixees par `!` (ex : `!e`, `!b`, `!sort 2`).

Commandes disponibles (enum `ECommand`) :
- `EXIT` (`!exit`, `!e`) → `ExitCommand`
- `BACK` (`!back`, `!b`) → `BackCommand`
- `BACK_BACK` (`!backback`, `!bb`) → `BackBackCommand` (remonte jusqu'a une route differente)
- `SORT_COLUMN` (`!sort`, `!s`) → `SortColumnCommand` (accepte des arguments : `!sort 2:DESC 3`)

Parsing via `CommandManager.convertInput(String)`. Les menus/vues qui ne gerent pas une commande lancent `UnhandledCommandException`.

`CommandHandler` : interface fonctionnelle `(String input, Command command) -> Object` utilisee par `OrderedMenu` et `KinomichiFunctions.promptInput()`.

## Fonctions utilitaires applicatives (app.utils.helpers.KinomichiFunctions)

- `promptInput(Scanner, CommandHandler, ThrowingConsumer, ...)` — boucle d'input generique avec gestion des commandes, des erreurs, et des hooks
- `promptField(Scanner, ThrowingConsumer)` — raccourci pour un champ de formulaire avec support `!b` (back) et `!e` (exit)

`ThrowingConsumer<T>` : interface fonctionnelle dont `accept(T)` peut lancer une `Exception` (utilisee pour les setters qui lancent `ModelException`).

`ThrowingVerificator<T>` : interface fonctionnelle `boolean accept(T)` pouvant lancer une `Exception` (utilisee pour les predicats de validation dans les formulaires).

## Navigation (app.utils.NavigationHistory)

`AppState.navigationHistory` (instance statique) maintient une pile de paths.

- `push(path)` — ajoute le path (ignore si identique au dernier)
- `goBack(steps)` — retourne le path cible et nettoie la pile (note : supprime la cible elle-meme de la pile, le `push` dans la boucle principale la re-ajoute)
- `goBackUntilDifferentRoute(Router)` — remonte dans la pile jusqu'a trouver un path qui correspond a une route differente de la route courante (utilise par `GoBackBackEvent`)

## Validation (package `utils.helpers.validation`)

`Validators` — classe utilitaire statique avec methodes de validation :
- `validateNotNullOrBlank(String, boolean strip)`, `validateNotNullOrStrictlyEmpty(String)`
- `validateInt(String)`, `validatePositiveInt(String)`, `validateStrictlyPositiveInt(String)`, `validateInteger(String)` (nullable)
- `validateDouble(String, boolean lax)` (mode lax : convertit les virgules en points)
- `validateEmail(String)`, `validateInstant(String)` (ISO 8601)

Hierarchie d'exceptions :
- `ValidatorException` → `BlankOrNullValueValidatorException`, `StrictlyEmptyOrNullValueValidatorException`, `ParsingValidatorException`, `PatternMatchingValidatorException`
- `BoundaryValidatorException` (extends ValidatorException) → `BelowBoundaryValidatorException`, `AboveBoundaryValidatorException`

## Elements utilitaires applicatifs (package `app.utils.elements`)

### Money (`app.utils.elements.money`)

- `Currency` (enum) — devises supportees (EURO), avec symbole et placement (BEFORE/AFTER)
- `MoneyAmount` — montant avec devise, proprietes `currency` et `amount`
- `Price` (extends MoneyAmount) — montant non-negatif (validation dans `setAmount`)

### Time (`app.utils.elements.time`)

- `TimeSlot` — intervalle temporel immutable avec `start` (Instant) et `end` (Instant). Validation : end > start. Methodes utilitaires : `overlaps()`, `contains()`, `isBefore()`, `isAfter()`, `getDuration()`. Affichage localise en francais via `toPrettyStringFormat()`

## Tarification (package `app.utils.tarification`)

Systeme de tarification avec `ChargeableElement`, `ChargeableElementType`, `ChargingElementType`, `EChargeableCategory`. Exceptions : `ChargeableElementException`, `ChargeableException`, `TarificationException`.

# Conventions

## Structure des classes Java

Les classes doivent etre organisees avec des commentaires de section `// ─── Nom ─── //` dans cet ordre. N'inclure un commentaire de section que si la section contient du contenu — omettre les sections vides :

1. `// ─── Properties ─── //`
2. `// ─── Constructors ─── //`
3. `// ─── Getters ─── //`
4. `// ─── Special getters ─── //`
5. `// ─── Setters ─── //`
6. `// ─── Special setters ─── //`
7. `// ─── Utility methods ─── //`
8. `// ─── Overrides & inheritance ─── //`
9. `// ─── Sub classes ─── //`

## Style de code Java

- Toujours utiliser `this.` pour acceder aux proprietes et methodes de l'instance (pas d'ecriture simplifiee).
- Nommage verbeux et complet : les noms de variables doivent refleter le type/contexte complet (ex : `unsavedDataManagers` plutot que `unsaved`, `badlyInitializedDataManagers` plutot que `badlyInitialized`).
- Validation fail-fast dans les setters : verifier les arguments et lancer `ModelException` (ou `DataManagerException`) immediatement si invalide. Pattern typique : `if (value == null || value.isBlank()) throw new ModelException("...")`.
- Les sections `Getters` / `Setters` sont reservees aux getters et setters **stricts** (acces direct a une propriete). Les methodes qui deleguent a un sous-objet (ex : `this.paths.size()`) vont dans `Utility methods`.
- Eviter les `if` one-liner sans accolades (ex : `if (x) doSomething();`). Toujours utiliser des accolades. Les ternaires sont OK.

## Langue

- Tout le code doit etre en anglais : noms de variables, methodes, classes, commentaires, JavaDoc, etc.
- Seuls les textes affiches a l'utilisateur (messages d'erreur, labels de menu, etc.) sont en francais.

# Modeles et donnees

## Pattern Model / Data (hydrate/dehydrate)

Hierarchie des modeles :
- `Model` (abstract) — classe de base, impose `isValid()`
- `IdentifiedModel` (abstract, extends Model) — ajoute un `id` entier avec `@TableDisplay`
- `IdentifiedModelData` (abstract) — DTO de base avec `id` + `setId(int)` / `setId(String)`

Modeles avec references (pattern complet hydrate/dehydrate) :
- Le Model implemente `Hydratable<{Model}.Data>`
- `{Model}.Data` extends `IdentifiedModelData` et implemente `CustomSerializable` + `JsonConvertible`
- `hydrate(Data)` : peuple le Model depuis le DTO (les FK sont stockees dans des champs `pending*Pk`, pas encore resolues)
- `dehydrate()` : convertit le Model en DTO (extrait les IDs des objets references)

`CampScheduledItem` (interface) : implemente par les modeles dont le `timeSlot` doit etre contenu dans celui du camp parent (`Dinner`, `Lodging`, `Session`). Fournit `validateTimeSlotWithinCampBounds()` et `validateInstantWithinCampBounds()`.

Modeles existants :
- `Camp` — Hydratable, references : `@ModelReference → Address`
- `Club` — Hydratable, references : `@ModelReference → Address`
- `Affiliation` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Person`, `@ModelReference → Club`. Propriete `validityPeriod` (TimeSlot) pour la periode de validite. Possede un `affiliationNumber` (format XXXX-YYYYY)
- `Address` — Hydratable, references : `@ModelReference → Country`
- `Person` — pas de inner class Data, implemente directement `CustomSerializable` + `JsonConvertible` (cas particulier historique, pas de FK). Les formulaires d'ajout manipulent l'objet `Person` directement (pas un DTO)
- `Country` — extends `Model` directement (pas IdentifiedModel), utilise `iso3` comme cle, implemente `CsvConvertible`
- `Dinner` — extends `IdentifiedModel`, Hydratable
- `DinnerReservation` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Person`, `@ModelReference → Dinner`. Propriete `cancellationDatetime` (Instant, nullable)
- `Invitation` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Person`, `@ModelReference → Camp`
- `Lodging` — extends `IdentifiedModel`, Hydratable, implements `CampScheduledItem`, references : `@ModelReference → Camp`. Proprietes `label`, `timeSlot`, `price`
- `LodgingReservation` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Person`, `@ModelReference → Lodging`
- `Session` — extends `IdentifiedModel`, Hydratable, implements `CampScheduledItem`, references : `@ModelReference → Camp`. Proprietes `label`, `timeSlot`. Creneau horaire valide dans les bornes du camp
- `SessionTrainer` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Session`, `@ModelReference → Person`. Represente l'assignation d'un formateur a une session
- `SessionRegistration` — extends `IdentifiedModel`, Hydratable, references : `@ModelReference → Session`, `@ModelReference → Person`. Represente l'inscription d'un participant a une session
- `CampDiscount` — extends `IdentifiedModel`, Hydratable

### Exception `NoResultForPrimaryKeyException`

Exception specialisee (extends `ModelException`) lancee par les methodes `get*WithExceptions()` des DataManagers quand aucun modele ne correspond a la cle primaire fournie. Utilisee pour valider les FK saisies dans les formulaires.

### Special getters pour FK avec protection null

Les getters de FK affichees en table (`getAddressId()`, `getClubId()`, `getCountryIso3()`) doivent gerer le cas ou la reference n'est pas encore resolue :
```java
public int getAddressId() {
    return this.address != null ? this.address.getId() : -1;
}
```

## Pattern DataManager

Chaque DataManager :
- Extends `DataManager<{Manager}.Data>` et implemente `Hydratable<{Manager}.Data>`
- Stocke les modeles dans un `TreeMap` (cle = PK, valeur = Model)
- A une inner class `Data` implementant `CustomSerializable` + `JsonConvertible`/`CsvConvertible`
- Doit avoir un constructeur sans argument (peut etre `private`)
- Doit overrider : `init()`, `export()`, `export(FileType)`, `count()`, `hydrate(Data)`, `dehydrate()`, `addResolvedModel(Model)`

DataManagers existants : `PersonDataManager`, `CountryDataManager`, `AddressDataManager`, `ClubDataManager`, `CampDataManager`, `SessionDataManager`, `LodgingDataManager`, `LodgingReservationDataManager`, `DinnerDataManager`, `DinnerReservationDataManager`, `InvitationDataManager`, `AffiliationDataManager`, `SessionTrainerDataManager`, `SessionRegistrationDataManager`, `CampDiscountDataManager`.

Convention `get*WithExceptions()` : certains DataManagers exposent un getter qui lance `NoResultForPrimaryKeyException` si la PK n'existe pas (ex : `getClubWithExceptions(int)`, `getPersonWithExceptions(int)`, `getCountryWithExceptions(String)`). Utilise pour la validation dans les formulaires et les `set*FromPk()`.

Exceptions DataManager :
- `DeletingReferencedDataManagerDataException` — lancee lors de la suppression d'un modele encore reference par d'autres
- `OverridingUninitializedDataManagerDataException` — lancee lors d'une tentative d'export/modification sur un DataManager non initialise

Auto-increment centralise : `DataManager.applyAutoIncrementIfPossible(IdentifiedModel)` attribue automatiquement un ID aux nouveaux modeles. Les getters de collections retournent des maps non modifiables via `Collections.unmodifiableSortedMap()`.

`DataManagers.exportAll()` : sauvegarde tous les DataManagers ayant `hasUnsavedChanges() == true`. Appelee a la sortie de l'application.

Initialisation en deux passes (dans `DataManagers.initAndResolveReferencesOf()`) :
1. **Pass 1** : instanciation + `init()` — lit le fichier, parse le JSON/CSV, appelle `hydrate()` qui cree des modeles avec des `pending*Pk` non resolus (stockes dans `this.pendingModels`)
2. **Pass 2** : `resolveReferences()` — cascade les dependances via `@ModelReference`, appelle les `set*FromPk()` par reflection, puis `addResolvedModel()` pour chaque modele valide

Exceptions : `PersonDataManager` et `CountryDataManager` n'utilisent pas `pendingModels` — ils ajoutent les modeles directement dans `hydrate()` et font `this.initialized = true` immediatement (car pas de FK a resoudre).

Donnees stockees dans `/data/` avec le nom de fichier derive automatiquement (voir section reflection).

### Regles metier du systeme de sessions

Le systeme de sessions (Session / SessionTrainer / SessionRegistration) implique des validations croisees entre DataManagers :

- **Ajout d'un formateur** (`SessionTrainerDataManager.addSessionTrainer`) : la personne doit etre affiliee durant le camp (`AffiliationDataManager.getPersonAffiliationsDuringTimeSlot`), ne pas etre deja formateur de cette session, ne pas etre formateur ou inscrit dans une session avec timeSlot qui overlap
- **Retrait d'un formateur** (`SessionTrainerDataManager.deleteSessionTrainer`) : bloque si la session a des inscrits (`SessionRegistrationDataManager.isSessionUsed`)
- **Ajout d'une inscription** (`SessionRegistrationDataManager.addSessionRegistration`) : la session doit avoir au moins un formateur, la personne ne doit pas etre inscrite ou formateur dans une session avec timeSlot qui overlap
- **Suppression d'une session** (`SessionDataManager.deleteSession`) : les formateurs ET les inscriptions doivent etre vides
- **Modification du timeSlot d'une session** (`SessionDataManager.updateSession`) : les formateurs ET les inscriptions doivent etre vides
- **Suppression/modification d'une affiliation** (`AffiliationDataManager`) : verifie que la personne n'est pas formateur dans une session dont le camp tombe durant la periode de validite de l'affiliation (sauf si une autre affiliation couvre cette periode)

## Conventions de nommage implicites (reflection)

Le projet utilise de la reflection et des conventions de nommage pour eviter du parametrage verbeux. Ces regles **doivent** etre respectees pour que l'application fonctionne :

### 1. `{Model}DataManager` → nom du fichier de donnees

- La classe `DataManager` derive automatiquement le nom du fichier de donnees a partir de son propre nom de classe.
- Regle : on retire le suffixe `"DataManager"` du nom de la classe, puis on convertit en `snake_case`.
- Exemple : `ClubDataManager` → fichier `club.json`, `AddressDataManager` → fichier `address.json`.
- Code : `DataManager.fileName = Functions.toSnakeCase(this.getModelSimpleName())` ou `getModelSimpleName()` retire le suffixe `"DataManager"`.

### 2. `{Model}DataManager` → classe `Model` correspondante

- `DataManagers.hasDependencies()` et `DataManager.getModelClass()` retrouvent la classe Model associee par reflection.
- Regle : on retire `"DataManager"` du nom de la classe, puis on remonte d'un package (de `app.models.managers` vers `app.models`) pour construire le FQCN.
- Exemple : `app.models.managers.ClubDataManager` → `app.models.Club`.
- Implication : le Model **doit** etre dans le package parent direct du package du DataManager, et porter exactement le nom `{X}` si le manager s'appelle `{X}DataManager`.

### 3. `@ModelReference` → champ `pending{Field}Pk` + methode `set{Field}FromPk`

- Quand un champ de Model est annote `@ModelReference`, le systeme de resolution des references (`DataManagers.resolveModelReferences()`) s'attend a trouver par reflection :
  1. Un champ `pending{CapitalizedFieldName}Pk` sur le meme Model, contenant la cle primaire en attente de resolution.
  2. Une methode `set{CapitalizedFieldName}FromPk(type)` qui resout la reference a partir de cette cle primaire.
- Exemple concret pour `Club` :
  - Champ annote : `@ModelReference(manager = AddressDataManager.class) private Address address;`
  - Champ pending attendu : `private int pendingAddressPk;`
  - Methode setter attendue : `public void setAddressFromPk(int addressId)`
- Exemple concret pour `Address` :
  - Champ annote : `@ModelReference(manager = CountryDataManager.class) private Country country;`
  - Champ pending attendu : `private String pendingCountryPk;`
  - Methode setter attendue : `public void setCountryFromPk(String iso3)`

### 4. `ModelTable` et `@ModelTableDisplay` → colonnes d'affichage en table

Le systeme d'affichage tabulaire est base sur des classes `ModelTable` decouplees des modeles (package `app.models.formatting.table`).

Hierarchie :
- `ModelTable<T extends Model>` (abstract) — base, fournit la resolution par reflection (`fromModelType(M)`, `fromModelClass(Class)`)
- `IdentifiedModelTable<T extends IdentifiedModel>` (extends ModelTable) — ajoute automatiquement la colonne ID
- Implementations concretes : `PersonModelTable`, `ClubModelTable`, `AffiliationModelTable`, `CampModelTable`, `CountryModelTable`, `AddressModelTable`, `DinnerModelTable`, `DinnerReservationModelTable`, `InvitationModelTable`, `LodgingModelTable`, `LodgingReservationModelTable`, `SessionModelTable`, `SessionTrainerModelTable`, `SessionRegistrationModelTable`

Convention de nommage : `{Model}ModelTable` dans le package `app.models.formatting.table`. Resolu par reflection a partir du nom du Model.

`ModelTableFormatter` (dans `app.models.formatting`) decouvre par reflection les methodes annotees `@ModelTableDisplay` sur la classe `ModelTable` correspondante :
- Les methodes sont triees par `order()` et invoquees pour extraire les valeurs
- Le `name()` de l'annotation sert de titre de colonne
- Methodes statiques : `forList(List<T>)`, `forDetail(T item)`, `getColumnCount(Class)`, `comparatorForColumn(Class, int)`
- Convention pour les FK : `name = "#& (entite)"` avec `ModelKeyTextFormattingPreset` et `TextAlignment.CENTER`

Presets de formatage (dans `app.models.formatting`) : `ModelPrimaryKeyTextFormattingPreset` (bold, underline, blue), `ModelKeyTextFormattingPreset` (underline).

### 5. Instanciation des `DataManager` par reflection

- `DataManagers.initAndGet()` instancie les DataManagers via `clazz.getDeclaredConstructor().newInstance()` avec `setAccessible(true)`.
- Implication : chaque DataManager **doit** avoir un constructeur sans argument (il peut etre `private` ou package-private).
