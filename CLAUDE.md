# Projet

Application CLI d'administration pour le Kinomichi. Java 21, Maven, terminal ANSI.

Packages principaux :
- `app/` — code applicatif (models, controllers, views, events, middlewares, routing, utils)
- `utils/` — bibliotheque reutilisable (I/O, formatage, data management, time)

# Architecture

## Boucle principale (Main.java)

`Main.java` contient la boucle centrale de l'application :

1. Initialise les `DataManagers` via `DataManagers.initAndResolveReferencesOf(...)` (pass 1 + pass 2)
2. Charge les commandes via `CommandManager.loadCommands()`
3. Instancie un `Router` et enregistre toutes les `Route` (nom + regex de path + action de controller)
4. Boucle : `push path dans NavigationHistory` → `router.dispatch(path)` → recoit un `Event` → determine le prochain path (ou `null` pour quitter)
5. A la sortie de la boucle : `DataManagers.exportAll()` sauvegarde automatiquement tous les DataManagers ayant des modifications non enregistrees

Le switch sur les events :
- `CallUrlEvent` → navigue vers l'URL contenue
- `GoBackEvent` → `AppState.navigationHistory.goBack()`
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

Controllers existants : `MainController`, `ExploreController`, `PersonController`, `CampController`, `ClubController`, `AffiliatedController`, `AddressController`, `CountryController`, `DataManagerController`.

## Views (package `app.views`)

