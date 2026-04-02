# Ajout de params aux URLs du Router

## Problème actuel

Les informations comme le tri (`sort`) sont encodees dans le path de l'URL (ex: `/camps/list/sort/1:DESC,2`), ce qui oblige a :
- Construire manuellement le segment de path (`buildSortPathSegment()`) — duplique dans `ModelListView` et `SelectCampView`
- Parser manuellement le segment dans chaque controller (`parseSortParameter()`)
- Avoir des regex de route complexes avec groupes optionnels (`(?:/sort/(?<sort>.+))?`)

## Idee

Ajouter un systeme de query params (ou un objet params arbitraire) au routing :
- `CallUrlEvent("/camps/list", Map.of("sort", sortOrders))` au lieu de `CallUrlEvent("/camps/list/sort/1:DESC,2")`
- Le `Request` porterait directement les params types, sans parsing manuel
- Plus besoin de `buildSortPathSegment()` nulle part
- Les routes deviennent plus simples : `/camps/list` sans regex de sort

## Ce que ca resoudrait

- Duplication de `buildSortPathSegment()` (ModelListView + SelectCampView)
- Simplification des regex de routes
- Parsing centralise dans le Router plutot que dans chaque controller
- Possibilite de passer des objets types (pas juste des strings) entre vues/controllers

## Ce que ca ne resoudrait PAS

- Duplication du pattern "get DataManager or GoBackEvent" dans les controllers (-> extraire une methode dans Controller)
- Duplication du formulaire adresse entre AddCampView et AddClubView (-> extraire un composant AddressFormFragment)
