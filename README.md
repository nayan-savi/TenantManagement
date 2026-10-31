# 🏢 Tenant Manager - Android App

A modern, offline-first Android application for property owners and land managers to manage tenants, property details, rental income, agreements, and annual rent revisions. Built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**.

---

## 📱 App Highlights & Features

### 📊 1. Dashboard & Financial Analytics
- **At-a-Glance Stat Grid**: Quick view of Active Tenants, Total Buildings, Total Properties/Flats, and Total Monthly Income.
- **Financial Breakdown**:
  - **Total Monthly Rent**: Sum of active monthly rental income.
  - **Total Advance Held**: Total security deposit held across active tenants.
  - **Estimated Annual Income**: Projected yearly revenue calculation (`Monthly Rent × 12`).
- **Tenant Status Summary**: Real-time breakdown of Active, Notice Period, and Vacated tenants.
- **Indian Currency Formatting**: Standardized formatting (e.g., `₹1,06,900`, `₹4,30,000`, `₹12,82,800`) with responsive typography for large figures.

---

### 👥 2. Tenant Management
- **Complete Profile Management**: Register & update tenant details including Name, Flat/House Number, Building Name, Joining Date, Monthly Rent, Security Deposit, Aadhar Number, Phone Number, Email, and Notes.
- **Aadhar Formatting**: Auto-formatted 12-digit Aadhar input (`XXXX XXXX XXXX`).
- **Lease Agreement Storage**: Attach, view, and store PDF/image lease agreements directly inside private app storage.
- **Status Tracking**: Categorize tenants into `Active`, `Notice Period`, or `Vacated`.
- **Search & Filtering**: Instant search by Name, Flat, Building, or Aadhar number, with filter chips.

---

### 🏢 3. Building & Property Management
- **Multi-Building Support**: Store building names, locality/addresses, total floors/units, owner names, and owner email addresses.
- **Flat Details**: Track EC Account Numbers, RR Numbers, Water Meter Numbers, and standalone properties.
- **Occupancy Tracking**: Displays total flats and active tenant count per building.

---

### 📈 4. Annual Rent Revision & Forecasting Engine
- **Anniversary Tracker**: Calculates tenant tenancy anniversaries from joining dates.
- **Increment Calculations**: Supports percentage (`%`) or fixed amount (`₹`) annual increments.
- **One-Tap Rent Update**: Enables the **Update Rent** action during anniversary months.

---

### 💬 5. WhatsApp & Communication Integration
- **Pre-filled WhatsApp Notices**: One-tap action to launch WhatsApp with a pre-filled formal notice containing a complete breakdown:
  ```text
  *RENT REVISION & BREAKDOWN NOTICE*

  Dear *[Tenant Name]*,

  This is a formal notice regarding the annual rent revision for Flat #[Flat Number] ([Building Name]).

  • Tenant Name: [Tenant Name]
  • Joined Date: [Joining Date]
  • Tenancy Cycle: Year [Year Number]
  • Previous Rent: ₹[Previous Rent]/month
  • Yearly Increment: [Increment Rate]
  • New Updated Rent: ₹[Forecast Rent]/month
  • Effective Date: 01/[MM/YYYY] (1st of Anniversary Month)

  Best regards,
  *[Building Owner Name]*
  ```
- **Notice Sent Tracking**: Automatically tracks sent notices so buttons update to `"✓ Notice Sent"`.
- **Direct Calls & Email**: One-tap phone dialer launch and pre-filled email notices.

---

### 🎨 6. Side Navigation Drawer & Theme Selection
- **Navigation Drawer**: Hamburger menu providing access to Dashboard, Tenants, Buildings, and App Info.
- **App Theme Switcher**: Persisted support for:
  - ☀️ **Light Mode**
  - 🌙 **Dark Mode**
  - 📱 **System Default**
- **Notification Badges**: Real-time badge count on the navigation item for pending renewal notices.

---

### 🔒 7. 100% Offline Data Privacy & Safety
- **Local Storage Guarantee**:
  - **Room Database**: `/data/data/com.example.tenantmanagement/databases/tenant_database`
  - **Document Storage**: `/data/data/com.example.tenantmanagement/files/agreements/`
  - **SharedPreferences**: `/data/data/com.example.tenantmanagement/shared_prefs/tenant_mgmt_prefs.xml`
- **Zero Cloud Uploads**: All financial figures, documents, and contact details remain strictly on your mobile device.
- **Confirmation Popups**: Explicit confirmation dialogs before saving any Tenant, Flat, or Building edits.

---

## 🛠️ Technical Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library (SQLite)
- **Background Tasks**: WorkManager (Anniversary reminders & notifications)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow & Kotlin Coroutines
- **Build System**: Android Gradle Plugin (AGP) 8.x + Gradle KTS
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15) / Compile SDK 37

---

## 🏗️ How to Build & Run

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 35/37

### Building via Terminal
1. Clone or open the repository in Android Studio.
2. Build the **Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   *Generated File:* `app/build/outputs/apk/debug/Tenant_Management.apk`

3. Build the **Signed Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
   *Generated File:* `app/build/outputs/apk/release/Tenant_Management.apk`

---

## 📄 License & Version
- **Current Version**: `v1.2` (`versionCode = 3`)
- **Developer**: Nayan Kumar
