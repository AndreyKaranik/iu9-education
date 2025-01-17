import 'package:flutter/material.dart';
import 'package:flutter_cube/flutter_cube.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      title: 'exam1',
      home: Lab10StatefulWidget(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class Lab10StatefulWidget extends StatefulWidget {
  const Lab10StatefulWidget({super.key});

  @override
  State<Lab10StatefulWidget> createState() => _Lab10StatefulWidgetState();
}

class _Lab10StatefulWidgetState extends State<Lab10StatefulWidget> {
  late Object orc;
  double orcRotation = 0.0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('exam1')),
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: Cube(
                onSceneCreated: (Scene scene) {
                  orc = Object(fileName: 'assets/orc/Orc.obj');
                  scene.world.add(orc);
                },
              ),
            ),
          ),
          Text(
              'Угол поворота орка'
          ),
          Slider(
            value: orcRotation,
            min: 0.0,
            max: 360.0,
            label: orcRotation.toStringAsFixed(2),
            onChanged: (value) {
              setState(() {
                orcRotation = value;
                orc.rotation.setValues(0, orcRotation, 0);
                orc.updateTransform();
              });
            },
          ),
        ],
      ),
    );
  }
}