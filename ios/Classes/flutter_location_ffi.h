#ifndef FLUTTER_LOCATION_FFI_H
#define FLUTTER_LOCATION_FFI_H

#import <result_channel/result_channel.h>

FFI_PLUGIN_EXPORT void flutter_location_ffi_check_and_request_permission(Callback callback);

FFI_PLUGIN_EXPORT ResultNative *flutter_location_ffi_check_permission(void);

FFI_PLUGIN_EXPORT void flutter_location_ffi_get_current(Callback callback);

FFI_PLUGIN_EXPORT void flutter_location_ffi_start_updates(Callback callback);

FFI_PLUGIN_EXPORT void flutter_location_ffi_stop_updates(void);

FFI_PLUGIN_EXPORT void flutter_location_ffi_open_app_settings(void);

FFI_PLUGIN_EXPORT void flutter_location_ffi_open_location_settings(void);

#endif
