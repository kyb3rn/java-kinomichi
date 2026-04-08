# Cycle de vie de l'application

## Phase 1 — Démarrage

```
Main.main()
│
├─ 1. DataManagers.initAll()
│     │
│     ├─ Pass 1 : Instanciation + chargement des données
│     │     Pour chaque DataManager (dans l'ordre de dépendance) :
│     │     ├─ Instanciation via reflection (constructeur sans argument)
│     │     ├─ init() → lit le fichier JSON/CSV depuis /data/
│     │     ├─ hydrate() → crée les modèles avec des pendingXxxId non résolus
│     │     └─ Les modèles sont stockés dans pendingModels (dataLoaded = true)
```

> *Les `pendingModels` sont une zone de transit. A ce stade, les modèles contiennent des clés primaires brutes
> (ex: `pendingAddressId = 3`) mais pas encore les objets Java correspondants (ex: `Address`). On ne peut pas résoudre
> ces références immédiatement car le DataManager cible n'est peut-être pas encore chargé. On stocke donc les modèles
> "en attente" pour les compléter une fois que tous les DataManager ont lu leurs fichiers.*

```
│     │
│     ├─ Pass 2 : Résolution des références
│     │     Pour chaque DataManager chargé :
│     │     ├─ resolveReferences() parcourt les pendingModels
│     │     ├─ Pour chaque champ @ModelReference défini dans le modèle :
│     │     │     ├─ Lit le champ pendingXxxId par reflection
│     │     │     └─ Appelle setXxxFromPk() par reflection → résout la FK
│     │     └─ Vérifie isValid() sur chaque modèle résolu puis, si vrai :
│     │           └─ addResolvedModel() → l'insère dans la TreeMap finale
```

> *Maintenant que tous les fichiers ont été lus (passe 1), chaque `pendingXxxId` peut être transformé en référence
> Java réelle. Par exemple, un Club avec `pendingAddressId = 3` appelle `setAddressFromPk(3)` qui interroge
> l'`AddressDataManager` (déjà chargé) pour obtenir l'objet `Address` correspondant. Une fois toutes les FK résolues,
> le modèle est complet et valide : il quitte la liste `pendingModels` pour rejoindre la `TreeMap` finale du
> DataManager, prêt à être utilisé par l'application.*

```
│     │
│     └─ Pass 3 : Validation post-chargement
│           Pour chaque DataManager dont dataLoaded = true :
│           ├─ initialized = true
│           ├─ validateResolvedModels() → validations croisées inter-DM
│           └─ Si échec : initialized = false (DM inutilisable)
```

> *Les passes 1 et 2 garantissent que chaque modèle est complet individuellement (toutes ses FK pointent vers des
> objets réels). Mais certaines règles métier dépendent de **plusieurs** DataManagers à la fois — par exemple, vérifier
> qu'un formateur de session est bien affilié pendant la durée du stage implique de croiser `SessionTrainerDataManager`
> et `AffiliationDataManager`. Ces validations ne peuvent pas avoir lieu pendant la résolution (passe 2) car l'autre
> DataManager n'est peut-être pas encore finalisé. La passe 3 intervient donc une fois que **tous** les DM sont résolus,
> pour effectuer ces vérifications croisées. Si elles échouent, le DM repasse à `initialized = false` et sera
> inutilisable dans l'application.*

```
│
├─ 2. CommandManager.loadCommands()
│     Enregistre les commandes : EXIT, BACK, BACK_BACK, SORT_COLUMN
│
├─ 3. Instanciation des Controllers
│
└─ 4. Enregistrement des Routes dans le Router
      Chaque Route associe : nom + regex de path + ControllerAction (+ middlewares)
```

## Phase 2 — Boucle principale

```
nextPath = "/"

while (nextPath != null)
│
├─ 1. NavigationHistory.push(nextPath)
│     Empile le path (ignore si identique au sommet)
│
├─ 2. Router.dispatch(nextPath)
│     │
│     ├─ 2a. Router.match(path)
│     │       Parcourt les routes enregistrées
│     │       ├─ Teste chaque regex contre le path
│     │       ├─ Extrait les paramètres nommés (ex: campId, sort)
│     │       └─ Retourne une Request (route matchée + paramètres)
│     │       OU lance RouteNotFoundException → retour à "/"
│     │
│     ├─ 2b. Exécution des Middlewares
│     │       Pour chaque Middleware attaché à la Route :
│     │       ├─ middleware.verify()
│     │       ├─ Si retourne null → passe au suivant
│     │       └─ Si retourne CallUrlEvent → court-circuite, redirige
│     │
│     └─ 2c. Exécution du Controller
│           route.getControllerAction().execute(request) → retourne un Event
│
│           (voir Phase 3 pour le détail)
│
└─ 3. Traitement de l'Event reçu
      │
      ├─ CallUrlEvent(url)      → nextPath = url (navigation vers une nouvelle page)
      ├─ GoBackEvent            → nextPath = NavigationHistory.goBack()
      ├─ GoBackBackEvent        → nextPath = NavigationHistory.goBackUntilDifferentRoute()
      ├─ ExitProgramEvent       → nextPath = null (sort de la boucle)
      └─ null                   → nextPath = null (sort de la boucle)
```

## Phase 3 — Dans le `Controller`

Le `Controller` est le chef d'orchestre entre les données et l'affichage. Il reçoit une `Request`, prépare ce qu'il faut,
délègue le rendu à une `View`, puis interprète ce que la `View` lui renvoie (un `Event`).

