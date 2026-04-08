# Kinomichi — Application CLI d'administration

Application de gestion pour le Kinomichi : stages, personnes, clubs, affiliations, sessions, repas, hébergements.
Java 21, Maven, terminal ANSI.

## Lancer l'application

```bash
mvn compile exec:java
```

## Structure du projet

- `src/main/java/app/` — Code applicatif (models, controllers, views, routing, events)
- `src/main/java/utils/` — Bibliothèque réutilisable (I/O, formatage, menus, commandes, validation)
- `data/` — Fichiers de données (JSON/CSV) chargés au démarrage

## Documentation

Le dossier `.private/` contient la documentation détaillée du projet :

| Fichier                                    | Contenu                                                        |
|--------------------------------------------|----------------------------------------------------------------|
| [`.private/tree.md`](.private/tree.md)     | Arborescence des dossiers avec description de chacun           |
| [`.private/app-lifecycle.md`](.private/app-lifecycle.md) | Cycle de vie complet de l'application (démarrage → arrêt) |
| [`.private/class-types.md`](.private/class-types.md)     | Explication des types de classes non triviaux               |

Le fichier `CLAUDE.md` à la racine contient les conventions, l'architecture et les règles de nommage du projet.
