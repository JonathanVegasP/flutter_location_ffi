#ifndef FLUTTER_LOCATION_FFI_H
#define FLUTTER_LOCATION_FFI_H

#include "result_channel.h"

extern "C" {
FFI_PLUGIN_EXPORT void flutter_location_ffi_check_and_request_permission(Callback callback);
FFI_PLUGIN_EXPORT ResultNative *flutter_location_ffi_check_permission();
FFI_PLUGIN_EXPORT void flutter_location_ffi_get_current(Callback callback);
FFI_PLUGIN_EXPORT void flutter_location_ffi_start_updates(Callback callback);
FFI_PLUGIN_EXPORT void flutter_location_ffi_stop_updates();
FFI_PLUGIN_EXPORT void flutter_location_ffi_open_app_settings();
FFI_PLUGIN_EXPORT void flutter_location_ffi_open_location_settings();
}
#endif
