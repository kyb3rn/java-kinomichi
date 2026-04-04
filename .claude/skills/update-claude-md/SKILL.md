---
name: update-claude-md
description: Relit la codebase depuis la dernière mise à jour du CLAUDE.md (via git) et met à jour le CLAUDE.md en conséquence.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read Grep Glob Bash Edit
---

# Mise à jour du CLAUDE.md

Tu dois relire la codebase et mettre à jour le `CLAUDE.md` pour qu'il reflète l'état actuel du code.

## Étape 1 : Identifier les changements depuis la dernière mise à jour

1. Lis le `CLAUDE.md` et récupère le datetime dans la ligne `> Dernière mise à jour : YYYY-MM-DD HH:MM` (première ligne du fichier).
2. Utilise git pour trouver tous les fichiers modifiés depuis ce datetime :
   ```
   git log --since="YYYY-MM-DD HH:MM" --name-only --pretty=format: -- src/
   ```
3. Déduplique et trie la liste des fichiers modifiés.

## Étape 2 : Relire le code modifié

1. Lis chaque fichier modifié qui existe encore (ignore les fichiers supprimés).
2. Note les changements structurels :
   - Nouveaux packages, classes, interfaces, enums
   - Classes renommées, déplacées ou supprimées
   - Nouvelles hiérarchies d'héritage
   - Nouveaux patterns ou conventions
   - Changements dans les controllers, views, routes, models, DataManagers
   - Changements dans les utilitaires

## Étape 3 : Mettre à jour le CLAUDE.md

1. Mets à jour chaque section du `CLAUDE.md` pour refléter l'état actuel :
   - Ajoute les nouvelles classes/packages dans les listes existantes
   - Supprime les références à du code qui n'existe plus
   - Documente les nouveaux patterns si pertinents
   - Mets à jour les hiérarchies de classes si elles ont changé
2. **Ne réécris PAS tout le fichier** — fais des édits ciblés avec l'outil Edit.
3. Respecte le style existant du document (même format, même niveau de détail).
4. Mets à jour la date en première ligne : `> Dernière mise à jour : YYYY-MM-DD HH:MM` (datetime du moment présent).

## Règles

- Ne documente que ce qui est **structurel et non-évident** depuis le code (architecture, patterns, conventions, hiérarchies). Pas de documentation ligne par ligne.
- Le CLAUDE.md est un guide pour comprendre l'architecture, pas une javadoc.
- Garde le même style concis et factuel que le reste du document.
- Si aucun changement structurel significatif n'a été fait, dis-le et mets juste à jour la date.
