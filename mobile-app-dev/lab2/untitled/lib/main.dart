import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String response1 = "Response from API 1 will appear here";
  String response2 = "Response from API 2 will appear here";

  Future<void> request0() async {
    final response = await http.get(Uri.parse('http://iocontrol.ru/api/sendData/karanik/value/0'));

    if (response.statusCode == 200) {
      setState(() {
        response1 = jsonDecode(response.body).toString();
      });
    } else {
      setState(() {
        response1 = 'Failed';
      });
    }
  }

  Future<void> request1() async {
    final response = await http.get(Uri.parse('http://iocontrol.ru/api/sendData/karanik/value/1'));

    if (response.statusCode == 200) {
      setState(() {
        response2 = jsonDecode(response.body).toString();
      });
    } else {
      setState(() {
        response2 = 'Failed';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("API Request Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              onPressed: request0,
              child: Text("OFF"),
            ),
            SizedBox(height: 16),
            Text(response1),
            SizedBox(height: 32),
            ElevatedButton(
              onPressed: request1,
              child: Text("ON"),
            ),
            SizedBox(height: 16),
            Text(response2),
          ],
        ),
      ),
    );
  }
}