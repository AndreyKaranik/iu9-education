import 'dart:io';

const String filePath = 'value.txt';

void main() async {
  final server = await HttpServer.bind(InternetAddress.anyIPv4, 8100);
  print('Server running on http://${server.address.address}:${server.port}');

  await for (HttpRequest request in server) {
    final pathSegments = request.uri.pathSegments;

    if (request.method == 'GET' && pathSegments.length == 0) {
      await handleGetRequest(request);
    } else if (request.method == 'POST' && pathSegments.length == 1) {
      final valueString = pathSegments[0];

      try {
        final value = int.parse(valueString);
        await handlePostRequest(request, value);
      } catch (e) {
        handleInvalidRequest(request, 'Invalid integer value: $valueString');
      }
    } else {
      handleInvalidRequest(request, 'Invalid route or method');
    }
  }
}

Future<void> handleGetRequest(HttpRequest request) async {
  try {
    final file = File(filePath);

    if (await file.exists()) {
      String content = await file.readAsString();
      request.response
        ..statusCode = HttpStatus.ok
        ..write('$content');
    } else {
      request.response
        ..statusCode = HttpStatus.ok
        ..write('No value stored yet.');
    }
  } catch (e) {
    request.response
      ..statusCode = HttpStatus.internalServerError
      ..write('Error reading file');
  } finally {
    await request.response.close();
  }
}

Future<void> handlePostRequest(HttpRequest request, int value) async {
  try {
    final file = File(filePath);
    await file.writeAsString(value.toString());

    request.response
      ..statusCode = HttpStatus.ok
      ..write('Value stored: $value');
  } catch (e) {
    request.response
      ..statusCode = HttpStatus.internalServerError
      ..write('Error writing to file');
  } finally {
    await request.response.close();
  }
}

void handleInvalidRequest(HttpRequest request, String message) {
  request.response
    ..statusCode = HttpStatus.badRequest
    ..write(message);
  request.response.close();
}