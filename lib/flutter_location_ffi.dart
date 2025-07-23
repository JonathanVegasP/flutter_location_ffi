import 'dart:async';
import 'dart:ffi';
import 'dart:io';

import 'package:result_channel/result_channel.dart';

final _lib = (() {
  const lib = 'flutter_location_ffi';

  return switch (Platform.operatingSystem) {
    'android' => DynamicLibrary.open('lib$lib.so'),
    'ios' => DynamicLibrary.open('$lib.framework/$lib'),
    _ => throw 'Unsupported OS',
  };
})();

enum LocationPermission { denied, neverAskAgain, granted }

enum LocationPriority {
  reduced,
  lowest,
  low,
  medium,
  high,
  best,
  bestForNavigation,
}

enum AndroidGranularity { permissionLevel, coarse, fine }

final class AndroidLocationSettings {
  /// Defaults to [LocationPriority.medium]
  final LocationPriority? priority;

  /// Defaults to 10000 (10 seconds)
  final int intervalMs;

  /// Defaults to 200.0
  final double? accuracyFilter;

  /// Defaults to permissionLevel
  final AndroidGranularity granularity;

  /// Defaults to true
  final bool waitForAccurateLocation;

  /// Defaults to -1 (Long.MaxValue on Kotlin Side)
  final int durationMs;

  /// Defaults to 0.0
  final double? minUpdateDistanceMeters;

  /// Defaults to [intervalMs]
  final int? minUpdateIntervalMs;

  /// Defaults to 0
  final int maxUpdateDelayMs;

  /// Defaults to 0
  final int maxUpdateAgeMillis;

  /// Defaults to -1 (Int.MaxValue on Kotlin Side)
  final int maxUpdates;

  const AndroidLocationSettings({
    this.priority,
    this.intervalMs = 10000,
    this.accuracyFilter,
    this.granularity = AndroidGranularity.permissionLevel,
    this.waitForAccurateLocation = true,
    this.durationMs = -1,
    this.minUpdateDistanceMeters,
    this.minUpdateIntervalMs,
    this.maxUpdateDelayMs = 0,
    this.maxUpdateAgeMillis = 0,
    this.maxUpdates = -1,
  });
}

enum ActivityType {
  other,
  automotiveNavigation,
  fitness,
  otherNavigation,
  airbone,
}

final class AppleLocationSettings {
  final LocationPriority? priority;
  final double? distanceFilter;
  final double? accuracyFilter;
  final ActivityType activityType;
  final bool pausesLocationUpdatesAutomatically;
  final bool allowsBackgroundLocationUpdates;
  final bool showBackgroundLocationIndicator;
  final double headingFilter;

  const AppleLocationSettings({
    this.priority,
    this.distanceFilter,
    this.accuracyFilter,
    this.activityType = ActivityType.other,
    this.pausesLocationUpdatesAutomatically = true,
    this.allowsBackgroundLocationUpdates = false,
    this.showBackgroundLocationIndicator = true,
    this.headingFilter = 0,
  });
}

final class LocationSettings {
  final LocationPriority priority;
  final double distanceFilter;
  final double accuracyFilter;
  final AndroidLocationSettings? androidLocationSettings;
  final AppleLocationSettings? appleLocationSettings;

  const LocationSettings({
    this.priority = LocationPriority.medium,
    this.distanceFilter = 0,
    this.accuracyFilter = 200.0,
    this.androidLocationSettings,
    this.appleLocationSettings,
  });

  List<Object?> encode() {
    switch (Platform.operatingSystem) {
      case 'android':
        final android =
            androidLocationSettings ??
            AndroidLocationSettings(
              priority: priority,
              minUpdateDistanceMeters: distanceFilter,
              accuracyFilter: accuracyFilter,
            );

        return [
          (android.priority ?? priority).index,
          android.intervalMs,
          android.accuracyFilter ?? accuracyFilter,
          android.granularity.index,
          android.waitForAccurateLocation,
          android.durationMs,
          android.minUpdateDistanceMeters ?? distanceFilter,
          android.minUpdateIntervalMs ?? android.intervalMs,
          android.maxUpdateDelayMs,
          android.maxUpdateAgeMillis,
          android.maxUpdates,
        ];
      case 'ios':
        final ios =
            appleLocationSettings ??
            AppleLocationSettings(
              priority: priority,
              distanceFilter: distanceFilter,
              accuracyFilter: accuracyFilter,
            );
        return [
          (ios.priority ?? priority).index,
          ios.distanceFilter ?? distanceFilter,
          ios.accuracyFilter ?? accuracyFilter,
          ios.activityType.index,
          ios.pausesLocationUpdatesAutomatically,
          ios.allowsBackgroundLocationUpdates,
          ios.showBackgroundLocationIndicator,
          ios.headingFilter,
        ];
      default:
        throw 'Unsupported OS';
    }
  }
}

