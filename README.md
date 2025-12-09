# HarmonyCare - Smart Elderly Safety Network

A complete Android application built with Java and XML, implementing MVVM architecture for elderly safety and volunteer coordination.

## Features

### Elderly User Features
- Large, accessible SOS button
- Automatic location capture using GPS
- Emergency request creation and tracking
- Emergency history view
- Text-to-Speech (TTS) voice feedback
- Large text mode for accessibility
- Local notifications

### Volunteer User Features
- Availability toggle
- View nearby emergency requests
- Distance calculation (Haversine formula)
- Accept emergency requests
- Navigate to emergency location via Google Maps
- Mark emergencies as completed
- View emergency history

## Architecture

- **MVVM (Model-View-ViewModel)** architecture
- **Room Database** for local SQLite storage
- **Repository pattern** for data access
- **LiveData** for reactive UI updates

## Database Schema

### Users Table
- id (Primary Key)
- name
- contact
- role (elderly/volunteer)
- password

### Emergencies Table
- id (Primary Key)
- elderly_id
- latitude
- longitude
- status (active/accepted/completed)
- volunteer_id
- timestamp

### Volunteer Status Table
- volunteer_id (Primary Key)
- is_available

## Setup Instructions

1. Open the project in Android Studio (2022+)
2. Sync Gradle files
3. Build the project
4. Run on an Android device or emulator (API 24+)

## Permissions Required

- `ACCESS_FINE_LOCATION` - For GPS location services
- `ACCESS_COARSE_LOCATION` - For approximate location
- `POST_NOTIFICATIONS` - For local notifications
- `INTERNET` - For Google Maps navigation

## Technologies Used

- Java
- XML layouts
- Room Database
- Android Location Services
- Local Notifications
- Text-to-Speech (TTS)
- Google Maps Intent (for navigation)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/harmonycare/app/
│   │   ├── data/
│   │   │   ├── database/     # Room database, DAOs
│   │   │   ├── model/        # Entity classes
│   │   │   └── repository/   # Repository classes
│   │   ├── view/             # Activities
│   │   ├── viewmodel/        # ViewModels
│   │   └── util/             # Utility classes
│   ├── res/
│   │   ├── layout/           # XML layouts
│   │   ├── values/           # Strings, colors, themes
│   │   └── drawable/         # Icons and drawables
│   └── AndroidManifest.xml
└── build.gradle
```

## Notes

- No Firebase or cloud services - all data is stored locally
- Location services require device GPS or network location
- Google Maps app is required for navigation (falls back to web if not installed)
- All authentication and data storage is local only

## License

This project is created for educational purposes.

