import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_location_ffi/flutter_location_ffi.dart';

void main() {
  runApp(const App());
}

class App extends StatelessWidget {
  const App({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(title: 'flutter_location_ffi', home: MyApp());
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamSubscription<LocationData>? _subscription;
  var _currentLocationData = "";

  @override
  void initState() {
    _onInit();
    super.initState();
  }

  void _onInit() async {
    FlutterLocation.setSettings(
      const LocationSettings(
        priority: LocationPriority.medium,
        distanceFilter: 0,
        accuracyFilter: 200.0,

        /// When `minUpdateDistanceMeters`, `priority`, `accuracyFilter`, or `intervalMs` are set,
        /// they override the corresponding values in `distanceFilter`, `priority`, and `intervalMs`.
        androidLocationSettings: AndroidLocationSettings(
          priority: LocationPriority.best,
          accuracyFilter: 50.0,
          waitForAccurateLocation: true,
          maxUpdates: 0,
          durationMs: 0,
          minUpdateIntervalMs: 1000,
          maxUpdateDelayMs: 0,
          intervalMs: 1000,
          minUpdateDistanceMeters: 0,
          granularity: AndroidGranularity.permissionLevel,
          maxUpdateAgeMillis: 1000,
        ),

        /// When `distanceFilter`, `priority`, `accuracyFilter`, or `intervalMs` are set,
        /// they override the corresponding values in `distanceFilter`, `priority`, and `intervalMs`.
        appleLocationSettings: AppleLocationSettings(
          accuracyFilter: 200.0,
          distanceFilter: 1,
          priority: LocationPriority.medium,
          activityType: ActivityType.other,
          allowsBackgroundLocationUpdates: false,
          headingFilter: 0,
          pausesLocationUpdatesAutomatically: true,
          showBackgroundLocationIndicator: true,
        ),
      ),
    );

    _subscription?.cancel();

    final permission = await FlutterLocation.checkAndRequestPermission();

    if (!mounted) return;

    setState();

    if (permission != LocationPermission.granted) return;

    // _subscription = FlutterLocation.onChanged.listen(_onLocationChange);
    final data = await FlutterLocation.getCurrent();

    _onLocationChange(data);
  }

  void _onLocationChange(LocationData data) {
    _currentLocationData =
        '  Latitude: ${data.latitude}\n  Longitude: ${data.longitude}\n  Accuracy: ${data.accuracy}\n  Timestamp: ${data.timestamps.toIso8601String()}\n  AltitudeEllipsoid: ${data.altitudeEllipsoid}\n  AltitudeMSL: ${data.altitudeMSL}\n  AltitudeAccuracy: ${data.altitudeAccuracy}\n  Heading: ${data.heading}\n  HeadingAccuracy: ${data.headingAccuracy}\n  Speed: ${data.speed}\n  Speed Accuracy: ${data.speedAccuracy}\n  Floor: ${data.floor}';
    setState();
  }

  static void _() {}

  @override
  void setState([VoidCallback fn = _]) {
    super.setState(fn);
  }

  @override
  void reassemble() {
    _subscription?.pause();
    _subscription?.resume();
    super.reassemble();
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin flutter_location_ffi example app'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text("Current Permission: ${FlutterLocation.checkPermission()}"),
            const SizedBox(height: 4),
            if (_currentLocationData.isNotEmpty)
              Text("Current LocationData:\n\n$_currentLocationData"),
            TextButton(onPressed: _onInit, child: const Text('Get current')),
            TextButton(onPressed: _pause, child: const Text("Pause")),
            TextButton(onPressed: _resume, child: const Text("Resume")),
            const TextButton(
              onPressed: FlutterLocation.openAppSettings,
              child: Text('Open application settings'),
            ),
            const TextButton(
              onPressed: FlutterLocation.openLocationSettings,
              child: Text("Open location settings"),
            ),
          ],
        ),
      ),
    );
  }

  void _pause() {
    _subscription?.pause();
  }

  void _resume() {
    _subscription?.resume();
  }
}
