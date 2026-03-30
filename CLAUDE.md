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
