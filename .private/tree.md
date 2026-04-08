# Arborescence des dossiers

```
java-kinomichi/
├── data/                            — Fichiers de donnees (JSON/CSV) charges par les DataManagers
└── src/main/java/
    ├── app/                         — Code applicatif (logique metier, UI, navigation)
    │   ├── controllers/             — Controllers (actions des routes, orchestration views/models)
    │   ├── events/                  — Events de navigation et de retour (CallUrl, GoBack, Exit, FormResult...)
    │   ├── middlewares/             — Middlewares de route (verification avant dispatch)
    │   ├── models/                  — Modeles metier (Camp, Person, Club, Session, Dinner, Lodging...)
    │   │   ├── formatting/          — Formatage tabulaire des modeles (presets de style, formatter)
    │   │   │   └── table/           — Classes ModelTable par modele (colonnes d'affichage via @ModelTableDisplay)
    │   │   └── managers/            — DataManagers (CRUD, persistence, resolution des references)
    │   ├── routing/                 — Systeme de routing (Router, Route, Request, ControllerAction)
    │   ├── utils/                   — Utilitaires applicatifs
    │   │   ├── elements/            — Elements de domaine reutilisables
    │   │   │   ├── money/           — Montants et devises (MoneyAmount, Price, Currency)
    │   │   │   │   └── exceptions/  — Exceptions liees aux devises
    │   │   │   └── time/            — Intervalles temporels (TimeSlot)
    │   │   ├── helpers/             — Fonctions utilitaires applicatives (KinomichiFunctions)
    │   │   ├── menus/               — Surcouches de menus applicatifs (KinomichiStandardMenu, ModelMenu, ModelListMenu, ModelDetailMenu)
    │   │   └── tarification/        — Systeme de tarification et reductions (ChargeElement, ChargingElement, ChargeableElement)
    │   └── views/                   — Vues (rendu terminal, formulaires, menus)
    │       └── [by controller]/     — Un sous-dossier par controller (persons/, camps/, clubs/, etc.)
    └── utils/                       — Bibliotheque reutilisable (independante de l'app)
        ├── data_management/         — Gestion des donnees (lecture/ecriture fichiers)
        │   ├── converters/          — Interfaces de serialisation (CustomSerializable, Hydratable)
        │   │   ├── convertibles/    — Interfaces de conversion par format (JSON, CSV, XML)
        │   │   │   └── parseables/  — Interfaces de parsing web
        │   │   ├── enums/           — Enums de configuration (sites web pour readers)
        │   │   ├── readers/         — Lecteurs par format (JSON, CSV, XML, HTML, fichier, web)
        │   │   └── writers/         — Ecrivains par format (JSON, CSV, XML, fichier)
        │   └── parsing/             — Parsing de chaines (StringParser)
        ├── helpers/                 — Fonctions utilitaires generiques
        │   └── validation/          — Validators et exceptions de validation
        └── io/                      — Entrees/sorties terminal
            ├── commands/            — Systeme de commandes utilisateur (!exit, !back, !sort...)
            │   ├── exceptions/      — Exceptions liees aux commandes
            │   └── list/            — Implementations des commandes (Back, Exit, Sort...)
            ├── menus/               — Systeme de menus generique (Menu, OrderedMenu, StandardMenu)
            ├── tables/              — Affichage tabulaire generique (Table, SimpleBox)
            └── text_formatting/     — Formatage de texte ANSI (couleurs, styles, alignement)
```
