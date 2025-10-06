final class LocationData {
  final bool isLocationAvailable;
  final double latitude;
  final double longitude;
  final double accuracy;
  final DateTime timestamps;
  final double altitudeEllipsoid;
  final double altitudeMSL;
  final double altitudeAccuracy;
  final double heading;
  final double headingAccuracy;
  final double speed;
  final double speedAccuracy;
  final int? floor;

  LocationData(List<Object?> data)
    : isLocationAvailable = data[0] as bool,
      latitude = data[1] as double,
      longitude = data[2] as double,
      accuracy = data[3] as double,
      timestamps = DateTime.fromMillisecondsSinceEpoch(
        data[4] as int,
        isUtc: true,
      ),
      altitudeEllipsoid = data[5] as double,
      altitudeMSL = data[6] as double,
      altitudeAccuracy = data[7] as double,
      heading = data[8] as double,
      headingAccuracy = data[9] as double,
      speed = data[10] as double,
      speedAccuracy = data[11] as double,
      floor = data[12] as int?;

  factory LocationData.fromResult(Object? data) {
    return LocationData(data as List<Object?>);
  }
}
