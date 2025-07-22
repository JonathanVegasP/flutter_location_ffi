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
        intervalMs: 1000,
        priority: LocationPriority.best,
        distanceFilter: 1,
          /// When `minUpdateDistanceMeters`, `priority`, or `intervalMs` are set,
          /// they override the corresponding values in `distanceFilter`, `priority`, and `intervalMs`.
        androidLocationSettings: AndroidLocationSettings(
          priority: LocationPriority.high,
          waitForAccurateLocation: true,
          maxUpdates: 10, /// -1 for unlimited
          durationMs: 60000, /// -1 for unlimited duration
          minUpdateIntervalMs: 1000, /// defaults to [LocationSettings.intervalMs]
          maxUpdateDelayMs: 1000, /// defaults to 0
          intervalMs: 1050, /// if not changed defaults to [LocationSettings.intervalMs]
          minUpdateDistanceMeters: 2, /// if not changed defaults to [LocationSettings.distanceFilter]
          granularity: AndroidGranularity.fine, /// Defaults to [AndroidGranularity.permissionLevel]
          maxUpdateAgeMillis: 1000, /// Defaults to 0 for no caching location
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