- `View` (abstract) — classe de base, methode abstraite `Event render()`
- Les views construisent des menus (`KinomichiStandardMenu`, `ModelTableMenu`) ou gerent des formulaires manuels (scanner + boucle d'input)
- `render()` retourne un `Event` qui remonte au controller puis a la boucle principale

Views existantes :
- `MainView` — menu principal avec options dynamiques selon l'etat des DataManagers
- `ExploreDataView` — menu d'exploration de toutes les donnees
- `ModelListView<M>` — vue generique de liste tabulaire pour n'importe quel Model
- `persons/` — `PersonsDashboardView`, `AddPersonView` + `AddPersonFormData` (record : `Person` + `Affiliated.Data` nullable)
- `camps/` — `SelectCampView`, `ManageCampView`, `AddCampView` + `AddCampFormData` (record : `Camp.Data` + `Address.Data`)
- `clubs/` — `ClubsDashboardView`, `AddClubView` + `AddClubFormData` (record : `Club.Data` + `Address.Data`)
- `data_managers/` — `ReInitDataManagersView`, `SaveDataManagersView`

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
- `ModelTableMenu<M>` (extends OrderedMenu) — affiche une table de modeles via `ModelTableFormatter`, gere la commande `!sort`

`MenuResponse` encapsule la reponse (un `Event`, une `Command`, ou autre objet) qui remonte au code appelant.

## Systeme de commandes (package `utils.io.commands`)

Les commandes utilisateur sont prefixees par `!` (ex : `!e`, `!b`, `!sort 2`).

Commandes disponibles (enum `ECommand`) :
- `EXIT` (`!exit`, `!e`) → `ExitCommand`
- `BACK` (`!back`, `!b`) → `BackCommand`
- `SORT_COLUMN` (`!sort`, `!s`) → `SortColumnCommand` (accepte des arguments : `!sort 2:DESC 3`)

Parsing via `CommandManager.convertInput(String)`. Les menus/vues qui ne gerent pas une commande lancent `UnhandledCommandException`.

`CommandHandler` : interface fonctionnelle `(String input, Command command) -> Object` utilisee par `OrderedMenu` et `KinomichiFunctions.promptInput()`.

## Fonctions utilitaires applicatives (app.utils.helpers.KinomichiFunctions)

- `promptInput(Scanner, CommandHandler, ThrowingConsumer, ...)` — boucle d'input generique avec gestion des commandes, des erreurs, et des hooks
- `promptField(Scanner, ThrowingConsumer)` — raccourci pour un champ de formulaire avec support `!b` (back) et `!e` (exit)

`ThrowingConsumer<T>` : interface fonctionnelle dont `accept(T)` peut lancer une `Exception` (utilisee pour les setters qui lancent `ModelException`).

## Navigation (app.utils.NavigationHistory)

`AppState.navigationHistory` (instance statique) maintient une pile de paths.

- `push(path)` — ajoute le path (ignore si identique au dernier)
- `goBack(steps)` — retourne le path cible et nettoie la pile (note : supprime la cible elle-meme de la pile, le `push` dans la boucle principale la re-ajoute)

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

Modeles existants :
- `Camp` — Hydratable, references : `@ModelReference → Address`
- `Club` — Hydratable, references : `@ModelReference → Address`
- `Affiliated` — extends `Person`, Hydratable, references : `@ModelReference → Person`, `@ModelReference → Club`
- `Address` — Hydratable, references : `@ModelReference → Country`
- `Person` — pas de inner class Data, implemente directement `CustomSerializable` + `JsonConvertible` (cas particulier historique, pas de FK). Les formulaires d'ajout manipulent l'objet `Person` directement (pas un DTO)
- `Country` — extends `Model` directement (pas IdentifiedModel), utilise `iso3` comme cle, implemente `CsvConvertible`

### Exception `NotResultForPrimaryKeyException`

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

DataManagers existants : `CampDataManager`, `ClubDataManager`, `AffiliatedDataManager`, `AddressDataManager`, `PersonDataManager`, `CountryDataManager`.

Convention `get*WithExceptions()` : certains DataManagers exposent un getter qui lance `NotResultForPrimaryKeyException` si la PK n'existe pas (ex : `getClubWithExceptions(int)`, `getPersonWithExceptions(int)`, `getCountryWithExceptions(String)`). Utilise pour la validation dans les formulaires et les `set*FromPk()`.

`DataManagers.exportAll()` : sauvegarde tous les DataManagers ayant `hasUnsavedChanges() == true`. Appelee a la sortie de l'application.

Initialisation en deux passes (dans `DataManagers.initAndResolveReferencesOf()`) :
1. **Pass 1** : instanciation + `init()` — lit le fichier, parse le JSON/CSV, appelle `hydrate()` qui cree des modeles avec des `pending*Pk` non resolus (stockes dans `this.pendingModels`)
2. **Pass 2** : `resolveReferences()` — cascade les dependances via `@ModelReference`, appelle les `set*FromPk()` par reflection, puis `addResolvedModel()` pour chaque modele valide

Exceptions : `PersonDataManager` et `CountryDataManager` n'utilisent pas `pendingModels` — ils ajoutent les modeles directement dans `hydrate()` et font `this.initialized = true` immediatement (car pas de FK a resoudre).

Donnees stockees dans `/data/` avec le nom de fichier derive automatiquement (voir section reflection).

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

### 4. `@TableDisplay` sur les getters → colonnes d'affichage en table

- `ModelTableFormatter` decouvre par reflection toutes les methodes publiques annotees `@TableDisplay` sur une classe Model.
- Les methodes sont triees par `order()` et invoquees par reflection pour extraire les valeurs.
- Le `name()` de l'annotation sert de titre de colonne.
- Convention pour les FK : `name = "#& (entite)"` avec `ModelKeyTextFormattingPreset` et `TextAlignment.CENTER`.
- L'annotation imbriquee `@TableDisplayFormattingOptions` dans `format()` permet de specifier : `preset` (classe `TextFormattingPreset`), `alignment`, `color`, `backgroundColor`, `styles`.
- Si un `TextFormattingPreset` custom est specifie, il est instancie par reflection via `getDeclaredConstructor().newInstance()`.
- Presets existants : `ModelPrimaryKeyTextFormattingPreset` (bold, underline, blue), `ModelKeyTextFormattingPreset` (underline).

### 5. Instanciation des `DataManager` par reflection

- `DataManagers.initAndGet()` instancie les DataManagers via `clazz.getDeclaredConstructor().newInstance()` avec `setAccessible(true)`.
- Implication : chaque DataManager **doit** avoir un constructeur sans argument (il peut etre `private` ou package-private).