final class LocationData {
  final bool isGpsEnabled;
  final double latitude;
  final double longitude;
  final double accuracy;

  LocationData(List<Object?> data)
    : isGpsEnabled = data[0] as bool,
      latitude = data[1] as double,
      longitude = data[2] as double,
      accuracy = data[3] as double;

  factory LocationData.fromResult(Object? data) {
    return LocationData(data as List<Object?>);
  }
}

final _setSettings = _lib
    .lookup<ResultChannelVoidFunctionWithArgs>(
      'flutter_location_ffi_set_settings',
    )
    .asFunction<ResultChannelVoidFunctionWithArgsDart>();

final _checkAndRequestPermission = _lib
    .lookup<ResultChannelCallbackFunction>(
      'flutter_location_ffi_check_and_request_permission',
    )
    .asFunction<ResultChannelCallbackFunctionDart>();

final _checkPermission = _lib
    .lookup<ResultChannelFunction>('flutter_location_ffi_check_permission')
    .asFunction<ResultChannelFunctionDart>();

final _getCurrent = _lib
    .lookup<ResultChannelCallbackFunction>('flutter_location_ffi_get_current')
    .asFunction<ResultChannelCallbackFunctionDart>();

final _startUpdates = _lib
    .lookup<ResultChannelCallbackFunction>('flutter_location_ffi_start_updates')
    .asFunction<ResultChannelCallbackFunctionDart>();

final _stopUpdates = _lib
    .lookup<ResultChannelVoidFunction>('flutter_location_ffi_stop_updates')
    .asFunction<ResultChannelVoidFunctionDart>();

final _openAppSettings = _lib
    .lookup<ResultChannelVoidFunction>('flutter_location_ffi_open_app_settings')
    .asFunction<ResultChannelVoidFunctionDart>();

final _openLocationSettings = _lib
    .lookup<ResultChannelVoidFunction>(
      'flutter_location_ffi_open_location_settings',
    )
    .asFunction<ResultChannelVoidFunctionDart>();

NativeCallable<ResultChannelCallback>? _ptr;

StreamController<Object?>? _updates;

void _onListen(Pointer<ResultNative> pointer) {
  final result = pointer.toResultDart();

  if (result.status == ResultStatus.ok) {
    _updates!.sink.add(result.data);

    return;
  }

  final data = result.data;

  if (data == null) {
    _ptr!.close();

    _ptr = null;

    return;
  }

  _updates!.sink.addError(StateError('${result.data}'));
}

void _onStart() {
  final ptr = _ptr = NativeCallable<ResultChannelCallback>.listener(_onListen);
  _startUpdates(ptr.nativeFunction);
}

void _onPause() {
  _stopUpdates();
}

void _onStop() {
  _onPause();
  _updates!.close();
  _updates = null;
}

abstract final class FlutterLocation {
  static void setSettings(LocationSettings settings) {
    final result = ResultDart(
      data: settings.encode(),
      status: ResultStatus.ok,
    ).toResultNative();

    try {
      _setSettings(result);
    } finally {
      result.free();
    }
  }

  static Future<LocationPermission> checkAndRequestPermission() async {
    final handler = ResultChannel.createHandler();

    _checkAndRequestPermission(handler.nativeFunction);

    final result = await handler.future;

    if (result.status == ResultStatus.ok) {
      return LocationPermission.values[result.data as int];
    }

    final data = result.data;

    if (data == null) {
      throw ResultChannel.checkApplicationError();
    }

    throw StateError('$data');
  }

  static LocationPermission checkPermission() {
    final ptr = _checkPermission();
    final result = ptr.toResultDart();
    return LocationPermission.values[result.data as int];
  }

  static Future<LocationData> getCurrent() async {
    final handler = ResultChannel.createHandler();

    _getCurrent(handler.nativeFunction);

    final result = await handler.future;

    if (result.status == ResultStatus.ok) {
      return LocationData.fromResult(result.data);
    }

    final data = result.data;

    if (data == null) {
      throw ResultChannel.checkApplicationError();
    }

    throw StateError('$data');
  }

  static Stream<LocationData> get onChanged {
    final stream = _updates ??= StreamController(
      onListen: _onStart,
      onResume: _onStart,
      onCancel: _onStop,
      onPause: _onPause,
    );

    return stream.stream.map(LocationData.fromResult);
  }

  static void openAppSettings() {
    _openAppSettings();
  }

  static void openLocationSettings() {
    _openLocationSettings();
  }
}
