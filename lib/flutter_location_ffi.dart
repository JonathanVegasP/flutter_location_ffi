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
    .lookup<ResultChannelVoidFunction>('flutter_location_ffi_open_location_settings')
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
