# VoiceCloning Pro - Android App

Eine professionelle Android-Anwendung zur Stimmklonierung mit modernster KI-Technologie. Diese App ermöglicht es Benutzern, ihre eigene Stimme mit minimalen Audioaufnahmen zu klonen und für Text-zu-Sprache-Synthese zu verwenden.

## 🚀 Features

### 🎤 Professionelle Audioaufnahme
- Hochqualitative Audioaufnahme mit Echtzeit-Wellenform-Visualisierung
- Integrierte Audio-Verbesserungstools:
  - Rauschunterdrückung
  - Audio-Normalisierung
  - Equalizer-Einstellungen
- Unterstützung für Datei-Upload (WAV, MP3, M4A)
- Aufnahme-Templates mit vordefinierten deutschen Texten

### 🧠 KI-basierte Stimmklonierung
- Training mit wenigen Audiosamples (minimum 3, empfohlen 5-10)
- Fortschrittsverfolgung während des Trainings
- Lokale Verarbeitung für maximalen Datenschutz
- Optimierte Modelle für verschiedene Qualitätsstufen

### 🗣️ Text-zu-Sprache (TTS)
- Echtzeit-Textkonvertierung mit geklonter Stimme
- Unterstützung für deutsche Sprache
- Audio-Export und Sharing-Funktionen
- Vordefinierte Text-Vorlagen
- Bis zu 500 Zeichen pro Generierung

### ⚙️ Erweiterte Einstellungen
- Anpassbare Audio-Qualität (Sample-Rate, Bit-Rate)
- Modell-Qualitätseinstellungen (Schnell bis Perfekt)
- Verarbeitungsleistung-Kontrolle
- Speicher-Management
- Modell-Information und -Verwaltung

### 🎨 Moderne Benutzeroberfläche
- Material Design 3 mit ansprechenden Farbverläufen
- Responsive und intuitive Navigation
- Professionelle Card-basierte Layouts
- Echzeit-Audio-Visualisierung
- Animierte Fortschrittsanzeigen

## 📱 App-Struktur

### Hauptaktivitäten
1. **WelcomeActivity** - Onboarding und erste Einrichtung
2. **MainActivity** - Dashboard mit Hauptfunktionen
3. **RecordingActivity** - Audio-Aufnahme und -Verbesserung
4. **TrainingActivity** - Modell-Training mit Fortschritt
5. **TTSActivity** - Text-zu-Sprache-Generierung
6. **SettingsActivity** - Umfassende Einstellungen

### Kernkomponenten
- **AudioRecordingManager** - Verwaltet Audioaufnahmen
- **AudioProcessor** - Implementiert Audio-Verbesserungsalgorithmen
- **WaveformView** - Benutzerdefinierte Wellenform-Visualisierung
- **VoiceCloningModel** - Simuliert ML-basierte Stimmklonierung
- **PreferencesManager** - Verwaltet App-Einstellungen

## 🛠️ Technische Details

### Abhängigkeiten
- **Android Architecture Components** (Navigation, Lifecycle, ViewModel)
- **Material Design 3** für moderne UI-Komponenten
- **TensorFlow Lite** für Machine Learning (simuliert)
- **Media API** für Audio-Aufnahme und -Wiedergabe
- **Preferences** für Einstellungsverwaltung
- **FlexboxLayout** für responsive Layouts

### Berechtigungen
- `RECORD_AUDIO` - Für Audioaufnahmen
- `WRITE_EXTERNAL_STORAGE` - Für Dateispeicherung
- `READ_EXTERNAL_STORAGE` - Für Datei-Upload
- `INTERNET` - Für mögliche Modell-Downloads
- `WAKE_LOCK` - Für Hintergrundverarbeitung

### Unterstützte Audioformate
- **Aufnahme**: WAV (44.1kHz, 16-bit)
- **Import**: WAV, MP3, M4A
- **Export**: WAV (anpassbare Qualität)

## 📋 Verwendung

### 1. Erste Schritte
1. App starten und durch das Onboarding navigieren
2. Berechtigungen für Mikrofon und Speicher gewähren
3. Dashboard öffnet sich mit vier Hauptfunktionen

### 2. Audioaufnahme
1. **"Aufnahme"** im Dashboard auswählen
2. Vorgegebene Texte laut und deutlich vorlesen
3. Audio-Verbesserungstools nach Bedarf anwenden
4. Aufnahmen zum Trainingsset hinzufügen

### 3. Modell-Training
1. **"Training"** im Dashboard auswählen
2. Mindestens 3 Audioaufnahmen bereitstellen
3. Training starten und Fortschritt verfolgen
4. Abwarten bis Training abgeschlossen ist

### 4. Text-zu-Sprache
1. **"Text zu Sprache"** im Dashboard auswählen
2. Text eingeben (max. 500 Zeichen)
3. Audio generieren lassen
4. Wiedergabe, Speicherung oder Teilen

### 5. Einstellungen
1. **"Einstellungen"** für erweiterte Konfiguration
2. Audio-Qualität und Modell-Parameter anpassen
3. Modell-Informationen anzeigen
4. Cache und Daten verwalten

## 🔒 Datenschutz

- **Lokale Verarbeitung**: Alle Audio- und Modell-Daten bleiben auf dem Gerät
- **Keine Cloud-Übertragung**: Kein Upload von Sprachdaten
- **Sichere Speicherung**: Verschlüsselte lokale Dateispeicherung
- **Benutzer-Kontrolle**: Vollständige Kontrolle über alle Daten

## 📊 Qualitätsmetriken

Die App implementiert professionelle Audio-Qualitätsanalysen:
- **RMS-Werte** für Lautstärkekonsistenz
- **Signal-zu-Rausch-Verhältnis** für Aufnahmequalität
- **Dynamikbereich** für Audio-Klarheit
- **Spektrale Zentroide** für Klangcharakteristika

## 🎯 Zielgruppe

- Content-Ersteller und Podcaster
- Personen mit Sprachbeeinträchtigungen
- Entwickler und KI-Enthusiasten
- Professionelle Audio-Produzenten
- Alle, die ihre Stimme digitalisieren möchten

## 📈 Performance

- **Minimaler Speicherbedarf**: Optimierte Modellgrößen
- **Schnelle Verarbeitung**: Multi-Threading für Performance
- **Batterieeffizienz**: Adaptive Verarbeitungsleistung
- **Skalierbare Qualität**: Von schnell bis perfekt

---

**Entwickelt mit ❤️ für beste Benutzerfreundlichkeit und professionelle Ergebnisse.**