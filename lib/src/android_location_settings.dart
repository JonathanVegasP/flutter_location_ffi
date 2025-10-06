import 'package:flutter_location_ffi/src/android_location_notification_settings.dart';

import 'android_granularity.dart';
import 'location_priority.dart';

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

  final bool showLocationServiceDialogWhenRequested;

  final AndroidLocationNotificationSettings androidLocationNotificationSettings;

  const AndroidLocationSettings({
    this.priority,
    this.intervalMs = 10000,
    this.accuracyFilter,
    this.granularity = AndroidGranularity.permissionLevel,
    this.waitForAccurateLocation = true,
    this.durationMs = 0,
    this.minUpdateDistanceMeters,
    this.minUpdateIntervalMs,
    this.maxUpdateDelayMs = 0,
    this.maxUpdateAgeMillis = 0,
    this.maxUpdates = 0,
    this.showLocationServiceDialogWhenRequested = false,
    this.androidLocationNotificationSettings =
        const AndroidLocationNotificationSettings(),
  });
}
