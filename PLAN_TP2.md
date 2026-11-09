# Plan : suite du TP2 KanaQuiz

## Contexte

Le générateur de quiz est terminé et testé (`business/quiz/QuizModels.kt`, `business/quiz/QuizGenerator.kt`, règle de grade `≤ g`), mais rien n'est visible dans l'application : `MainActivity` affiche seulement `LearningScreen`.

Il reste à livrer : les écrans de quiz, le stockage Room, le grade utilisateur sur une fenêtre glissante, le graphique de progression et les tests instrumentés.

Le travail se fait dans `../kana-quiz`, package `com.enssat.kanaquiz.mgaillar_adely`. Rien n'est commité sans demande explicite.

## Étape 1 : navigation et écrans de quiz

- Ajouter `navigation-compose` et remplacer l'affichage direct de `LearningScreen` dans `MainActivity` par un `NavHost`.
- Routes : accueil, apprentissage (`LearningScreen` existant, inchangé), paramètres, quiz, résultat, progression.
- `ui/home/HomeScreen` : grade cliquable, boutons « Lancer un quiz » et « Apprendre ».
- `ui/quiz/QuizSetupScreen` :
  - type de caractère ;
  - type de question, filtré avec `QuestionType.supports` ;
  - grade parmi `KANJI_GRADES`, affiché seulement pour les kanji ;
  - interrupteurs aide et text-to-speech.
- `ui/quiz/QuizViewModel` et `QuizScreen` :
  - `AndroidViewModel`, comme `LearningViewModel` ;
  - données chargées avec `KanaRepository.loadKanas` et `loadKanjis`, quiz créé avec `QuizGenerator` ;
  - affichage : 10 indicateurs (correcte, incorrecte, pas encore répondu), question, aide éventuelle, 4 réponses.
- Après un clic : mise à jour des indicateurs, retour couleur bref (environ 800 ms) pour laisser le temps à `speechAfterAnswer`, puis question suivante.
- Voix : réutiliser `tts/JapaneseSpeechService`, avec `speechBeforeAnswer` à l'affichage de la question.
- Écran de résultat : score, grade après le quiz, rejouer ou revenir à l'accueil.

## Étape 2 : stockage Room

- Ajouter Room et KSP.
- **Point à vérifier en premier :** la compatibilité de KSP avec AGP 9.4.1 et Kotlin 2.2.10 (le projet utilise le Kotlin intégré à AGP), car c'est le seul vrai risque de build.
- `data/db/QuizResultEntity` : `id`, `timestamp`, `charType`, `questionType`, `quizGrade`, `withHelp`, `score`, `isFake`.
- `QuizResultDao` : `insert`, `insertAll`, `observeAll` (`Flow`, trié par date), `getFake`, `deleteFake` (`WHERE isFake = 1`).
- `AppDatabase` en singleton et `QuizResultRepository`.
- Enregistrement à la fin d'un quiz uniquement ; un quiz abandonné ne compte pas.

## Étape 3 : calcul du grade

`stats/GradeCalculator`, en fonctions pures avec des tests unitaires :

- **Score d'un quiz :** `score / 10`, multiplié par 0,8 si l'aide était activée.
- **Fenêtre :** les 20 derniers quiz.
- **Niveau validé :** au moins 3 quiz de ce niveau dans la fenêtre et au moins 80 % de moyenne.
- **Grade :** le plus haut niveau validé (0 si aucun), plus une partie décimale vers le niveau suivant : `min(n / 3, 1) × min(moyenne / 0,8, 1)`, où `n` et `moyenne` portent sur les quiz de ce niveau suivant.
- Le niveau suivant de 6 est 8 (le grade 7 n'existe pas), donc la progression s'étale sur 2 points entre eux.
- `gradeHistory(results)` rejoue l'historique et donne un point par quiz.
- La justification de ce choix (fenêtre glissante, pas de pénalité pour les révisions faciles, décimale motivante) sera écrite dans un commentaire et un court paragraphe de rendu.

## Étape 4 : page de progression

- `ui/progress/ProgressScreen` : grade actuel, niveau suivant, meilleur grade atteint.
- `ProgressChart` en `Canvas` Compose : courbe, axes, point final mis en avant, état vide.
- `testTag` et `contentDescription` sur le graphique (nombre de points, grade final) pour le test.
- Accueil branché sur le `Flow` Room : le grade se met à jour tout seul après un quiz.

## Étape 5 : tests instrumentés

Dans `androidTest`, sur la vraie base :

- `SeedFakeResultsTest` :
  - insère des résultats `isFake = true` ;
  - vérifie le grade calculé sur ces seuls résultats (`getFake`), pour ne pas dépendre de l'historique réel ;
  - affiche `ProgressChart` et vérifie sa présence et sa description ;
  - ne nettoie pas, volontairement, pour pouvoir voir les données dans l'app.
- `CleanFakeResultsTest` : insère un résultat réel marqué par un horodatage unique, lance `deleteFake`, vérifie qu'il n'y a plus de factices et que le résultat réel a survécu, puis retire ce seul résultat.

**À savoir :** `./gradlew connectedAndroidTest` désinstalle l'app à la fin, donc efface la base. Pour garder les données factices, lancer le test depuis Android Studio.

## Vérification

- `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDebugUnitTest` à chaque étape.
- `:app:connectedDebugAndroidTest` sur un émulateur ou un téléphone pour l'étape 5.
- Parcours manuel : lancer un quiz de chaque type, vérifier aide et voix, finir un quiz, voir le grade changer à l'accueil, ouvrir la progression, relancer l'app pour confirmer la persistance.
