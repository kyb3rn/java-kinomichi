# Énoncé — Application de gestion de stages de Kinomichi

## Contexte

Application de gestion pour des **stages internationaux de Kinomichi**. L'application permet d'organiser plusieurs stages (camps), de gérer les inscriptions des personnes aux différentes prestations, et de calculer la facturation.

**Un seul utilisateur** (l'administrateur/organisateur) a accès à l'application — il n'y a pas de système de login ni d'inscription externe.

---

## Enums

| Enum | Valeurs | Description |
|---|---|---|
| `PersonCategory` | `PERSON`, `AFFILIATED`, `TRAINER`, `INVITED` | Catégorie tarifaire d'une personne pour un camp donné |
| `RegistrationStatus` | `ACTIVE`, `WITHDRAWAL` | Statut d'une inscription |
| `RoomType` | `STANDARD`, `SINGLE_ROOM` | Type de chambre pour un logement |

### Résolution de la catégorie d'une personne

La catégorie est déterminée **par camp** selon la priorité suivante (la première qui correspond s'applique) :

1. **`INVITED`** — la personne a une entrée dans Invitation pour ce camp
2. **`TRAINER`** — la personne est dans la liste `formateurs` d'au moins une session du camp
3. **`AFFILIATED`** — la personne est une instance d'Affiliated
4. **`PERSON`** — par défaut

---

## Interface Chargeable

Interface marqueur (sans méthode) implémentée par Session, Dinner et Lodging. Sert uniquement à typer les éléments facturables dans le code.

```
Chargeable (marker interface)
├── Session
├── Dinner
└── Lodging
```

> La logique de calcul des prix n'est **pas** sur les modèles eux-mêmes. Elle est centralisée dans un utilitaire `TarifCalculator` (voir section [Tarification](#tarification)).

---

## Modèles de données

### Country *(existe déjà)*

| Champ | Type | Description |
|---|---|---|
| name | `String` | Nom du pays |
| iso2 | `String` | Code ISO 3166-1 alpha-2 |
| iso3 | `String` | Code ISO 3166-1 alpha-3 — **clé primaire** |

### Address *(existe déjà)*

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| country | `Country` | Référence vers Country |
| zipCode | `String` | Code postal |
| city | `String` | Ville |
| street | `String` | Rue |
| number | `String` | Numéro |
| boxNumber | `String` | Boîte |

### Club *(existe déjà)*

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| name | `String` | Nom du club |
| address | `Address` | Référence vers Address |
| googleMapsLink | `String` | Lien Google Maps |

### Camp *(existe déjà — à compléter)*

Représente un stage. Un stage peut durer un week-end, une semaine, etc.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| name | `String` | Nom du stage |
| address | `Address` | Référence vers Address |
| timeSlot | `TimeSlot` | Période du stage (début / fin) |
| withdrawalRate | `int` | % facturé en cas de désistement (ex : `50` = 50%) |
| singleRoomSurcharge | `double` | Supplément chambre seule (ajouté au prix lodging standard) |

### Person

Entité de base représentant une personne.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| firstName | `String` | Prénom |
| lastName | `String` | Nom |
| phone | `String` | Téléphone |
| email | `String` | Email |

### Affiliated *(hérite de Person)*

Personne affiliée à un club. Hérite de tous les champs de Person.

| Champ | Type | Description |
|---|---|---|
| club | `Club` | Référence vers Club |
| affiliationNumber | `String` | Numéro d'affiliation |

### CampTarif

Grille tarifaire d'un camp. **Une entrée par catégorie par camp** (4 entrées par camp).

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| camp | `Camp` | Référence vers Camp |
| category | `PersonCategory` | Catégorie applicable |
| sessionPricePerHour | `double` | Tarif horaire par session |
| dinnerBasePrice | `double` | Prix fixe par souper |
| lodgingBasePrice | `double` | Prix fixe par nuitée (standard) |

Exemple pour un camp :

| Catégorie | Session (€/h) | Dinner (€) | Lodging (€) |
|---|---|---|---|
| `PERSON` | 15 | 25 | 30 |
| `AFFILIATED` | 12 | 20 | 25 |
| `TRAINER` | 8 | 20 | 25 |
| `INVITED` | 0 | 0 | 0 |

> Le prix d'une **chambre seule** = `lodgingBasePrice` + `camp.singleRoomSurcharge`.

### Session *(implémente Chargeable)*

Plage horaire de pratique de kinomichi au sein d'un camp. L'organisateur définit lui-même les sessions de chaque camp.

Organisation type : **samedi** 5 sessions, **dimanche** 3 sessions (variable selon le stage).

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| camp | `Camp` | Référence vers Camp |
| name | `String` | Nom / label de la session |
| timeSlot | `TimeSlot` | Plage horaire (début / fin) — fournit la durée |
| formateurs | `List<Person>` | Liste des formateurs de cette session |

> Les **participants** d'une session sont déterminés par les SessionRegistration pointant vers cette session.

### Dinner *(implémente Chargeable)*

Souper organisé durant le stage. Il peut y avoir **plusieurs soupers** par camp (un par jour, etc.).

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| camp | `Camp` | Référence vers Camp |
| label | `String` | Label (ex : "Souper du samedi") |
| timeSlot | `TimeSlot` | Plage horaire (début / fin) |

### Lodging *(implémente Chargeable)*

Nuitée / hébergement. Il peut y avoir **plusieurs nuitées** par camp. Facultatif pour les participants.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| camp | `Camp` | Référence vers Camp |
| label | `String` | Label (ex : "Nuit samedi → dimanche") |
| timeSlot | `TimeSlot` | Plage horaire (début / fin) |

### Invitation

Lie une Person à un Camp. Détermine que cette personne a la catégorie `INVITED` pour ce camp.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| camp | `Camp` | Référence vers Camp |
| person | `Person` | Référence vers Person |

### SessionRegistration

Inscription d'une Person à une Session.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| person | `Person` | Référence vers Person |
| session | `Session` | Référence vers Session |
| status | `RegistrationStatus` | `ACTIVE` ou `WITHDRAWAL` |

### DinnerRegistration

Inscription d'une Person à un Dinner.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| person | `Person` | Référence vers Person |
| dinner | `Dinner` | Référence vers Dinner |
| status | `RegistrationStatus` | `ACTIVE` ou `WITHDRAWAL` |

### LodgingRegistration

Inscription d'une Person à un Lodging.

| Champ | Type | Description |
|---|---|---|
| id | `int` | Clé primaire |
| person | `Person` | Référence vers Person |
| lodging | `Lodging` | Référence vers Lodging |
| roomType | `RoomType` | `STANDARD` ou `SINGLE_ROOM` |
| status | `RegistrationStatus` | `ACTIVE` ou `WITHDRAWAL` |

> **Suppression** : si l'administrateur supprime une inscription, l'enregistrement est simplement supprimé (pas de statut "supprimé").

---

## Tarification

### TarifCalculator

Utilitaire centralisé pour le calcul des prix. Prend une inscription et résout le tarif applicable.

**Logique** :
1. Déterminer le **camp** via l'élément Chargeable de l'inscription (session.camp, dinner.camp, lodging.camp)
2. Déterminer la **catégorie** de la personne pour ce camp (voir [résolution de catégorie](#résolution-de-la-catégorie-dune-personne))
3. Récupérer la ligne **CampTarif** correspondante (camp + catégorie)
4. Calculer le prix selon le type d'inscription :

| Inscription | Calcul |
|---|---|
| **SessionRegistration** | `campTarif.sessionPricePerHour` × durée de la session (heures) |
| **DinnerRegistration** | `campTarif.dinnerBasePrice` |
| **LodgingRegistration (STANDARD)** | `campTarif.lodgingBasePrice` |
| **LodgingRegistration (SINGLE_ROOM)** | `campTarif.lodgingBasePrice` + `camp.singleRoomSurcharge` |

5. Si `status == WITHDRAWAL` : prix × `camp.withdrawalRate` / 100

### Facturation totale par personne

Pour un camp donné, le total d'une personne = somme de `TarifCalculator.compute(registration)` pour toutes ses inscriptions (SessionRegistration + DinnerRegistration + LodgingRegistration) dans ce camp.

---

## Éléments facturables — Combinaisons possibles

Une personne peut s'inscrire à **n'importe quelle combinaison** de Chargeables :
- Uniquement des sessions (sans souper ni logement)
- Uniquement un ou plusieurs soupers (sans participer aux sessions)
- Uniquement un ou plusieurs logements (ex : enfants accompagnants)
- Toute autre combinaison

---

## Fonctionnalités

### Gestion des camps
- Créer, modifier, supprimer des camps
- Sélectionner le camp actif à gérer *(déjà implémenté)*
- Configurer la grille tarifaire du camp (CampTarif — une ligne par catégorie)
- Configurer le taux de désistement et le supplément chambre seule

### Gestion des personnes
- Créer, modifier, supprimer des personnes (Person et Affiliated)
- Lister les personnes avec leurs coordonnées et club

### Gestion des sessions
- Définir les sessions de chaque camp (nom, plage horaire)
- Assigner des formateurs à une session

### Gestion des soupers et logements
- Définir les soupers et logements de chaque camp (date, label)

### Gestion des inscriptions
- Inscrire une personne à une ou plusieurs prestations (session, souper, logement)
- Marquer une inscription comme désistement
- Supprimer une inscription
- Visualiser les inscriptions par camp / par personne

### Gestion des invitations
- Définir quelles personnes sont invitées à un camp donné

### Affichage et consultation
- Afficher le planning / horaires d'un camp
- Afficher la liste des participants avec coordonnées et club
- Afficher un récapitulatif de ce qui a été encodé (nombre de soupers, de logements à prévoir, etc.)
- Consulter la facturation par personne

---

## Notes techniques

- Les données sont stockées en **fichiers JSON** (un fichier par type d'entité, comme des tables de base de données).
- Chaque enregistrement lié à un camp contient une **référence vers l'ID du camp** (pas de fichier JSON séparé par camp).
- L'architecture suit les conventions de **reflection et nommage** du projet (voir `CLAUDE.md`).
