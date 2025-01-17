import 'package:flutter/material.dart';
import 'package:flutter_cube/flutter_cube.dart';
import 'package:file_picker/file_picker.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '3D Model Viewer',
      home: const ModelLoader(),
    );
  }
}

class ModelLoader extends StatefulWidget {
  const ModelLoader({super.key});

  @override
  State<ModelLoader> createState() => _ModelLoaderState();
}

class _ModelLoaderState extends State<ModelLoader> {
  Object? _object;

  Future<void> _pickFileAndLoadModel() async {
    try {
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.any
      );

      if (result != null) {
        String? filePath = result.files.single.path;

        if (filePath != null) {
          setState(() {
            _object = Object(
              fileName: filePath,
              isAsset: false,
            );
          });
        }
      }
    } catch (e) {
      print("Ошибка при загрузке модели: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('3D Model Viewer')),
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: _object == null
                  ? const Text('Выберите модель для загрузки')
                  : Cube(
                onSceneCreated: (Scene scene) {
                  scene.world.add(_object!);
                },
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: ElevatedButton(
              onPressed: _pickFileAndLoadModel,
              child: const Text('Загрузить модель'),
            ),
          ),
        ],
      ),
    );
  }
}