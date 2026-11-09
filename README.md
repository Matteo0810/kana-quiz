# KanaQuiz

Application Android pour apprendre les hiragana, les katakana et les kanji, puis tester ses connaissances avec des quiz de 10 questions.

## Calcul du grade

Le grade est calculé séparément pour les hiragana, les katakana et les kanji :

1. On prend les **20 derniers quiz terminés** du type de caractères choisi, tous types de questions confondus.
2. Pour chaque niveau, on calcule la moyenne des scores des quiz de ce niveau : `moyenne = somme des scores / nombre de quiz`.
3. Un niveau est validé s’il comporte **au moins 3 quiz** et une **moyenne supérieure ou égale à 8/10**.
4. Le grade affiché est le **plus haut niveau validé**, ou **0** si aucun niveau n’est validé.

Les quiz avec ou sans aide comptent de la même façon. Il n’y a pas de partie décimale dans le calcul, même si l’interface affiche deux décimales.

**Exemple :** trois quiz de kanji de niveau 2 avec les scores 7/10, 8/10 et 9/10 donnent une moyenne de `(7 + 8 + 9) / 3 = 8/10`. Le niveau 2 est donc validé : le grade vaut 2 si aucun niveau supérieur n’est validé. Avec seulement deux quiz, ce niveau ne serait pas encore validé.

Il n’est pas nécessaire de valider les niveaux inférieurs avant un niveau supérieur. Les kana ont uniquement le niveau 1 ; les niveaux de kanji sont 1, 2, 3, 4, 5, 6 et 8 (pas de niveau 7 dans les données). Après le niveau 6, le prochain niveau est donc 8.

Le grade peut baisser si la moyenne baisse ou si d’anciens quiz sortent des 20 derniers résultats. Le graphique recalcule le grade après chaque quiz ; le meilleur grade correspond au maximum atteint dans cet historique.

## Compilation et tests

Avec le JDK d’Android Studio :

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Voir [TP2_IMPLEMENTATION.md](TP2_IMPLEMENTATION.md) pour les détails du parcours, du stockage et des tests instrumentés.
