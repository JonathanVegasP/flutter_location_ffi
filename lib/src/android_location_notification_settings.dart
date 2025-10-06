import 'package:flutter_location_ffi/src/android_location_notification_priority.dart';

final class AndroidLocationNotificationSettings {
  final AndroidLocationNotificationPriority priority;
  final bool showBadge;
  final bool vibrationEnabled;
  final bool lightsEnabled;
  final bool silent;
  final String title;
  final String message;
  final String? info;

  const AndroidLocationNotificationSettings({
    this.priority = AndroidLocationNotificationPriority.low,
    this.showBadge = false,
    this.vibrationEnabled = false,
    this.lightsEnabled = false,
    this.silent = true,
    this.title = 'Background service is running',
    this.message = 'Background service is running',
    this.info,
  });
}
