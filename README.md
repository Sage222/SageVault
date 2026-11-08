# SageVault

**SageVault** is a clean, dark-themed Android application for managing physical cards—such as library cards, gym keytags, health insurance IDs, and club memberships—using QR/barcode scanning and local photo storage.

---

## Pre-built APK

A ready-to-install APK is included directly in this repository:
- **[`SageVault.apk`](./SageVault.apk)** (also available at [`releases/SageVault.apk`](./releases/SageVault.apk))

### How to Install on Your Android Phone
1. Download **`SageVault.apk`** from this repository onto your Android device (or export it via the AI Studio **Settings / Export -> Generate APK / Push to GitHub** menu).
2. Open the downloaded `SageVault.apk` file on your phone.
3. If prompted by Android, allow installation from your browser or file manager, then tap **Install**.

---

## Key Features

- **Live QR & Barcode Scanner (CameraX + ML Kit)**
  - Real-time scanning for **QR Code**, **Code 128**, **Code 39**, **EAN-13**, **UPC-A**, **PDF417**, **Aztec**, and **Codabar**.
  - Includes a built-in flashlight/torch toggle for dim environments.
  - **Scan from Photo**: Extract barcodes or QR codes directly from an existing photo or screenshot using the zero-permission Android Photo Picker.
- **High-Contrast Optical Scanner Pass**
  - Renders crisp 1D and 2D barcodes on a pure white optical pad using **ZXing**.
  - **Automatic Scanner Brightness Boost**: Temporarily maximizes screen brightness while viewing a card pass so library desk scanners and gym turnstiles read the code reliably.
  - One-tap copy for member, policy, or card ID numbers.
- **Front & Back Physical Card Photos**
  - Snap photos of the front and back of any card using your camera or select images from your gallery.
  - Includes an interactive Front/Back toggle and a fullscreen zoomable photo viewer.
- **Custom Categories**
  - Pre-configured with **Library**, **Gym & Fitness**, **Insurance**, **Medical & Dental**, **Membership & Club**, and **Retail & Rewards**.
  - Create, rename, recolor, and delete custom categories with 12 category icons and 10 vibrant dark-mode accent colors.
- **Rename & Reorganize Favorites**
  - Tap the **Rename (pencil)** icon on any card across the Vault, Favorites tab, or Pass modal to rename it immediately.
  - Pin important cards to **Favorites** and reorder them (**Move Up**, **Move Down**, or **Move to #1 Top**) so your most-used cards are always first.
- **100% Offline & Private (Room SQLite)**
  - All card data, custom categories, and card photos are stored locally on your device.

---

## Tech Stack

- **UI Framework**: Jetpack Compose + Material Design 3 (Obsidian & Sage Dark Theme)
- **Typography**: Locally bundled Google Fonts (`Space Grotesk`, `Plus Jakarta Sans`, `JetBrains Mono`)
- **Camera & Barcode Scanning**: AndroidX CameraX + Google ML Kit Barcode Scanning (`com.google.mlkit:barcode-scanning`)
- **Barcode Generation**: ZXing Core (`com.google.zxing:core`)
- **Local Persistence**: AndroidX Room Database (`androidx.room`) with Kotlin Coroutines & `StateFlow`
- **Image Loading**: Coil Compose (`io.coil-kt:coil-compose`) & Android Photo Picker (`PickVisualMedia`)

---

## Building from Source

```bash
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
