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
        intervalMs: 10000,
        priority: LocationPriority.medium,
        distanceFilter: 0,
        accuracyFilter: 200.0,
        /// When `minUpdateDistanceMeters`, `priority`, `accuracyFilter`, or `intervalMs` are set,
        /// they override the corresponding values in `distanceFilter`, `priority`, and `intervalMs`.
        androidLocationSettings: AndroidLocationSettings(
          priority: LocationPriority.medium,
          accuracyFilter: 200.0,
          waitForAccurateLocation: true,
          maxUpdates: -1,
          durationMs: -1,
          minUpdateIntervalMs: 10000,
          maxUpdateDelayMs: 0,
          intervalMs: 10000,
          minUpdateDistanceMeters: 0,
          granularity: AndroidGranularity.permissionLevel,
          maxUpdateAgeMillis: 10000,
        ),
          /// When `distanceFilter`, `priority`, `accuracyFilter`, or `intervalMs` are set,
          /// they override the corresponding values in `distanceFilter`, `priority`, and `intervalMs`.
        appleLocationSettings: AppleLocationSettings(
          accuracyFilter: 200.0,
          distanceFilter: -1,
          priority: LocationPriority.medium,
          intervalMs: 10000,
          activityType: ActivityType.other,
          allowsBackgroundLocationUpdates: false,
          headingFilter: -1,
          pausesLocationUpdatesAutomatically: true,
          showBackgroundLocationIndicator: true,
        )
      ),
    );

    _subscription?.cancel();

    final permission = await FlutterLocation.checkAndRequestPermission();

    if (!mounted) return;

    setState();

    if (permission != LocationPermission.granted) return;

    _subscription = FlutterLocation.onChanged.listen(_onLocationChange);
  }

  void _onLocationChange(data) {
    _currentLocationData =
        'Latitude: ${data.latitude}\nLongitude: ${data.longitude}\nAccuracy: ${data.accuracy}';
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
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Text("Current Permission: ${FlutterLocation.checkPermission()}"),
            Text("Current LocationData: $_currentLocationData"),
            TextButton(
              onPressed: _onInit,
              child: const Text('Tente Novamente'),
            ),
            TextButton(onPressed: _pause, child: const Text("Pausar")),
            TextButton(onPressed: _resume, child: const Text("Resumir")),
            const TextButton(
              onPressed: FlutterLocation.openAppSettings,
              child: Text('Abrir configurações'),
            ),
            const TextButton(
              onPressed: FlutterLocation.openLocationSettings,
              child: Text("Abrir configurações de localização"),
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
