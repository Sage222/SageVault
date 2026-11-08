# SageVault

**SageVault** is a clean, dark-themed Android application for managing physical cards—such as library cards, gym keytags, health insurance IDs, and club memberships—using QR/barcode scanning and local photo storage.

---

## Downloading & Installing the APK

> **Why is `SageVault.apk` generated via GitHub Actions?**  
> AI Studio's "Push to GitHub" exports source code files and excludes large binary `.apk` artifacts during the initial push. This repository includes an automated GitHub Actions workflow ([`.github/workflows/build-apk.yml`](./.github/workflows/build-apk.yml)) that automatically compiles **`SageVault.apk`**, commits it directly to the repository root and `releases/` folder, and publishes a **GitHub Release** as soon as code is pushed.

### Option 1: Download from GitHub (After Pushing)
Once pushed to GitHub, wait ~2 minutes for the **Build & Publish SageVault APK** action (under the **Actions** tab) to finish, then download from any of these locations:
- **Direct Repo File**: [`SageVault.apk`](./SageVault.apk) or [`releases/SageVault.apk`](./releases/SageVault.apk)
- **GitHub Releases Sidebar**: Open the **Releases** section on the right side of your GitHub repository (`v1.0.0`) and tap **`SageVault.apk`**.
- **GitHub Actions Artifact**: Open the **Actions** tab -> latest workflow run -> download **`SageVault-APK`**.

### Option 2: Download Directly from AI Studio
1. Open the **Settings / Export** menu in the top-right corner of Google AI Studio.
2. Select **Download APK** (or **Generate APK**) to download the compiled APK directly to your phone or computer.

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

## Building Locally from Source

```bash
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
