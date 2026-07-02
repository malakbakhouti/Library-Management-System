#!/bin/bash
# run.sh — Version qui marche à 100% sur Mac

# Chemin vers ton projet
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Chemin vers le mysql-connector (adapte si besoin)
CONNECTOR="$PROJECT_DIR/lib/mysql-connector-j-9.4.0.jar"
# Si tu n'as pas de dossier lib, mets le chemin complet :
# CONNECTOR="/Users/malak/Downloads/mysql-connector-j-9.4.0.jar"

# Vérifie que le JAR existe
if [ ! -f "$CONNECTOR" ]; then
    echo "ERREUR : mysql-connector-j-*.jar non trouvé !"
    echo "Place-le dans un dossier lib/ ou change le chemin dans run.sh"
    exit 1
fi

echo "Lancement de l'application Bibliothèque..."

java --module-path "/Users/malak/Desktop/javafx-sdk-17.0.18/lib" \
     --add-modules javafx.controls,javafx.fxml \
     -cp ".:$CONNECTOR:$PROJECT_DIR/bin" \
     app.Main