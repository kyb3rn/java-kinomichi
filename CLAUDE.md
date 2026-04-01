# Projet

Application CLI d'administration pour le Kinomichi. Java 21, Maven, terminal ANSI.

Packages principaux :
- `app/` — code applicatif (models, menus, middlewares, utils)
- `utils/` — bibliothèque réutilisable (I/O, formatage, data management, time)

# Conventions

## Structure des classes Java

Les classes doivent être organisées avec des commentaires de section `// ─── Nom ─── //` dans cet ordre. N'inclure un commentaire de section que si la section contient du contenu — omettre les sections vides :

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

- Toujours utiliser `this.` pour accéder aux propriétés et méthodes de l'instance (pas d'écriture simplifiée).
- Nommage verbeux et complet : les noms de variables doivent refléter le type/contexte complet (ex : `unsavedDataManagers` plutôt que `unsaved`, `badlyInitializedDataManagers` plutôt que `badlyInitialized`).
- Validation fail-fast dans les setters : vérifier les arguments et lancer `ModelException` (ou `DataManagerException`) immédiatement si invalide. Pattern typique : `if (value == null || value.isBlank()) throw new ModelException("...")`.

## Langue

- Tout le code doit être en anglais : noms de variables, méthodes, classes, commentaires, JavaDoc, etc.
- Seuls les textes affichés à l'utilisateur (messages d'erreur, labels de menu, etc.) sont en français.

## Pattern Model / Data (hydrate/dehydrate)

Hiérarchie des modèles :
- `Model` (abstract) — classe de base, impose `isValid()`
- `IdentifiedModel` (abstract, extends Model) — ajoute un `id` entier avec `@TableDisplay`
- `Country` est un cas particulier : extends `Model` directement (utilise `iso3` comme clé, pas un id entier)

Chaque Model avec un ID a un DTO interne `{Model}.Data` :
- `{Model}.Data` extends `IdentifiedModelData` et implémente `CustomSerializable` + `JsonConvertible` (ou `CsvConvertible` pour `Country`)
- Le Model implémente `Hydratable<{Model}.Data>`
- `hydrate(Data)` : peuple le Model depuis le DTO (les FK sont stockées dans les champs `pending*Pk`, pas encore résolues)
- `dehydrate()` : convertit le Model en DTO (extrait les IDs des objets référencés)

Exemple concret (`Camp`) :
```
Camp extends IdentifiedModel implements Hydratable<Camp.Data>
Camp.Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible
```

## Pattern DataManager

Chaque DataManager :
- Extends `DataManager<{Manager}.Data>` et implémente `Hydratable<{Manager}.Data>`
- Stocke les modèles dans un `TreeMap` (clé = PK, valeur = Model)
- A une inner class `Data` implémentant `CustomSerializable` + `JsonConvertible`/`CsvConvertible`
- Doit avoir un constructeur sans argument (peut être `private`)
- Doit overrider : `init()`, `export()`, `export(FileType)`, `count()`, `hydrate(Data)`, `dehydrate()`, `addResolvedModel(Model)`

Initialisation en deux passes (dans `DataManagers.initAndResolveReferencesOf()`) :
1. **Pass 1** : instanciation + `init()` — lit le fichier, parse le JSON/CSV, appelle `hydrate()` qui crée des modèles avec des `pending*Pk` non résolus
2. **Pass 2** : `resolveReferences()` — cascade les dépendances via `@ModelReference`, appelle les `set*FromPk()` par reflection, puis `addResolvedModel()`

Données stockées dans `/data/` avec le nom de fichier dérivé automatiquement (voir section reflection).

## Navigation par menus

`Main.java` maintient un `HashMap<String, Supplier<MenuStage>>` de routes vers des menus.

Convention de nommage des routes : `{domaine}.{action}` (ex : `"camps.manage"`, `"camps.list"`, `"camps.add"`, `"camps.manage.camp"`, `"data_managers.reinit"`).

Deux types de menus dans Main :
- **Dynamiques** (recréés à chaque affichage via `::new`) : menus dont le contenu dépend de l'état courant (compteurs, état d'init, camp sélectionné)
- **Statiques** (instance unique réutilisée via `() -> instance`) : menus dont le contenu ne change pas

Hiérarchie des menus :
- `MenuStage` (abstract) — base, contient les `middlewares`, méthode abstraite `use()` qui retourne `MenuLeadTo`
- `OptionedMenuStage` (abstract, extends MenuStage) — menu à choix numérotés, gère input loop, commandes, hooks (`beforeDisplay`, `afterDisplay`, `beforeInput`, `afterEveryInput`, `afterValidInput`)
- `StandardMenu` (extends OptionedMenuStage) — implémentation concrète, rendu en table avec titre coloré

