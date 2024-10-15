import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'lab3',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'lab3'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  String _serverResponse = '';
  final TextEditingController _textController = TextEditingController();

  // Функция инкремента
  void _incrementCounter() {
    setState(() {
      _counter++;
    });
    _sendCounterToServer();
  }

  // Функция декремента
  void _decrementCounter() {
    setState(() {
      _counter--;
    });
    _sendCounterToServer();
  }

  // Отправка значения на сервер (POST)
  Future<void> _sendCounterToServer() async {
    final url = Uri.parse('http://194.67.88.154:8100/$_counter');
    try {
      final response = await http.post(url);

      if (response.statusCode == 200) {
        setState(() {
          _serverResponse = 'Значение отправлено: $_counter';
        });
      } else {
        setState(() {
          _serverResponse =
              'Не удалось отправить значение. Сервер ответил кодом статуса: ${response.statusCode}';
        });
      }
    } catch (e) {
      setState(() {
        _serverResponse = 'Ошибка отправки значения: $e';
      });
    }
  }

  Future<void> _sendValueToServer(int value) async {
    final url = Uri.parse('http://194.67.88.154:8100/$value');
    try {
      final response = await http.post(url);

      if (response.statusCode == 200) {
        setState(() {
          _serverResponse = 'Значение отправлено: $value';
          _counter = value;

        });
      } else {
        setState(() {
          _serverResponse =
          'Не удалось отправить значение. Сервер ответил кодом статуса: ${response.statusCode}';
        });
      }
    } catch (e) {
      setState(() {
        _serverResponse = 'Ошибка отправки значения: $e';
      });
    }
  }

  // Получение значения с сервера (GET)
  Future<void> _getValueFromServer() async {
    final url = Uri.parse('http://194.67.88.154:8100');
    try {
      final response = await http.get(url);

      if (response.statusCode == 200) {
        setState(() {
          _counter = int.parse(response.body);
          _serverResponse = 'Счетчик обновлен с сервера: $_counter';
        });
      } else {
        setState(() {
          _serverResponse =
              'Не удалось получить значение. Сервер ответил кодом статуса: ${response.statusCode}';
        });
      }
    } catch (e) {
      setState(() {
        _serverResponse = 'Ошибка получения значения: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const SizedBox(height: 20),
            TextField(
              controller: _textController,
              decoration: const InputDecoration(
                labelText: 'Введите init value',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () {
                _sendValueToServer(int.parse(_textController.text));
              },
              child: const Text('POST INIT'),
            ),
            const Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _getValueFromServer,
              child: const Text('GET Counter'),
            ),
            const SizedBox(height: 20),
            Text(
              _serverResponse,
              style: const TextStyle(color: Colors.lightGreen),
            ),
          ],
        ),
      ),
      floatingActionButton: Column(
        mainAxisAlignment: MainAxisAlignment.end,
        children: <Widget>[
          FloatingActionButton(
            onPressed: _incrementCounter,
            tooltip: 'Increment',
            child: const Icon(Icons.add),
          ),
          const SizedBox(height: 10),
          FloatingActionButton(
            onPressed: _decrementCounter,
            tooltip: 'Decrement',
            child: const Icon(Icons.remove),
          ),
        ],
      ),
    );
  }
}
