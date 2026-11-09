# KanaQuiz — TP2

## Parcours

Accueil → configuration → quiz de 10 questions → résultat → progression.
L’apprentissage existant reste accessible depuis l’accueil.

Les écrans utilisent Compose et les composants Material du projet. La navigation et la session sont conservées dans un ViewModel lors des changements de configuration. Après destruction du processus, l’application revient à l’accueil ; seuls les quiz terminés et enregistrés sont persistants.

Après chaque réponse, les choix sont verrouillés et la correction reste visible jusqu’au bouton « Question suivante ». La voix ne révèle pas la réponse avant le choix et utilise une vitesse constante dans les quiz. L’apprentissage conserve son alternance normal/lent. Une voix japonaise doit être installée sur l’appareil.

## Stockage

SQLite natif avec SQLiteOpenHelper, sans dépendance supplémentaire. Le fichier privé `databases/kanaquiz.db` contient les résultats. Les lectures et écritures passent par Dispatchers.IO. Un StateFlow actualise l’accueil et la progression après sauvegarde.

Chaque session possède un identifiant unique, clé primaire en base : retenter une sauvegarde ne crée pas de doublon. Le résultat est enregistré à la validation de la dernière correction (« Voir mon résultat »). Un abandon n’enregistre rien. En cas d’échec de sauvegarde, le quiz reste affiché pour permettre de réessayer.

## Progression

Le calcul du grade est décrit dans le [README](README.md#calcul-du-grade). Il retient le plus haut niveau ayant au moins 3 quiz et une moyenne de 8/10 parmi les 20 derniers quiz du type sélectionné, avec ou sans aide. Le grade est entier, sans progression décimale.

Le graphique affiche un point par quiz, dans l’ordre chronologique, et le meilleur grade est calculé sur l’historique complet.

## Vérification

Avec le JDK d’Android Studio :

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

Si les dépendances du lanceur de tests Gradle ne sont pas disponibles hors ligne, après compilation des APK et avec `adb` dans le PATH :

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.enssat.kanaquiz.mgaillar_adely.test/androidx.test.runner.AndroidJUnitRunner
```

Les tests unitaires couvrent le parsing, le générateur, le calcul du grade et les transitions de session. Les tests instrumentés couvrent SQLite après réouverture, les doublons, la suppression sélective des résultats factices, le graphique vide ou renseigné, la correction, le parcours complet avec recréation d’activité et l’abandon.

Les tests SQLite utilisent des fichiers temporaires supprimés en fin de test. Le test du parcours utilise la base de l’application et retire uniquement le résultat de sa propre session. Aucun jeu de démonstration n’est laissé dans l’historique.

Référence pour le stockage : [documentation Android SQLite](https://developer.android.com/training/data-storage/sqlite).
