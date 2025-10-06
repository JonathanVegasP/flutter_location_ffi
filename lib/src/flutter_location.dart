import 'dart:async';
import 'dart:ffi';

import 'package:result_channel/result_channel.dart';

import 'location_data.dart';
import 'location_permission.dart';
import 'location_settings.dart';

const _packageName =
    'dev.jonathanvegasp.flutter_location_ffi.FlutterLocationFfiPlugin';

NativeCallable<CallbackNative>? _ptr;

StreamController<Object?>? _updates;

void _onListen(Pointer<ResultNative> pointer) {
  final result = pointer.toResultDart();

  if (result.isOk) {
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
  final ptr = _ptr = NativeCallable<CallbackNative>.listener(_onListen);

  ResultChannel.callStaticVoidAsyncNative(
    _packageName,
    'startUpdates',
    ptr.nativeFunction,
  );
}

void _onPause() {
  ResultChannel.callStaticVoid(_packageName, 'stopUpdates');
}

void _onStop() {
  _onPause();
  _updates!.close();
  _updates = null;
}

abstract final class FlutterLocation {
  static void setSettings(LocationSettings settings) {
    ResultChannel.registerClass(_packageName);
    ResultChannel.callStaticVoidWithArgs(
      _packageName,
      'setSettings',
      ResultDart.ok(settings.encode()),
    );
  }

  static Future<LocationPermission> checkAndRequestPermission() async {
    final result = await ResultChannel.callStaticVoidAsync(
      _packageName,
      'checkAndRequestPermission',
    );

    if (result.isOk) {
      return LocationPermission.values[result.data as int];
    }

    final data = result.data;

    if (data == null) {
      throw const OutOfMemoryError();
    }

    throw StateError('$data');
  }

  static LocationPermission checkPermission() {
    final result = ResultChannel.callStaticReturn(
      _packageName,
      'checkPermission',
    );

    return LocationPermission.values[result.data as int];
  }

  static Future<bool> isServiceEnabled() async {
    final result = await ResultChannel.callStaticVoidAsync(
      _packageName,
      'isServiceEnabled',
    );

    if (result.isOk) {
      return result.data as bool;
    }

    final data = result.data;

    if (data == null) {
      throw OutOfMemoryError();
    }

    throw StateError('$data');
  }

  static Future<LocationData> getCurrent() async {
    final result = await ResultChannel.callStaticVoidAsync(
      _packageName,
      'getCurrent',
    );

    if (result.isOk) {
      return LocationData(result.data as List<Object?>);
    }

    final data = result.data;

    if (data == null) {
      throw const OutOfMemoryError();
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
    ResultChannel.callStaticVoid(_packageName, 'openAppSettings');
  }

  static void openLocationSettings() {
    ResultChannel.callStaticVoid(_packageName, 'openLocationSettings');
  }

  static void openNotificationSettings() {
    ResultChannel.callStaticVoid(_packageName, 'openNotificationSettings');
  }
}
