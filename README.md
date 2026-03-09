# AdoptMe 🐾

**AdoptMe** is a Tinder-style pet adoption application for Android that connects potential pet adopters with shelters and rescue organizations.

---

## 🚀 Key Features

* **User Authentication**: Secure email/password registration, login, and persistent sessions using Firebase Auth.
* **Pet Discovery**: Tinder-style swipe interface with animated card transitions, dynamic loading spinners, and custom scaling.
* **Smart Filtering**: Location-based search (GPS with Israel boundary detection) with adjustable radius, pet type (Dog/Cat), and size preferences.
* **Favorites System**: Save liked pets, view them in a grid-based interface, and contact shelters directly via email or phone.
* **User Preferences**: Adjustable filter settings persisted in Firestore.
* **Modern UI/UX**: Built with Material Design 3, smooth animations, and a responsive custom blue theme.

---

## 🛠️ Technology Stack

* **Language**: Java
* **Architecture**: Single Activity with Navigation Component
* **Backend**: Firebase (Auth, Firestore, Storage)
* **Image Loading**: Glide
* **Location**: Google Play Services (Location/Maps)
* **UI**: Material Design 3, ConstraintLayout, XML Animations

---

## 🏗️ Project Structure

```text
AdoptMe/
├── app/
│   ├── src/main/java/com/example/adoptme/
│   │   ├── MainActivity.java              # Main entry point
│   │   ├── Pet.java                       # Data model
│   │   ├── Authentication/                # Login/Register logic
│   │   ├── Main Features/                 # Explore/Favorites/Profile
│   │   └── Adapters/                      # RecyclerView logic
│   └── res/                               # Layouts, Navigation, Animations
└── build.gradle.kts                       # Build configuration

---
## 🔧 Technical Implementations

* **Custom Gesture Detection**: Smooth card swiping with rotation and scale effects using `MotionEvent`.
* **Navigation Back-Stack**: Clear navigation graph management to prevent returning to authentication screens after login.
* **Location Services**: Integrated GPS tracking with a hardcoded fallback to Tel Aviv for emulator consistency.
* **Dynamic Loading**: Custom rotating `RotateAnimation` for image loading feedback.
* **Dialog Interaction**: Custom dialogs for shelter contact info that control the swiping flow.

## ⚙️ How to Run

1. **Clone the repo**: `git clone [your-repo-link]`
2. **Setup Firebase**: Create a project in Firebase Console, enable Auth/Firestore, and add your `google-services.json` to the `app/` folder.
3. **Build**: Open in **Android Studio** and sync Gradle.
4. **Emulator Settings**: If you encounter a black screen, edit your AVD and change Graphics to "Software - GLES 2.0".

## 💡 Future Enhancements

- [ ] Real-time in-app chat with shelters.
- [ ] Advanced search filters (breed, age, special needs).
- [ ] Push notifications for new pets.
- [ ] Map-based view of nearby pets.
- [ ] Admin panel for shelter management.

## 🛠️ Troubleshooting Guide

* **Black Screen (Emulator)**: Switch graphics to "Software - GLES 2.0" and Cold Boot.
* **No Pets Found**: Verify location permissions and Firestore filter settings.
* **Card Stuck Off-Center**: Ensure initial centered coordinates (`cardInitialX/Y`) are captured once via `view.post()`.
* **Firebase Errors**: Ensure your `google-services.json` is correctly placed and SHA-1 keys are added to your Firebase project.
