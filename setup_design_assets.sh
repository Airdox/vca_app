#!/bin/bash

echo "Starte Skript zur Erstellung von Design-Assets (Icons und Strings)."

BASE_RES_DIR="app/src/main/res"
DRAWABLE_DIR="$BASE_RES_DIR/drawable"
VALUES_DIR="$BASE_RES_DIR/values"
STRINGS_FILE="$VALUES_DIR/strings.xml"

# ----------------------------------------------------
# Schritt 4: Icons hinzufügen
# ----------------------------------------------------

echo "Überprüfe und erstelle Verzeichnis: $DRAWABLE_DIR"
mkdir -p "$DRAWABLE_DIR"

# ic_mic.xml
echo "Erstelle ic_mic.xml..."
cat <<EOF > "$DRAWABLE_DIR/ic_mic.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12,14c1.66,0 2.99,-1.34 2.99,-3L15,5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v6c0,1.66 1.34,3 3,3zM17.3,11c0,3.53 -2.61,6.44 -6,6.93V21h-2v-3.07c-3.39,-0.49 -6,-3.4 -6,-6.93h-2c0,4.17 3.06,7.6 7,8.35V21h4v-2.65c3.94,-0.75 7,-4.18 7,-8.35h-2z" />
</vector>
EOF
echo "ic_mic.xml erstellt."

# ic_stop.xml
echo "Erstelle ic_stop.xml..."
cat <<EOF > "$DRAWABLE_DIR/ic_stop.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M6,6h12v12H6V6z" />
</vector>
EOF
echo "ic_stop.xml erstellt."

# ic_play.xml
echo "Erstelle ic_play.xml..."
cat <<EOF > "$DRAWABLE_DIR/ic_play.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M8,5v14l11,-7z" />
</vector>
EOF
echo "ic_play.xml erstellt."

# ----------------------------------------------------
# Schritt 5: Strings anpassen
# ----------------------------------------------------

echo "Überprüfe und erstelle Verzeichnis: $VALUES_DIR"
mkdir -p "$VALUES_DIR"

echo "Aktualisiere strings.xml..."
cat <<EOF > "$STRINGS_FILE"
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">My Voice Cloning App</string>
</resources>
EOF
echo "strings.xml aktualisiert."

echo "Skript abgeschlossen. Design-Assets wurden erstellt/aktualisiert."