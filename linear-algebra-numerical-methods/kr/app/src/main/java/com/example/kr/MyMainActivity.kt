//package com.example.kr
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import com.github.tehras.charts.bar.BarChart
//import com.github.tehras.charts.bar.BarChartData
//import com.github.tehras.charts.bar.renderer.bar.SimpleBarDrawer
//import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
//import com.github.tehras.charts.bar.renderer.xaxis.SimpleXAxisDrawer
//import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer
//import kotlin.system.measureNanoTime
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                MultiplicationApp()
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MultiplicationApp() {
//    var uInput by remember { mutableStateOf(TextFieldValue("")) }
//    var vInput by remember { mutableStateOf(TextFieldValue("")) }
//    var binaryU by remember { mutableStateOf("") }
//    var binaryV by remember { mutableStateOf("") }
//    var normalResult by remember { mutableStateOf(0L) }
//    var karatsubaResult by remember { mutableStateOf(0L) }
//    var normalTime by remember { mutableStateOf(0L) }
//    var karatsubaTime by remember { mutableStateOf(0L) }
//    var showChart by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Top
//    ) {
//        TextField(
//            value = uInput,
//            onValueChange = { uInput = it },
//            label = { Text("Введите число U") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        TextField(
//            value = vInput,
//            onValueChange = { vInput = it },
//            label = { Text("Введите число V") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = {
//            val u = uInput.text.toLongOrNull() ?: 0L
//            val v = vInput.text.toLongOrNull() ?: 0L
//
//            binaryU = u.toString(2)
//            binaryV = v.toString(2)
//
//            normalTime = measureNanoTime {
//                normalResult = classicBinaryMultiply(binaryU, binaryV)
//            }
//
//            karatsubaTime = measureNanoTime {
//                karatsubaResult = karatsubaBinaryMultiply(binaryU, binaryV)
//            }
//
//            normalTime /= 1000;
//            karatsubaTime /= 1000;
//
//            showChart = true
//        }) {
//            Text("Вычислить")
//        }
//
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("Двоичное представление U:")
//            Text(binaryU, fontSize = 14.sp)
//            Text("Двоичное представление V:")
//            Text(binaryV, fontSize = 14.sp)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("Результат обычного умножения:")
//            Text("$normalResult", fontSize = 14.sp)
//            Text("Время обычного умножения:")
//            Text("$normalTime мкс", fontSize = 14.sp)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Результат умножения методом Карацубы:")
//            Text("$karatsubaResult", fontSize = 14.sp)
//            Text("Время умножения методом Карацубы:")
//            Text("$karatsubaTime мкс", fontSize = 14.sp)
//            Spacer(modifier = Modifier.height(16.dp))
//            if (showChart) {
//                TimeComparisonChart(normalTime, karatsubaTime)
//            }
//        }
//    }
//}
//
//fun classicBinaryMultiply(binaryU: String, binaryV: String): Long {
//    val u = binaryU.reversed()
//    val v = binaryV.reversed()
//
//    val result = MutableList(u.length + v.length) { 0 }
//
//    for (i in u.indices) {
//        if (u[i] == '1') {
//            for (j in v.indices) {
//                if (v[j] == '1') {
//                    result[i + j] += 1
//                }
//            }
//        }
//    }
//    for (k in result.indices) {
//        if (result[k] >= 2) {
//            val carry = result[k] / 2
//            result[k] %= 2
//            if (k + 1 < result.size) {
//                result[k + 1] += carry
//            }
//        }
//    }
//
//    return result.reversed().joinToString("").toLong(2)
//}
//
//fun karatsubaBinaryMultiply(binaryU: String, binaryV: String): Long {
//    val u = binaryU.toLong(2)
//    val v = binaryV.toLong(2)
//    return karatsubaMultiply(u, v)
//}
//
//fun karatsubaMultiply(x: Long, y: Long): Long {
//    if (x < 10 || y < 10) return x * y
//
//    val n = maxOf(x.toString(2).length, y.toString(2).length)
//    val m = n / 2
//
//    val high1 = x shr m
//    val low1 = x and ((1L shl m) - 1)
//    val high2 = y shr m
//    val low2 = y and ((1L shl m) - 1)
//
//    val z0 = karatsubaMultiply(low1, low2)
//    val z1 = karatsubaMultiply(low1 + high1, low2 + high2)
//    val z2 = karatsubaMultiply(high1, high2)
//
//    return (z2 shl (2 * m)) + ((z1 - z2 - z0) shl m) + z0
//}
//
//@Composable
//fun TimeComparisonChart(normalTime: Long, karatsubaTime: Long) {
//    val barChartData = BarChartData(
//        bars = listOf(
//            BarChartData.Bar(normalTime.toFloat(), Color.Blue, label = "Обычное умножение"),
//            BarChartData.Bar(karatsubaTime.toFloat(), Color.Green, label = "Метод Карацубы")
//        )
//    )
//
//    BarChart(
//        barChartData = barChartData,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(300.dp),
//        barDrawer = SimpleBarDrawer(),
//        xAxisDrawer = SimpleXAxisDrawer(),
//        yAxisDrawer = SimpleYAxisDrawer(),
//        labelDrawer = SimpleValueDrawer(
//            drawLocation = SimpleValueDrawer.DrawLocation.Outside,
//            labelTextColor = Color.Black
//        )
//    )
//}