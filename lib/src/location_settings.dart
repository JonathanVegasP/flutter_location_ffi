import 'dart:io';

import 'android_location_settings.dart';
import 'apple_location_settings.dart';
import 'location_priority.dart';

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

        final notification = android.androidLocationNotificationSettings;

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
          android.showLocationServiceDialogWhenRequested,
          notification.priority,
          notification.showBadge,
          notification.vibrationEnabled,
          notification.lightsEnabled,
          notification.silent,
          notification.title,
          notification.message,
          notification.info
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
        throw UnsupportedError('Current platform is not supported');
    }
  }
}
