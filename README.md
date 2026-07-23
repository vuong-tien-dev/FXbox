# 🚀 FXbox - Android TikTok & YouTube Downloader & Media Player

<p align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="120" alt="FXbox Logo"/>
  <br>
  <b>An ultra-high performance, feature-packed Android application for downloading, managing, and playing media from TikTok & YouTube.</b>
  <br>
  <i>Crafted with passion & high-level architecture by Vuong Tien Dev (vuong-tien-dev)</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Java%20%7C%20Kotlin-blue.svg" alt="Language"/>
  <img src="https://img.shields.io/badge/MinSDK-API%2028-orange.svg" alt="MinSDK"/>
  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License"/>
</p>

---

## 📽️ Demo Showcase

**Demo 1**

https://github.com/user-attachments/assets/2e773512-316e-4745-b569-cbf61422cb1b

**Demo 2**

https://github.com/user-attachments/assets/7ec49d81-d671-45fd-b125-366c2c672eca

**Demo 3**

https://github.com/user-attachments/assets/f1c515ff-be82-4a77-815f-7efc8897aaae

---

## 🔥 Highlight Features

### 🎬 1. Advanced Multi-Mode Video Player & Quick Action Sheet
* **YouTube-Style Mini Player (`PlayerDragView`)**: Smoothly drag down to shrink the player into a floating bottom-right view while continuing to browse the app.
* **System-Wide Floating Window (PiP)**: Powered by `SmartyFloatyService` using `SYSTEM_ALERT_WINDOW` permission to play videos over any app.
* **Smart Accessibility Guard (`SmartyFloatyAccessibilityService`)**: Intelligently hides/restores the PiP window when system security dialogs or package installers pop up to prevent screen obstruction.
* **Interactive Quick Action Sheet (`FxVideoSelectionDialog`)**: A rich BottomSheet context menu featuring dynamic state toggles:
  * 📸 **Video Frame Capture**: Instant high-res video frame extraction.
  * 🔊 **Audio Booster & Mute Control**: Toggle mute/unmute and enhance audio levels beyond device defaults.
  * 🔄 **Auto-Swipe Toggle**: Hands-free auto-scrolling control.
  * ✂️ **Segment Switcher**: Seamlessly switch playback segments.
  * 🖼️ **PiP Mode & Media Limiting**: Fast launch to floating window or restrict video visibility.
  * 🔄 **Data Sync & Playlist Actions**: Sync likes/comments metadata, add to playlist, or inspect file properties.
* **Media Segment Clipping & Looper (`MediaSegment`)**: Split videos into custom segments (`CreateMediaSegmentDialog`, `ChangeMediaSegmentDialog`) by selecting start and end timestamps. ExoPlayer dynamically clips and loops specific choruses, highlights, or tutorial steps.

### 📱 2. Immersive Shorts & TikTok Browser
* **Auto-Swipe Mode**: Hands-free vertical scrolling that automatically plays the next video when the current one finishes.
* **TikTok Slideshow & Audio Merger**: Automatically parses photo galleries and pairs them with extracted background `.mp3` audio tracks.
* **CapCut Template Integration**: Detects CapCut template links and presents direct details to users.
* **Sensitive Content Warnings**: Built-in safety warnings (`Viewer Discretion Advised`, `Do Not Attempt`) for intense or stunt videos.

### 💬 3. Comment Extraction & Exporter (`FxCommentDialog`)
* **Nested Comment Trees**: View full comment threads complete with user avatars, usernames, like counts (`diggCount`), and nested replies.
* **Comment Downloader**: Download and export video comment threads directly to local storage.

### 👤 4. Creator Profiles & Dynamic Palette UI
* **Creator Analytics**: View full creator profile metrics including Followers, Following, Total Favorited/Likes, Bio, and unique user IDs.
* **Dynamic Palette API Integration**: Automatically extracts dominant colors from creator avatars to dynamically style the profile screen gradient background.
* **Creator Video Grid**: Seamless grid view displaying all uploaded videos by a specific creator.

### 🔍 5. Universal Smart Search & Playlist Management
* **Multi-Source Search**: Simultaneously search across local device storage and online TikTok/YouTube platforms.
* **Dynamic Filtering**: Filter search results by Name, Folder, or Creator/User.
* **Detailed Media Properties Inspector (`FxMediaPropertiesDialog`)**: Inspect full media metadata including thumbnail previews, FX internal ID, media type classification (Shorts, Photo Album, Device Video), exact file size, precise duration, creation date, and restriction status.
* **Custom Playlists & Batch Operations**: Create custom playlists, perform batch video additions/deletions, and manage your default Favorites list.
* **Watch History**: Automatically logs recently viewed videos with quick resume support.

### 📥 6. High-Performance Downloader (`FXDownloader`)
* **Foreground Service (`dataSync`)**: Ensures persistent, non-killed background downloading for large files.
* **Auto Network Recovery**: Auto-resumes paused downloads upon network reconnection via `ConnectivityManager.NetworkCallback`.
* **Multi-Format Extraction**: Download watermark-free `.mp4` videos or standalone `.mp3` audio tracks.

### 🔒 7. Privacy & Data Security
* **Private Vault & PIN Lock**: Protect private playlists and hidden videos with a custom PIN/Password lock.
* **RAM & Memory Guard**: Proactively monitors RAM usage and alerts users to maintain smooth ExoPlayer playback.
* **Database Backup & Restore (`BackupDialog`)**: Full SQLite Room DB import/export to safeguard playlists and history.

---

## 🛠️ Tech Stack & Architecture

* **Languages**: Java (Core Architecture) & Kotlin (UI Components)
* **Media Engine**: Google ExoPlayer `2.15.0` & Android Accessibility Services
* **UI Palette & Styling**: AndroidX Palette, Glide, Material Design 3, FadingEdgeLayout
* **Database**: Room SQLite Database `2.5.0`
* **Networking**: OkHttp3, Jsoup HTML Parser, Gson, RapidAPI Integration

---

## 🚀 Setup & Installation

### Prerequisites
* Android Studio Jellyfish | 2023.3.1 or newer
* Android SDK 34 (Minimum API 28)

### Build Instructions
1. **Clone the repository**:
   ```bash
   git clone https://github.com/vuong-tien-dev/FXbox.git
   cd FXbox
   ```

2. **Configure API Keys**:
   Add your RapidAPI Keys inside `local.properties` (ignored by Git):
   ```properties
   RAPID_API_KEYS=your_key_1,your_key_2
   ```

3. **Build & Run**:
   Open in Android Studio, sync Gradle, and press **Run**.

---

## 👨‍💻 Author

**Vuong Tien Dev**
* GitHub: [@vuong-tien-dev](https://github.com/vuong-tien-dev)

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for more information.
