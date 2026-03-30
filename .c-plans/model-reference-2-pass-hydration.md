# Plan: @ModelReference + Hydratation en 2 passes

## Contexte

Les Models resolvent leurs references vers d'autres Models pendant l'hydratation via `DataManagers.get()`. Cela impose un ordre de chargement strict et rend les references cycliques impossibles (ex: Club -> Personne -> Club).

**Solution**: Separer l'hydratation en 2 passes:
- **Passe 1**: Charger tous les managers avec les donnees primitives uniquement (les PKs des refs sont stockees dans des champs `pending*Pk` sur les Models)
- **Passe 2**: Une fois TOUS les managers charges, un resolveur par reflexion scanne les champs `@ModelReference` et appelle les special setters `set*FromPk` automatiquement

## Decisions de design

- `@ModelReference` = marqueur sans metadonnees, sur les **champs** (FIELD target)
- Conventions strictes de nommage derivees du type du champ annote:
  - `@ModelReference private Address address;`
  - Champ temporaire: `pendingAddressPk`
  - Special setter: `setAddressFromPk(pk)`
  - Manager: `AddressDataManager` (type + "DataManager")
  - Lookup: `getAddress(pk)` ("get" + type)
- Resolution automatique par reflexion dans `DataManagers`
- Pas d'interface `ReferenceResolvable` (la reflexion la rend inutile)
- Erreurs: try-catch par manager. Si passe 2 echoue, le manager reste dans `instances` (singleton preserve) avec un HashMap vide (pas d'objets partiels). `pendingModels` est clear
- Jamais d'objets partiels dans les managers: passe 1 stocke dans `pendingModels`, transfert dans le vrai HashMap seulement apres passe 2 reussie

## Renommages

| Actuel | Nouveau |
|--------|---------|
| `Club.setAddressFromId(int)` | `Club.setAddressFromPk(int)` |
| `Address.setCountryFromIso3(String)` | `Address.setCountryFromPk(String)` |

Impacts du renommage:
- `Club.setAddress(Address)` appelle `this.setAddressFromId(...)` → mettre a jour
- `Address.setCountry(Country)` appelle `this.setCountryFromIso3(...)` → mettre a jour
- `Address.getCountryFromIso3(String)` (methode statique utilitaire) → **pas renomme**, ce n'est pas un setter

## Fichier a creer (1)

### 1. `src/main/java/app/models/ModelReference.java`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModelReference {}
```

## Fichiers a modifier (7)

### 2. `src/main/java/app/models/Address.java`

- Annoter le champ `country` avec `@ModelReference`
- Ajouter champ `private String pendingCountryPk;`
- Renommer `setCountryFromIso3` → `setCountryFromPk`
- Mettre a jour `setCountry()` qui appelle l'ancien nom
- **hydrate()**: remplacer `this.setCountryFromIso3(dataObject.getCountryIso3())` par `this.pendingCountryPk = dataObject.getCountryIso3()`

### 3. `src/main/java/app/models/Club.java`

- Annoter le champ `address` avec `@ModelReference`
- Ajouter champ `private int pendingAddressPk = -1;`
- Renommer `setAddressFromId` → `setAddressFromPk`
- Mettre a jour `setAddress()` qui appelle l'ancien nom
- **hydrate()**: remplacer `this.setAddressFromId(dataObject.getAddressId())` par `this.pendingAddressPk = dataObject.getAddressId()`

### 4. `src/main/java/app/data_management/managers/AddressDataManager.java`

- Ajouter champ `private List<Address> pendingModels;` (stockage temporaire passe 1)
- **hydrate()**: creer les Address, hydrater (primitives uniquement), stocker dans `pendingModels` (PAS dans `this.addresses`)
- Override `resolveReferences()`: iterer `pendingModels`, appeler `DataManagers.resolveModelReferences(address)`, valider `isValid()`, puis transferer dans `this.addresses`. Mettre `pendingModels = null`
- **addAddress(Address.Data)** (chemin interactif): ajouter `DataManagers.resolveModelReferences(address)` apres `address.hydrate(addressData)` — ici on met directement dans `this.addresses` car les managers sont deja charges

**Garantie**: `this.addresses` ne contient jamais d'objets partiels. Il est vide jusqu'a ce que la passe 2 reussisse.

### 5. `src/main/java/app/data_management/managers/ClubDataManager.java`

- Meme pattern que AddressDataManager: champ `private List<Club> pendingModels;`
- **hydrate()**: creer les Club, hydrater (primitives + auto-ID), stocker dans `pendingModels` (PAS dans `this.clubs`)
- Override `resolveReferences()`: iterer `pendingModels`, resoudre, valider, transferer dans `this.clubs`. Mettre `pendingModels = null`
- **addClub(Club.Data)** (chemin interactif): ajouter `DataManagers.resolveModelReferences(club)` apres `club.hydrate(clubData)`

### 6. `src/main/java/app/data_management/managers/DataManager.java`

- Ajouter methode par defaut:
  ```java
  public void resolveReferences() throws ModelException {}
  ```

### 7. `src/main/java/app/data_management/managers/DataManagers.java`

- **Nouveau** `resolveModelReferences(Model model)`: methode statique qui fait la reflexion:
  1. Scanner les champs du Model pour `@ModelReference`
  2. Pour chaque champ annote (ex: `address` de type `Address`):
     - Trouver `pendingAddressPk` via convention
     - Trouver `setAddressFromPk` via convention
     - Appeler le setter avec la valeur du pending
- **Nouveau** `initAll(Class...)`:
  - Passe 1: `get(clazz)` pour chaque manager (try-catch individuel)
  - Passe 2: `manager.resolveReferences()` pour chaque manager charge (try-catch individuel, retire de `instances` si echec)

### 8. `src/main/java/Main.java`

- Remplacer les 3 appels `initDataManager()` par `DataManagers.initAll(CountryDataManager.class, AddressDataManager.class, ClubDataManager.class)`
- Supprimer ou conserver `initDataManager()` (plus utilisee)

## Flux d'execution

```
DataManagers.initAll(Country, Address, Club)
  |
  |-- Passe 1: instancier les managers (donnees primitives)
  |     CountryDataManager() → countries chargees (pas de refs)
  |     AddressDataManager() → addresses chargees (pendingCountryPk stocke, country=null)
  |     ClubDataManager()    → clubs charges (pendingAddressPk stocke, address=null)
  |
  |-- Passe 2: resoudre les references par reflexion
        CountryDataManager.resolveReferences()  → no-op (defaut)
        AddressDataManager.resolveReferences()  → pour chaque Address:
            resolveModelReferences(address)
              → trouve @ModelReference sur champ 'country'
              → lit pendingCountryPk = "BEL"
              → appelle setCountryFromPk("BEL")
              → isValid() ✓
        ClubDataManager.resolveReferences()     → pour chaque Club:
            resolveModelReferences(club)
              → trouve @ModelReference sur champ 'address'
              → lit pendingAddressPk = 1
              → appelle setAddressFromPk(1)
              → isValid() ✓
```

## Chemin interactif (AddClubMenu) — inchange

```
AddClubMenu → clubData + addressData remplis par l'utilisateur
  → AddressDataManager.addAddress(addressData)
      → address.hydrate(addressData)       // stocke pendingCountryPk
      → resolveModelReferences(address)    // resout immediatement
      → isValid() ✓
      → addresses.put(...)
  → ClubDataManager.addClub(clubData)
      → club.hydrate(clubData)             // stocke pendingAddressPk
      → resolveModelReferences(club)       // resout immediatement
      → isValid() ✓
      → clubs.put(...)
```

## Gestion d'erreurs

- Si passe 1 echoue pour un manager: log l'erreur, continue (comme actuellement). Le manager n'est pas dans `instances`
- Si passe 2 echoue pour un manager: log l'erreur, clear `pendingModels`. Le manager **reste dans `instances`** (singleton preserve) avec un HashMap vide. `count()` = 0, `get*(id)` = null
- Cascade naturelle: si Country echoue en passe 1, setCountryFromPk echouera pour Address en passe 2 → Address reste vide, puis setAddressFromPk echouera pour Club → Club reste vide

## Verification

1. Compiler: `mvn compile` ou equivalent
2. Lancer l'application: verifier compteurs dans le menu principal
3. Lister les clubs: verifier que les adresses sont resolues (colonne #& affiche bien l'ID)
4. Ajouter un club: verifier que le chemin interactif fonctionne (les refs sont resolues immediatement)
