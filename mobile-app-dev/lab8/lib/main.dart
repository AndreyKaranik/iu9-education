import 'dart:io';
import 'package:flutter/material.dart';
import 'package:mailer/mailer.dart';
import 'package:mailer/smtp_server.dart';
import 'package:file_picker/file_picker.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'lab8',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: EmailForm(),
    );
  }
}


class EmailForm extends StatefulWidget {
  @override
  _EmailFormState createState() => _EmailFormState();
}

class _EmailFormState extends State<EmailForm> {
  final _toController = TextEditingController();
  final _subjectController = TextEditingController();
  final _messageController = TextEditingController();
  String? _attachmentPath;

  Future<void> _pickAttachment() async {
    final result = await FilePicker.platform.pickFiles();
    if (result != null && result.files.isNotEmpty) {
      setState(() {
        _attachmentPath = result.files.single.path;
      });
    }
  }

  Future<void> _sendEmail() async {
    final smtpServer = SmtpServer('smtp.yandex.ru',
        port: 587,
        username: 'AndreyKaranik@yandex.ru',
        password: 'hvwfypvieioiclqb',
        ssl: false);

    final message = Message()
      ..from = Address('AndreyKaranik@yandex.ru', 'Andrey Karanik')
      ..recipients.add(_toController.text)
      ..subject = _subjectController.text
      ..text = _messageController.text;

    if (_attachmentPath != null) {
      message.attachments.add(FileAttachment(File(_attachmentPath!)));
    }

    try {
      final sendReport = await send(message, smtpServer);
      print('Email sent: ' + sendReport.toString());
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Email sent!')));
    } on MailerException catch (e) {
      print('Email not sent: $e');
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to send email.')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('lab8')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _toController,
              decoration: InputDecoration(labelText: 'To'),
            ),
            TextField(
              controller: _subjectController,
              decoration: InputDecoration(labelText: 'Subject'),
            ),
            TextField(
              controller: _messageController,
              decoration: InputDecoration(labelText: 'Message'),
              maxLines: 4,
            ),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: _pickAttachment,
              child: Text('Attach File'),
            ),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: _sendEmail,
              child: Text('Send Email'),
            ),
          ],
        ),
      ),
    );
  }
}