name: Build Android APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '18'

      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Install dependencies
        run: npm install

      - name: Add Android platform
        run: npx cap add android

      - name: Overlay native files
        run: |
          cp native/AndroidManifest.xml android/app/src/main/AndroidManifest.xml
          mkdir -p android/app/src/main/java/com/example/desktoppet
          cp native/java/com/example/desktoppet/*.java android/app/src/main/java/com/example/desktoppet/

      - name: Sync Capacitor
        run: npx cap sync android

      - name: Build debug APK
        run: cd android && chmod +x gradlew && ./gradlew assembleDebug

      - uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: android/app/build/outputs/apk/debug/app-debug.apk
