# KanaQuiz

Application Android pour apprendre les hiragana, les katakana et les kanji.

Projet réalisé à l'ENSSAT par Matteo Gaillard et Alan Dely.

## Fonctionnalités

- Fiches des caractères avec lecture à voix haute
- Quiz de 10 questions à 4 choix, avec aide et voix en option
- Grade calculé à partir des derniers résultats
- Courbe de progression

## Technique

Kotlin, Jetpack Compose, Room, Android TextToSpeech. Android 7.0 minimum.

```
app/src/main/java/.../mgaillar_adely/
├── business/   génération des quiz, calcul du grade
├── data/       lecture des JSON, base Room
├── tts/        synthèse vocale
└── ui/         écrans Compose
```

## Calcul du grade

Le grade est calculé séparément pour les hiragana, les katakana et les kanji, sur les 20 derniers quiz du type concerné.

Un niveau est validé avec au moins 3 quiz de ce niveau et une moyenne d'au moins 8/10. Un quiz fait avec l'aide compte pour 80 % de son score.

La partie entière du grade est le plus haut niveau validé. La partie décimale indique l'avancement vers le niveau suivant :

```
fraction = min(n / 3, 1) × min(moyenne / 8, 1)
grade    = validé + fraction × (suivant − validé)
```

`n` et `moyenne` portent sur les quiz du niveau suivant.

Exemple : le niveau 1 est validé et deux quiz de niveau 2 ont une moyenne de 8/10. La fraction vaut 2/3, le grade 1,67.

Seuls les derniers résultats comptent, pour que le grade reflète le niveau actuel. Réviser un niveau facile ne le fait pas baisser, car chaque niveau a sa propre moyenne.

## Lancer le projet

Ouvrir le dossier dans Android Studio, ou utiliser le JDK d'Android Studio en ligne de commande :

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
```

## Tests

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

`SeedFakeResultsTest` ajoute des résultats factices en base et `CleanFakeResultsTest` les retire. Le détail est dans [TP2_IMPLEMENTATION.md](TP2_IMPLEMENTATION.md).

## Données

Les kanji et leurs informations viennent de [kanji-data](https://github.com/davidluzgouveia/kanji-data).
