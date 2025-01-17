---
year: 2024
author: "Караник А.А."
group: "ИУ9-72Б"
teacher: "Посевин Д.П."
subject: "Разработка мобильных приложений"
name: "Калькулятор на Kotlin"
worktype: "Контрольная работа К-1"
---

# Цель работы

Реализовать калькулятор разобранный на лекции, но расширив его дополнительным функционалом в зависимости от варианта с использованием Expression Builder Например, расчет тригонометрических функций, логических выражений и т.д.

# Вариант 7

exponentation: 2 ^ 2
log2: logarithm (base 2)
tan: tangent

# Реализация

Исходный код MainActivity.kt:
```kotlin
package com.example.calculator


//import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /*Number Buttons*/
        val tvExpression: TextView = findViewById(R.id.tvExpression)

        val tvOne: TextView = findViewById(R.id.tvOne)
        tvOne.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "1"
        }

        val tvTwo: TextView = findViewById(R.id.tvTwo)
        tvTwo.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "2"
        }

        val tvThree: TextView = findViewById(R.id.tvThree)
        tvThree.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "3"
        }

        val tvFour: TextView = findViewById(R.id.tvFour)
        tvFour.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "4"
        }

        val tvFive: TextView = findViewById(R.id.tvFive)
        tvFive.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "5"
        }

        val tvSix: TextView = findViewById(R.id.tvSix)
        tvSix.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "6"
        }

        val tvSeven: TextView = findViewById(R.id.tvSeven)
        tvSeven.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "7"
        }

        val tvEight: TextView = findViewById(R.id.tvEight)
        tvEight.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "8"
        }

        val tvNine: TextView = findViewById(R.id.tvNine)
        tvNine.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "9"
        }

        val tvZero: TextView = findViewById(R.id.tvZero)
        tvZero.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "0"
        }

        /*Operators*/

        val tvPlus: TextView = findViewById(R.id.tvPlus)
        tvPlus.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "+"
        }

        val tvMinus: TextView = findViewById(R.id.tvMinus)
        tvMinus.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "-"
        }

        val tvMul: TextView = findViewById(R.id.tvMul)
        tvMul.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "*"
        }

        val tvDivide: TextView = findViewById(R.id.tvDivide)
        tvDivide.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "/"
        }

        val tvDot: TextView = findViewById(R.id.tvDot)
        tvDot.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "."
        }

        val op1: TextView = findViewById(R.id.log2)
        op1.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "log2"
        }

        val op2: TextView = findViewById(R.id.tan)
        op2.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "tan"
        }

        val op3: TextView = findViewById(R.id.exp)
        op3.setOnClickListener {
            tvExpression.text = tvExpression.text.toString() + "^"
        }


        val tvClear: TextView = findViewById(R.id.tvClear)

        val tvResult: TextView = findViewById(R.id.tvResult)
        tvClear.setOnClickListener {
            tvExpression.text = ""
            tvResult.text = ""
        }

        val tvEquals: TextView = findViewById(R.id.tvEquals)
        tvEquals.setOnClickListener {
            val text = tvExpression.text.toString()
            val expression = ExpressionBuilder(text).build()

            val result = expression.evaluate()
            val longResult = result.toLong()
            if (result == longResult.toDouble()) {
                tvResult.text = longResult.toString()
            } else {
                tvResult.text = result.toString()
            }
        }
        val tvBack: TextView = findViewById(R.id.tvBack)
        tvBack.setOnClickListener {
            val text = tvExpression.text.toString()
            if(text.isNotEmpty()) {
                tvExpression.text = text.drop(1)
            }

            tvResult.text = ""
        }

    }
}

```

Исходный код activity_main.xml:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:background="@android:color/black"
    android:orientation="vertical">

    <TextView
        android:id="@+id/tvExpression"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:textColor="@color/actionButton"
        android:layout_gravity="end"
        android:ellipsize="start"
        android:singleLine="true"
        android:textSize="40sp" />


    <TextView
        android:id="@+id/tvResult"
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:textColor="@color/white"
        android:layout_gravity="end"
        android:ellipsize="end"
        android:singleLine="true"
        android:textSize="30sp"/>


    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/tvClear"
                style="@style/ActionButtonStyle"
                android:text="CLEAR"/>

            <TextView
                android:id="@+id/tvDivide"
                style="@style/ActionButtonStyle"
                android:text="/"/>

            <TextView
                android:id="@+id/exp"
                style="@style/ActionButtonStyle"
                android:text="^"/>

            <TextView
                android:id="@+id/log2"
                style="@style/ActionButtonStyle"
                android:text="log2"/>

            <TextView
                android:id="@+id/tan"
                style="@style/ActionButtonStyle"
                android:text="tan"/>


        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/tvSeven"
                style="@style/NumberButtonStyle"
                android:text="7"/>

            <TextView
                android:id="@+id/tvEight"
                style="@style/NumberButtonStyle"
                android:text="8"/>

            <TextView
                android:id="@+id/tvNine"
                style="@style/NumberButtonStyle"
                android:text="9"/>

            <TextView
                android:id="@+id/tvMul"
                style="@style/NumberActionButton2"
                android:text="*"/>

        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/tvFour"
                style="@style/NumberButtonStyle"
                android:text="4"/>

            <TextView
                android:id="@+id/tvFive"
                style="@style/NumberButtonStyle"
                android:text="5"/>

            <TextView
                android:id="@+id/tvSix"
                style="@style/NumberButtonStyle"
                android:text="6"/>

            <TextView
                android:id="@+id/tvMinus"
                style="@style/NumberActionButton2"
                android:text="-"/>


        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/tvOne"
                style="@style/NumberButtonStyle"
                android:text="1"/>

            <TextView
                android:id="@+id/tvTwo"
                style="@style/NumberButtonStyle"
                android:text="2"/>

            <TextView
                android:id="@+id/tvThree"
                style="@style/NumberButtonStyle"
                android:text="3"/>

            <TextView
                android:id="@+id/tvPlus"
                style="@style/NumberActionButton2"
                android:text="+"/>


        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/tvDot"
                style="@style/NumberButtonStyle"
                android:text="."/>

            <TextView
                android:id="@+id/tvZero"
                style="@style/NumberButtonStyle"
                android:text="0"/>

            <TextView
                android:id="@+id/tvBack"
                style="@style/NumberButtonStyle"
                android:text="DEL"/>

            <TextView
                android:id="@+id/tvEquals"
                style="@style/EqualButtonStyle"
                android:text="="/>


        </LinearLayout>

    </LinearLayout>


</LinearLayout>

```

# Результаты

![результаты](1.jpg){width=5cm}

# Вывод

В ходе выполнения лабораторной работы был успешно реализован калькулятор с расширенными возможностями и функционалом.