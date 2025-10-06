import 'activity_type.dart';
import 'location_priority.dart';

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