`use()` retourne `MenuLeadTo` (prochaine route) ou `null` (quitter l'application).

Les menus custom (formulaires, listes interactives) peuvent extends `MenuStage` directement et gérer leur propre boucle d'input.

## Système de commandes

Les commandes utilisateur sont préfixées par `!` (ex : `!q`, `!b`, `!sort 2`).

Commandes disponibles (enum `ECommand`) : `QUIT` (`!q`), `BACK` (`!b`), `SORT` (`!sort`).

Parsing via `CommandManager.convertInput(String)`. Les menus qui ne gèrent pas une commande lancent `UnhandledCommandException`.

## Middleware

Les `Middleware` sont des vérifications exécutées avant l'affichage d'un `OptionedMenuStage`.

`verify()` retourne `null` (passe) ou un `MenuLeadTo` (redirection). Si un middleware échoue, le menu n'est pas affiché et l'utilisateur est redirigé.

Ajout dans le constructeur du menu : `this.middlewares.add(new IsACampSelectedMiddleware())`.

## Conventions de nommage implicites (reflection)

Le projet utilise de la reflection et des conventions de nommage pour éviter du paramétrage verbeux. Ces règles **doivent** être respectées pour que l'application fonctionne :

### 1. `{Model}DataManager` → nom du fichier de données

- La classe `DataManager` dérive automatiquement le nom du fichier de données à partir de son propre nom de classe.
- Règle : on retire le suffixe `"DataManager"` du nom de la classe, puis on convertit en `snake_case`.
- Exemple : `ClubDataManager` → fichier `club.json`, `AddressDataManager` → fichier `address.json`.
- Code : `DataManager.fileName = Functions.toSnakeCase(this.getModelSimpleName())` où `getModelSimpleName()` retire le suffixe `"DataManager"`.

### 2. `{Model}DataManager` → classe `Model` correspondante

- `DataManagers.hasDependencies()` retrouve la classe Model associée à un DataManager par reflection.
- Règle : on retire `"DataManager"` du nom de la classe, puis on remonte d'un package (de `app.models.managers` vers `app.models`) pour construire le FQCN.
- Exemple : `app.models.managers.ClubDataManager` → `app.models.Club`.
- Implication : le Model **doit** être dans le package parent direct du package du DataManager, et porter exactement le nom `{X}` si le manager s'appelle `{X}DataManager`.

### 3. `@ModelReference` → champ `pending{Field}Pk` + méthode `set{Field}FromPk`

- Quand un champ de Model est annoté `@ModelReference`, le système de résolution des références (`DataManagers.resolveModelReferences()`) s'attend à trouver par reflection :
  1. Un champ `pending{CapitalizedFieldName}Pk` sur le même Model, contenant la clé primaire en attente de résolution.
  2. Une méthode `set{CapitalizedFieldName}FromPk(type)` qui résout la référence à partir de cette clé primaire.
- Exemple concret pour `Club` :
  - Champ annoté : `@ModelReference(manager = AddressDataManager.class) private Address address;`
  - Champ pending attendu : `private int pendingAddressPk;`
  - Méthode setter attendue : `public void setAddressFromPk(int addressId)`
- Exemple concret pour `Address` :
  - Champ annoté : `@ModelReference(manager = CountryDataManager.class) private Country country;`
  - Champ pending attendu : `private String pendingCountryPk;`
  - Méthode setter attendue : `public void setCountryFromPk(String iso3)`

### 4. `@TableDisplay` sur les getters → colonnes d'affichage en table

- `ModelTableFormatter` découvre par reflection toutes les méthodes publiques annotées `@TableDisplay` sur une classe Model.
- Les méthodes sont triées par `order()` et invoquées par reflection pour extraire les valeurs.
- Le `name()` de l'annotation sert de titre de colonne.
- L'annotation imbriquée `@TableDisplayFormattingOptions` dans `format()` permet de spécifier : `preset` (classe `TextFormattingPreset`), `alignment`, `color`, `backgroundColor`, `styles`.
- Si un `TextFormattingPreset` custom est spécifié, il est instancié par reflection via `getDeclaredConstructor().newInstance()`.
- Presets existants : `ModelPrimaryKeyTextFormattingPreset` (bold, underline, blue), `ModelKeyTextFormattingPreset` (underline).

### 5. Instanciation des `DataManager` par reflection

- `DataManagers.initAndGet()` instancie les DataManagers via `clazz.getDeclaredConstructor().newInstance()` avec `setAccessible(true)`.
- Implication : chaque DataManager **doit** avoir un constructeur sans argument (il peut être `private` ou package-private).
