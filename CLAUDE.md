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

## Langue

- Tout le code doit être en anglais : noms de variables, méthodes, classes, commentaires, JavaDoc, etc.
- Seuls les textes affichés à l'utilisateur (messages d'erreur, labels de menu, etc.) sont en français.

## Conventions de nommage implicites (reflection)

Le projet utilise de la reflection et des conventions de nommage pour éviter du paramétrage verbeux. Ces règles **doivent** être respectées pour que l'application fonctionne :

### 1. `{Model}DataManager` → nom du fichier de données

- La classe `DataManager` dérive automatiquement le nom du fichier de données à partir de son propre nom de classe.
- Règle : on retire le suffixe `"DataManager"` du nom de la classe, puis on convertit en `snake_case`.
- Exemple : `ClubDataManager` → fichier `club.json`, `AddressDataManager` → fichier `address.json`.
- Code : `DataManager.fileName = Functions.toSnakeCase(this.getClass().getSimpleName().replace("DataManager", ""))`.

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
- Si un `TextFormattingPreset` custom est spécifié dans `format()`, il est instancié par reflection via `getDeclaredConstructor().newInstance()`.

### 5. Instanciation des `DataManager` par reflection

- `DataManagers.initAndGet()` instancie les DataManagers via `clazz.getDeclaredConstructor().newInstance()` avec `setAccessible(true)`.
- Implication : chaque DataManager **doit** avoir un constructeur sans argument (il peut être `private` ou package-private).
