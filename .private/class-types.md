# Types de classes

Ce document décrit les types de classes non triviaux du projet, leur rôle et leur fonctionnement.

---

## Modèles (`Model`)

### Hiérarchie

```
Model (abstract)                     — Classe de base, impose isValid()
└─ IdentifiedModel (abstract)        — Ajoute un id entier (auto-incrémenté par le DataManager)
```

### Rôle

Un modèle représente une entité métier (personne, club, stage, session...). Chaque modèle :

- Valide ses propres données dans ses setters (fail-fast : lance `ModelException` si invalide)
- Expose des méthodes statiques `verifyXxx(String)` pour valider un champ brut avant de l'assigner
- Implémente `isValid()` pour vérifier que l'objet est dans un état cohérent (toutes les propriétés requises non nulles)
- Implémente `clone()` pour créer une copie modifiable (utilisé par les formulaires de modification)

### DTO intégré (`Data`)

Les modèles qui ont des références vers d'autres modèles (FK) contiennent une **inner class `Data`** qui sert de DTO
(Data Transfer Object). Ce DTO est le format intermédiaire entre le fichier JSON et le modèle Java complet :

```
Fichier JSON  ──parseJson()──▸  Data (DTO)  ──hydrate()──▸  Model (avec pendingXxxId)  ──resolve──▸  Model complet
Model complet ──dehydrate()──▸  Data (DTO)  ──toJson()───▸  Fichier JSON
```

- **`Data`** extends `IdentifiedModelData`, implémente `CustomSerializable` + `JsonConvertible`
- **`Data`** ne contient que des types primitifs/String ou objets simples (Instant) (les FK sont stockées comme des `int`/`String`, pas comme des objets)
- **`hydrate(Data)`** : peuple le modèle depuis le DTO — les FK sont copiées dans des champs `pendingXxxId`, pas encore résolues
- **`dehydrate()`** : extrait les IDs des objets référencés pour recréer un DTO exportable

> *Exemple concret : `Club.Data` contient `addressId = 3` (un int). Après `hydrate()`, le Club a `pendingAddressPk = 3`
> et `address = null`. Après résolution, `address` pointe vers l'objet `Address` réel et `pendingAddressPk` n'est plus utilisé.*

### Références entre modèles (`@ModelReference`)

L'annotation `@ModelReference` sur un champ déclenche la résolution automatique par reflection :

```
@ModelReference(manager = AddressDataManager.class)
private Address address;              ← Le champ cible (null après hydrate, rempli après resolve)
private int pendingAddressPk = -1;    ← La FK brute (remplie après hydrate, ignorée après resolve)
```

La résolution appelle `setAddressFromPk(int)` par reflection, qui interroge le DataManager cible pour obtenir l'objet.

---

## DataManagers (`DataManager`)

### Rôle

Un DataManager est le **gestionnaire de persistance** d'un type de modèle. Il est responsable de :

- **Charger** les données depuis un fichier JSON/CSV au démarrage (`init()` + `hydrate()`)
- **Stocker** les modèles en mémoire dans une `TreeMap` triée par clé primaire
- **Résoudre** les références inter-modèles (`resolveReferences()`)
- **Valider** la cohérence croisée après résolution (`validateResolvedModels()`)
- **Exposer** des méthodes CRUD (add, update, delete, get) avec validation métier
- **Exporter** les données modifiées vers le fichier à la fermeture de l'application (`export()` + `dehydrate()`)

### Cycle de vie

```
1. Instanciation (reflection, constructeur sans argument)
2. init()                  → lit le fichier, parse le JSON, appelle hydrate() → pendingModels
3. resolveReferences()     → résout les FK via @ModelReference → TreeMap finale
4. validateResolvedModels() → validations croisées inter-DM
5. [utilisation normale : get, add, update, delete]
6. export()                → dehydrate() + écriture fichier (si unsavedChanges)
```

### Flags d'état

