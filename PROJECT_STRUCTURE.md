# HarmonyCare Project Structure

## Complete File List

### Root Files
- `build.gradle` - Project-level build configuration
- `settings.gradle` - Gradle settings
- `gradle.properties` - Gradle properties
- `.gitignore` - Git ignore rules
- `README.md` - Project documentation

### App Module (`app/`)

#### Build Configuration
- `app/build.gradle` - App-level build configuration with dependencies
- `app/proguard-rules.pro` - ProGuard rules

#### Manifest
- `app/src/main/AndroidManifest.xml` - Application manifest with all activities and permissions

#### Java Source Files (`app/src/main/java/com/harmonycare/app/`)

**Application Class:**
- `HarmonyCareApplication.java` - Application class

**Data Layer:**
- `data/database/AppDatabase.java` - Room database
- `data/database/UserDao.java` - User data access
- `data/database/EmergencyDao.java` - Emergency data access
- `data/database/VolunteerStatusDao.java` - Volunteer status data access

**Models:**
- `data/model/User.java` - User entity
- `data/model/Emergency.java` - Emergency entity
- `data/model/VolunteerStatus.java` - Volunteer status entity

**Repositories:**
- `data/repository/UserRepository.java` - User repository
- `data/repository/EmergencyRepository.java` - Emergency repository
- `data/repository/VolunteerStatusRepository.java` - Volunteer status repository

**ViewModels:**
- `viewmodel/AuthViewModel.java` - Authentication ViewModel
- `viewmodel/ElderlyViewModel.java` - Elderly ViewModel
- `viewmodel/VolunteerViewModel.java` - Volunteer ViewModel
- `viewmodel/EmergencyViewModel.java` - Emergency ViewModel

**Activities (Views):**
- `view/LoginActivity.java` - Login screen
- `view/RegisterActivity.java` - Registration screen
- `view/ElderlyDashboardActivity.java` - Elderly dashboard with SOS button
- `view/VolunteerDashboardActivity.java` - Volunteer dashboard
- `view/EmergencyHistoryActivity.java` - Emergency history list
- `view/VolunteerEmergencyListActivity.java` - Active emergencies for volunteers
- `view/EmergencyDetailsActivity.java` - Emergency details and navigation
- `view/SettingsActivity.java` - Settings screen

**Utilities:**
- `util/LocationHelper.java` - Location services helper
- `util/DistanceCalculator.java` - Distance calculation (Haversine)
- `util/NotificationHelper.java` - Local notifications helper
- `util/TTSHelper.java` - Text-to-Speech helper

#### Resources (`app/src/main/res/`)

**Layouts:**
- `layout/activity_login.xml` - Login screen layout
- `layout/activity_register.xml` - Registration screen layout
- `layout/activity_elderly_dashboard.xml` - Elderly dashboard layout
- `layout/activity_volunteer_dashboard.xml` - Volunteer dashboard layout
- `layout/activity_emergency_history.xml` - History screen layout
- `layout/activity_volunteer_emergency_list.xml` - Emergency list layout
- `layout/activity_emergency_details.xml` - Emergency details layout
- `layout/activity_settings.xml` - Settings screen layout
- `layout/item_emergency_history.xml` - History item layout
- `layout/item_volunteer_emergency.xml` - Emergency list item layout

**Values:**
- `values/strings.xml` - String resources
- `values/colors.xml` - Color resources
- `values/themes.xml` - Theme definitions

**Drawables:**
- `drawable/ic_notification.xml` - Notification icon

**Mipmaps:**
- `mipmap-*/ic_launcher.xml` - Launcher icon configurations

## Features Implemented

✅ Complete MVVM architecture
✅ SQLite database with Room
✅ User authentication (local)
✅ Dual user roles (Elderly & Volunteer)
✅ GPS location services
✅ Emergency SOS functionality
✅ Distance calculation
✅ Google Maps navigation
✅ Local notifications
✅ Text-to-Speech
✅ Emergency history
✅ Availability toggle for volunteers
✅ Large text mode for accessibility
✅ Clean, modern UI

## Database Tables

1. **users** - User accounts (elderly/volunteer)
2. **emergencies** - Emergency requests with location and status
3. **volunteer_status** - Volunteer availability status

## Navigation Flow

1. Login/Register → Dashboard (role-based)
2. Elderly: Dashboard → SOS → History/Settings
3. Volunteer: Dashboard → Emergency List → Details → Navigation

## Permissions

- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- POST_NOTIFICATIONS
- INTERNET (for Maps)

## Dependencies

- AndroidX AppCompat
- Material Design Components
- Room Database
- Lifecycle Components (ViewModel, LiveData)
- ConstraintLayout