### Action de consultation (manage, dashboard)

```
CampController.manageCamp(request)
│
├─ 1. Extrait les paramètres de la Request (ex: campId)
│
├─ 2. Crée la View en lui passant les données nécessaires (ex: l'ID du camp à gérer)
│
├─ 3. view.render()
│     La View affiche un menu et attend un choix utilisateur. (voir Phase 4)
│
└─ → Event (remonte à la boucle principale)
```

### Action de liste triable (list, explore)

```
Controller.list(request)
│
├─ 1. Charge le DataManager
│
├─ 2. Lit les paramètres de tri depuis la Request
│     parseSortParameter(request) → ex: colonne 2 DESC, colonne 3 ASC
│
├─ 3. Trie les modèles
│     sortModels(models, modelClass, sortOrders)
│
├─ 4. Crée une ModelListView avec la liste triée → view.render()
│
└─ → Event (remonte à la boucle principale)
```

### Action d'écriture (add, modify, delete)

```
Controller.add(request)
│
├─ 1. Crée la vue formulaire → view.render()
│     L'utilisateur remplit les champs un par un (voir Phase 5). La View retourne un Event.
│
├─ 2. Le controller inspecte l'Event reçu :
│     │
│     ├─ FormResultEvent<T> → l'utilisateur a terminé le formulaire
│     │     ├─ Charge le DataManager
│     │     ├─ Appelle la méthode d'écriture (addXxx / updateXxx / deleteXxx)
│     │     ├─ Affiche un message de confirmation
│     │     └─ Retourne CallUrlEvent vers la page parente (dashboard, manage...)
│     │
│     └─ Autre Event (GoBackEvent, ExitProgramEvent...)
│           → L'utilisateur a interrompu le formulaire. Le controller retourne l'Event tel quel, sans rien écrire.
│
└─ → Event (remonte à la boucle principale)
```

Le controller ne fait jamais d'affichage complexe lui-même — il délègue toujours à une View. Son rôle est de faire le
lien : lire la request, préparer les données, lancer la view, réagir au résultat.

## Phase 4 — Cycle de vie d'une `View` avec `Menu`

```
View.render()
│
├─ Crée un KinomichiStandardMenu (titre, options)
│     Chaque option est associée à un Object qui agit comme réponse (souvent un CallUrlEvent).
│     Le CommandHandler du menu est préconfiguré :
│     ├─ !b  (BackCommand)     → GoBackEvent
│     ├─ !bb (BackBackCommand) → GoBackBackEvent
│     └─ !e  (ExitCommand)     → ExitProgramEvent
│
└─ menu.use()
      │
      ├─ Hook: beforeUse()          → peut court-circuiter
      ├─ Hook: beforeDisplay()      → peut court-circuiter
      │
      ├─ menu.display()              → affiche le tableau des options dans le terminal
      │
      ├─ Hook: afterDisplay()       → peut court-circuiter
      │
      ├─ Boucle d'input (askingValidInputLoopHandle)
      │     │
      │     ├─ Hook: beforeInput()
      │     ├─ Lit l'input utilisateur ("> ")
      │     ├─ Hook: afterEveryInput(input)
      │     │
      │     ├─ Tentative de parsing comme commande (!xxx)
      │     │     CommandManager.convertInput(input)
      │     │     ├─ Si c'est une commande reconnue :
      │     │     │     commandHandler.handle(input, command)
      │     │     │     ├─ Si retourne un objet → MenuResponse immédiate
      │     │     │     └─ Si UnhandledCommandException → "commande non prise en charge"
      │     │     ├─ Si NotACommandException → continue vers le parsing numérique
      │     │     └─ Si UnknownCommandException → "commande inconnue"
      │     │
      │     ├─ Parsing numérique de l'input
      │     │     ├─ parseInt → index de l'option choisie
      │     │     └─ Validation des bornes (1..N)
      │     │
      │     └─ Si input invalide → message d'erreur, reboucle
      │
      ├─ Hook: afterValidInput(input, choice)  → peut court-circuiter
      ├─ Hook: beforeUseExit()
      │
      └─ Retourne MenuResponse (contient l'objet réponse de l'option choisie)

La View extrait l'Event depuis le MenuResponse et le retourne.
```

## Phase 5 — Formulaires (`FormView`)

```
FormView.render()
│
├─ Pour chaque champ du formulaire :
│     promptField(scanner, fieldHandlers, field)
│     │
│     ├─ Affiche le label du champ
│     ├─ Lit l'input utilisateur
│     │
│     ├─ Si l'input est une commande :
│     │     ├─ !b → lance GoBackEvent (interrompt le formulaire)
│     │     └─ !e → lance ExitProgramEvent (interrompt le formulaire)
│     │
│     ├─ Appelle le inputConsumer (setter du modèle/DTO)
│     │     ├─ Si le setter lance une exception → affiche l'erreur, re-demande le champ
│     │     └─ Si OK → passe au champ suivant
│     │
│     └─ (boucle tant que le champ n'est pas valide)
│
└─ Retourne FormResultEvent<T> avec le modèle/DTO rempli
```

## Phase 6 — Arrêt

```
(nextPath == null → sort de la boucle while)
│
├─ DataManagers.exportAll()
│     Pour chaque DataManager ayant hasUnsavedChanges() == true :
│     ├─ dehydrate() → convertit les modèles en DTOs
│     └─ Écrit le fichier JSON/CSV dans le dossier /data
│
└─ Affiche "Au revoir !"
```
