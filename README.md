# 🚀 FXbox — Media Ecosystem

<p align="center">
  <img src="assets/logo.png" width="120" alt="FXbox Logo"/>
  <br>
  <b>An ultra-high performance, feature-packed multimedia ecosystem for TikTok & YouTube media playback, downloading, curation, and cross-device sync.</b>
  <br>
  <i>Crafted with passion & high-level architecture by Vuong Tien Dev (<a href="https://github.com/vuong-tien-dev">@vuong-tien-dev</a>)</i>
</p>

<p align="center">
  <a href="https://github.com/vuong-tien-dev/FXbox-Android"><img src="https://img.shields.io/badge/Client-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Client"/></a>
  <a href="https://github.com/vuong-tien-dev/FXbox-Desktop"><img src="https://img.shields.io/badge/Client-Desktop%20%28Electron%2FVite%29-1DB954?style=for-the-badge&logo=spotify&logoColor=white" alt="Desktop Client"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License"/></a>
</p>

---

## 🌐 Official Repositories

The FXbox project is modularized into dedicated client repositories. Click below to explore the source code, releases, and documentation for each platform:

| Platform | Repository | Description | Tech Stack |
| :--- | :--- | :--- | :--- |
| 📱 **Android Client** | [**vuong-tien-dev/FXbox-Android**](https://github.com/vuong-tien-dev/FXbox-Android) | Native Android mobile application with mini-player, background PiP, and TikTok/YouTube integration | Java, Kotlin, ExoPlayer, Room DB, Material 3 |
| 💻 **Desktop Client** | [**vuong-tien-dev/FXbox-Desktop**](https://github.com/vuong-tien-dev/FXbox-Desktop) | Premium Spotify-inspired desktop player with LAN sync, multi-media carousel, and video comments | React 18, TypeScript, Tailwind CSS, Lucide |

---

## 📽️ Demo Showcase

Experience the FXbox interface and media playback in action:

### 🎬 Demo 1: Core Feed & Seamless Playback
https://github.com/user-attachments/assets/2e773512-316e-4745-b569-cbf61422cb1b

### 📱 Demo 2: Picture-in-Picture & Mini Player Controls
https://github.com/user-attachments/assets/7ec49d81-d671-45fd-b125-366c2c672eca

### 💬 Demo 3: Interactive Comments & Downloader
https://github.com/user-attachments/assets/f1c515ff-be82-4a77-815f-7efc8897aaae

---

## 🔥 Key Highlights

### 🎬 1. Multi-Mode Video Player
* **YouTube-Style Mini Player**: Smoothly drag down to shrink into a floating mini-player while continuing navigation.
* **System-Wide Floating Window (PiP)**: Plays media over any application with intelligent screen obstruction prevention.
* **Media Segment Clipping & Looper**: Split videos into custom segments to dynamically loop specific highlights or choruses.

### 📱 2. Immersive Shorts Browser
* **Auto-Swipe Mode**: Hands-free vertical scrolling that auto-plays the next item when the current one finishes.
* **TikTok Slideshow & Audio Merger**: Automatically parses photo galleries and pairs them with background audio tracks.
* **CapCut Template Integration & Sensitive Content Warnings**: Built-in safety alerts and template detection.

### 💬 3. Comment Threads & Media Exporter
* **Nested Comment Trees**: Full comment threads with user avatars, usernames, and like counts (`diggCount`).
* **Comment Downloader**: Export media comment threads directly to local storage.

### 🔄 4. Cross-Device Desktop LAN Sync
* **Instant Media Push**: Push videos, comments, and media playlists from the Android client directly to the Desktop player across local Wi-Fi.
* **Zero Cloud Latency**: Pure local network communication for maximum speed and privacy.

### 🔍 5. Universal Smart Search & Playlist Management
* **Multi-Source Search**: Search across local device storage and online platforms.
* **Smart Organization**: Custom playlists, default Favorites, and full watch history.

### 🔒 6. Privacy & Security
* **Private Vault & PIN Lock**: Safeguard private media collections behind password authentication.
* **Database Backup & Restore**: Robust SQLite Room database import and export.

---

## 👨‍💻 Author

**Vuong Tien Dev**
* GitHub: [@vuong-tien-dev](https://github.com/vuong-tien-dev)

---

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.
