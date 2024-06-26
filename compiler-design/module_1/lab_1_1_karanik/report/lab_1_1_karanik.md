% Лабораторная работа № 1.1. Раскрутка самоприменимого компилятора
% 26 февраля 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является ознакомление с раскруткой самоприменимых компиляторов на примере модельного
компилятора.

# Индивидуальный вариант
Компилятор BeRo. Разрешить использовать знак .. вместо ключевого слова to при записи цикла for. При этом
использование слова to не запрещается.

# Реализация

Различие между файлами `btpc.pas` и `btpc2.pas`:

```diff
***** btpc.pas
      SymPROC=50;

***** BTPC2.PAS
      SymPROC=50;
      SymDD=51;

*****

***** btpc.pas
    r:=-1;
   end else begin
***** BTPC2.PAS
    r:=-1;
   end else if CurrentSymbol=TokColon then begin
    Expect(TokColon);
   end else begin
*****

***** btpc.pas
 StringCopy(Keywords[SymTO],'TO                  ');
 StringCopy(Keywords[SymDOWNTO],'DOWNTO              ');
***** BTPC2.PAS
 StringCopy(Keywords[SymTO],'TO                  ');
 StringCopy(Keywords[SymDD],'..                  ');
 StringCopy(Keywords[SymDOWNTO],'DOWNTO              ');
*****
```

Различие между файлами `btpc2.pas` и `btpc3.pas`:

```diff
Сравнение файлов btpc2.pas и BTPC3.PAS
***** btpc2.pas
begin
 for i:=1 to OutputCodeDataSize do begin
  write(OutputCodeData[i]);
***** BTPC3.PAS
begin
 for i:=1 .. OutputCodeDataSize do begin
  write(OutputCodeData[i]);
*****

***** btpc2.pas
begin
 for i:=1 to 20 do begin
  EmitChar(s[i]);
***** BTPC3.PAS
begin
 for i:=1 .. 20 do begin
  EmitChar(s[i]);
*****

```

# Тестирование

Тестовый пример:

```pascal
program Hello;

var
    i: integer;

begin
  FOR i := 0 .. 2 DO
    begin
    WriteLn('Hello, student!');
    end;
end.
```

Вывод тестового примера на `stdout`

```
Hello, student!
Hello, student!
Hello, student!
```

# Вывод
Лабораторная работа позволила мне познакомиться с процессом раскрутки самоприменимых компиляторов на
примере модельного компилятора. В результате я узнал, как расширять функциональность компилятора и как
это влияет на язык программирования, а также приобрел опыт работы с модельным компилятором.