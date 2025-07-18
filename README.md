flutter_location_ffi
A high-performance Flutter plugin for accessing native location services using Dart FFI (Foreign Function Interface). By leveraging FFI and the result_channel package, flutter_location_ffi communicates directly with iOS (CoreLocation) and Android (Location Services) APIs, bypassing Method Channels for faster and more efficient location updates.

Features

Native Performance: Uses Dart FFI to call native location APIs directly, reducing latency compared to Method Channels.
Cross-Platform: Seamless support for iOS and Android with a consistent API.
Lightweight: Optimized for minimal CPU and memory usage, ideal for real-time location tracking.
Stream Support: Provides a stream for continuous location updates via onChanged.
Permission Management: Simplifies checking and requesting location permissions with a clear LocationPermission enum.
Settings Integration: Allows opening app and location settings for user convenience.

Getting Started
Prerequisites

Flutter 3.32
Dart 3.8
iOS 14.0 or higher
Android API Level 21 or higher

Installation
Add flutter_location_ffi to your pubspec.yaml:
dependencies:
flutter_location_ffi: ^0.9.0

Run flutter pub get to install the package.
Platform Setup
iOS

Add location permissions to ios/Runner/Info.plist:<key>NSLocationWhenInUseUsageDescription</key>
<string>Your app needs location access to provide real-time updates.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>Your app needs location access to function in the background.</string>


Ensure the CoreLocation framework is enabled in your Xcode project.

Android

Add location permissions to android/app/src/main/AndroidManifest.xml:<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" /> <!-- Optional for background location -->


Ensure your app targets at least API Level 21 and includes Google Play Services dependencies for location.

Usage
The flutter_location_ffi plugin provides a clean API for managing location permissions and accessing location data via FFI. Below are examples of how to use the plugin's main features. For a complete demonstration, see the example/ directory.
Checking Location Permission
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

void main() {
final permission = FlutterLocation.checkPermission();
switch (permission) {
case LocationPermission.granted:
print('Location permission granted');
case LocationPermission.denied:
print('Location permission denied');
case LocationPermission.neverAskAgain:
print('Location permission denied permanently');
}
}

Requesting Location Permission
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

Future<void> requestPermission() async {
try {
final permission = await FlutterLocation.checkAndRequestPermission();
switch (permission) {
case LocationPermission.granted:
print('Location permission granted');
case LocationPermission.denied:
print('Location permission denied');
case LocationPermission.neverAskAgain:
print('Location permission denied permanently');
}
} catch (e) {
print('Error requesting permission: $e');
}
}

Getting Current Location
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

Future<void> getLocation() async {
try {
final location = await FlutterLocation.getCurrent();
print('Location: (${location.latitude}, ${location.longitude})');
print('GPS Enabled: ${location.isGpsEnabled}');
print('Accuracy: ${location.accuracy}');
} catch (e) {
print('Error getting location: $e');
}
}

Streaming Location Updates
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

void streamLocations() {
FlutterLocation.onChanged.listen(
(location) {
print('New location: (${location.latitude}, ${location.longitude})');
print('GPS Enabled: ${location.isGpsEnabled}');
print('Accuracy: ${location.accuracy}');
},
onError: (error) {
print('Error: $error');
},
);
}

Opening Settings
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

void openSettings() {
// Open app-specific settings
FlutterLocation.openAppSettings();

// Open device location settings
FlutterLocation.openLocationSettings();
}

Example App
The example/ directory contains a sample Flutter app demonstrating how to use flutter_location_ffi to check permissions, request access, retrieve the current location, and stream location updates. Run the example to see the plugin in action.
Why Choose flutter_location_ffi?

Performance: Uses FFI to communicate directly with native APIs, reducing latency for location updates compared to Method Channels.
Robust Error Handling: Integrates with the result_channel package for reliable result and error management.
Simplicity: Clean API with a clear LocationPermission enum and LocationData class for easy integration.
Cross-Platform: Consistent behavior across iOS (using CoreLocation) and Android.

Contributing
Contributions are welcome! Please read our Contributing Guide for details on submitting issues, pull requests, or feature requests.

Fork the repository.
Create a feature branch (git checkout -b feature/new-feature).
Commit your changes (git commit -m 'Add new feature').
Push to the branch (git push origin feature/new-feature).
Open a pull request.

License
This project is licensed under the MIT License. See the LICENSE file for details.
Contact
For questions or feedback, reach out to JonathanVegasP or open an issue on GitHub.