| Flag          | Signification                                                       |
|---------------|---------------------------------------------------------------------|
| `dataLoaded`  | Le fichier a été lu et parsé (passe 1 terminée)                     |
| `initialized` | Les références sont résolues et validées (prêt à l'emploi)         |
| `unsavedChanges` | Des modifications en mémoire n'ont pas encore été écrites sur disque |

### Convention de nommage

Le nom du fichier de données est dérivé automatiquement du nom de la classe :
`ClubDataManager` → retire `DataManager` → `Club` → snake_case → `club.json`

---

## Events (`Event`)

### Rôle

Les Events sont le **mécanisme de communication ascendante**. Ils remontent depuis les Views/Menus vers les Controllers,
puis vers la boucle principale de `Main`. Chaque Event exprime une intention de navigation ou un résultat.

### Types

| Event                | Signification                                                            |
|----------------------|--------------------------------------------------------------------------|
| `CallUrlEvent(url)`  | Naviguer vers l'URL donnée                                               |
| `GoBackEvent`        | Revenir en arrière d'un pas dans l'historique                            |
| `GoBackBackEvent`    | Remonter dans l'historique jusqu'à une route différente de la courante   |
| `ExitProgramEvent`   | Quitter l'application                                                    |
| `FormResultEvent<T>` | Transporter le résultat d'un formulaire (un DTO, un ID sélectionné...)   |

`FormResultEvent<T>` est le seul Event qui transporte de la donnée. Il est générique : `T` peut être un `AddCampFormData`,
un `Integer` (ID sélectionné), un `Person`, etc. Le controller inspecte le type de `T` avec `instanceof` pour décider
quoi en faire.

---

## Commandes (`Command`)

### Rôle

Les commandes sont des **actions utilisateur préfixées par `!`** tapées dans les menus ou formulaires. Elles permettent
d'interagir sans passer par les options numérotées du menu.

### Fonctionnement

```
Input utilisateur "!sort 2:DESC"
        │
        ▼
CommandManager.convertInput(input)
        │
        ├─ Détecte le préfixe "!"
        ├─ Extrait le nom de commande ("sort") et les arguments ("2:DESC")
        ├─ Cherche dans ECommand le raccourci correspondant → SORT_COLUMN
        └─ Instancie SortColumnCommand avec les arguments parsés
```

### Commandes disponibles

| ECommand     | Raccourcis    | Classe               | Effet                                          |
|--------------|---------------|----------------------|-------------------------------------------------|
| `EXIT`       | `!exit`, `!e` | `ExitCommand`        | Quitter l'application                           |
| `BACK`       | `!back`, `!b` | `BackCommand`        | Revenir en arrière                              |
| `BACK_BACK`  | `!bb`         | `BackBackCommand`    | Remonter jusqu'à une route différente           |
| `SORT_COLUMN`| `!sort`, `!s` | `SortColumnCommand`  | Trier par colonne (accepte des arguments)       |

### Traitement

La commande instanciée est passée au `CommandHandler` du menu courant, qui décide quoi en faire :
- Le `KinomichiStandardMenu` mappe `BackCommand` → `GoBackEvent`, `ExitCommand` → `ExitProgramEvent`
- Le `ModelListMenu` gère en plus `SortColumnCommand` pour le tri dynamique
- Si une commande n'est pas gérée → `UnhandledCommandException` → message d'erreur

---

## Menus (`Menu`)

### Hiérarchie

```
Menu (abstract)                              — Base : hooks, cycle use(), display abstrait
└─ OrderedMenu (abstract)                    — Options numérotées, boucle d'input, CommandHandler, unoptionedRows
   └─ StandardMenu (abstract)                — Rendu en table, titre coloré, options "Retour"/"Quitter" auto-ajoutées
      └─ KinomichiStandardMenu               — Préconfigure le CommandHandler (Back→GoBackEvent, Exit→ExitProgramEvent)

ModelMenu<M> (abstract, extends OrderedMenu) — Base pour les menus qui affichent des modèles en table
├─ ModelListMenu<M>                          — Liste de modèles, gère !sort
└─ ModelDetailMenu<M>                        — Détail d'un modèle, gère !b et !bb
```

### Cycle de vie (`menu.use()`)

Le menu suit un cycle orchestré par des **hooks** — des fonctions optionnelles injectées par la View qui le crée.
Chaque hook peut court-circuiter le cycle en retournant une `MenuResponse` non nulle :

```
use()
 ├─ beforeUse()           — Avant tout (ex: vérification de prérequis)
 ├─ beforeDisplay()       — Avant l'affichage (ex: StandardMenu y ajoute les options Retour/Quitter)
 ├─ display()             — Affiche le menu dans le terminal
 ├─ afterDisplay()        — Après l'affichage (ex: afficher un message contextuel)
 ├─ [boucle d'input]      — Attend un choix valide ou une commande
 ├─ afterValidInput()     — Après un choix valide (ex: logique post-sélection)
 └─ beforeUseExit()       — Nettoyage avant de retourner (toujours appelé)
```

### `MenuResponse`

Le résultat de `menu.use()` est un `MenuResponse` qui encapsule un objet arbitraire — généralement un `Event`
(CallUrlEvent, GoBackEvent...) ou un résultat de commande. La View qui a créé le menu extrait cet objet et le retourne
en tant qu'Event.

---

## Views (`View`)

### Hiérarchie

```
View (abstract)                  — Méthode abstraite render() → Event
├─ FormView (abstract)           — Base pour les formulaires, fournit promptField() et getModelTable()
├─ ModelView<M>                  — Détail d'un modèle via ModelDetailMenu
├─ ModelDetailView<M>            — Détail d'un modèle (variante)
└─ ModelListView<M>              — Liste tabulaire via ModelListMenu
```

### Rôle

Une View est responsable de l'**affichage et de l'interaction** avec l'utilisateur. Elle :

- Crée et configure un ou plusieurs menus
- Appelle `menu.use()` pour afficher et attendre l'input
- Extrait l'Event du `MenuResponse` et le retourne au Controller

La View ne connaît ni le routing ni les DataManagers — elle reçoit les données déjà préparées par le Controller.

### `FormView`

`FormView` est la base des formulaires de saisie. Elle fournit :

- **`promptField(scanner, fieldHandlers, field)`** — Affiche le label d'un champ, lit l'input, appelle le setter.
  Si le setter lance une exception, l'erreur est affichée et le champ est re-demandé.
- **`FieldHandler(label, inputConsumer)`** — Associe un label d'affichage à un `ThrowingConsumer<String>` (le setter)
- **`FormViewField`** — Interface marqueur pour les enums de champs (chaque formulaire définit son propre enum)

---

## Affichage tabulaire (`ModelTable` + `ModelTableFormatter`)

### `ModelTable<T>`

Classe abstraite qui définit **quelles colonnes afficher** pour un modèle donné. Chaque modèle a sa propre
implémentation (ex: `ClubModelTable`, `PersonModelTable`).

Les méthodes annotées `@ModelTableDisplay` définissent chaque colonne :

```java
@ModelTableDisplay(name = "Nom", order = 2)
public String getName() {
    return this.getModel().getName();
}
```

- `name` → titre de la colonne
- `order` → position de la colonne dans la table
- `format` → options de formatage optionnelles (couleur, alignement, preset)

### `ModelTableFormatter`

Classe utilitaire qui **génère une `Table`** à partir d'une liste de modèles ou d'un modèle unique :

- `forList(items)` → découvre par reflection les `@ModelTableDisplay` sur le `ModelTable` correspondant, invoque chaque
  méthode pour chaque item, et construit une `Table` avec une colonne par annotation
- `forDetail(item)` → même principe mais en format clé/valeur (vertical)
- `comparatorForColumn(class, index)` → retourne un `Comparator` basé sur la valeur d'une colonne (utilisé par `!sort`)
### `Table`

La classe `Table` est le moteur de rendu tabulaire générique. Elle prend des `Column` (avec header, valeurs, formatage)
et produit un affichage en colonnes alignées avec bordures Unicode. Options configurables via `TableOptions` :
`SEPARATE_COLUMNS`, `SEPARATE_HEADER`, `DISPLAY_HEADER`, `BOX_AROUND`.

### Convention de nommage

Le `ModelTable` est résolu par reflection depuis le nom du modèle :
`Club` → cherche `ClubModelTable` dans le package `app.models.formatting.table`
---

## Formatage de texte ANSI (`TextFormatter`)

### Rôle

Système de formatage de texte pour le terminal. Produit des séquences d'échappement ANSI pour la couleur, le style et
l'alignement.

### Classes

| Classe                   | Rôle                                                                              |
|--------------------------|-----------------------------------------------------------------------------------|
| `TextFormatter`          | Façade statique : `bold(...)`, `red(...)`, `italic(...)`, `format(options, ...)`  |
| `FormattedText`          | Texte composé de `Segment`s, chacun avec son propre formatage                     |
| `TextFormattingOptions`  | Options complètes : couleur, fond, styles (bold/italic/underline), alignement     |
| `TextFormattingPreset`   | Interface pour des presets réutilisables (ex: `ModelPrimaryKeyTextFormattingPreset`) |
| `TextColor`              | Enum des couleurs ANSI (RED, BLUE, MAGENTA...)                                    |
| `TextBackgroundColor`    | Enum des couleurs de fond ANSI                                                    |
| `TextStyle`              | Enum des styles (BOLD, ITALIC, UNDERLINE, DIM, BLINK, REVERSE, HIDDEN, STRIKETHROUGH) |
| `TextAlignment`          | Enum (LEFT, CENTER, RIGHT, NONE)                                                  |

### `FormattedText` et `Segment`

Un `FormattedText` n'est **pas** une simple String colorée — c'est une **liste ordonnée de `Segment`**. Chaque
`Segment` est un `record` `(String rawText, TextFormattingOptions formatting)` qui associe un morceau de texte brut à ses
propres options de formatage.

Cette architecture permet de **composer** des styles différents dans un même texte :

```java
// Chaque appel crée un FormattedText avec un Segment ayant son propre formatage
TextFormatter.bold("Personne ", TextFormatter.blue("#42"), " ajoutée")
```

Produit un `FormattedText` avec 3 segments :

```
Segment 1 : rawText = "Personne "   formatting = {bold}
Segment 2 : rawText = "#42"         formatting = {bold, blue}    ← les styles se combinent
Segment 3 : rawText = " ajoutée"    formatting = {bold}
```

#### Composition (merge de formatage)

Quand un `FormattedText` est passé comme argument à un autre appel de formatage (ex: `bold(blue("#42"))`), les styles
ne s'écrasent pas : ils **fusionnent** via `withMergedFormatting()`. Le formatage extérieur (bold) est ajouté à chaque
Segment existant du `FormattedText` intérieur (blue), tant qu'il n'y a pas de conflit. Règle de priorité : le formatage
le plus interne l'emporte (la couleur blue du Segment interne n'est pas écrasée par une couleur extérieure).

#### Rendu en String (`toString()`)

A l'affichage, chaque Segment génère ses propres séquences ANSI :

```
\033[1m Personne \033[22m \033[1;34m #42 \033[22;39m \033[1m ajoutée \033[22m
  │                          │                          │
  bold on/off                bold+blue on / reset both  bold on/off
```

#### Alignement

`TextFormatter` fournit aussi des méthodes d'alignement (`alignLeft`, `alignRight`, `center`) qui ajoutent des Segments
d'espaces en début ou fin du `FormattedText` pour atteindre une largeur cible. La méthode `visibleLength()` calcule la
largeur visible (sans les séquences ANSI) pour un alignement correct.
