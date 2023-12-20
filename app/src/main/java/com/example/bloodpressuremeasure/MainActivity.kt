/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.bloodpressuremeasure

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.example.bloodpressuremeasure.MainActivity.Companion.BLINK_TIME
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    companion object {
        const val BLINK_TIME = 13 * 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavGraph()
        }
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Greeting") {
        composable("Greeting") {
            Greeting(navController)
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition", "ResourceType")
@Composable
fun Greeting(navController: NavController) {
    var bloodPressureValue by remember { mutableStateOf("") }
    var isBlinking by remember { mutableStateOf(true) }
    var isShowTime by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 设置背景颜色为黑色
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    // 加载GIF动画的ImageRequest
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .data(R.raw.blood_pressure)
                        .decoder(GifDecoder())
                        .build()

                    // 使用rememberImagePainter加载GIF动画并获取Painter对象
                    val painter = rememberImagePainter(imageRequest)
                    Spacer(modifier = Modifier.height(20.dp)) // 添加16dp的垂直间隔
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp), // 指定图片的高度
                        contentDescription = "key_in", // 描述图片内容，用于可访问性
                        painter = painter
                    )

                    Spacer(modifier = Modifier.height(20.dp)) // 添加16dp的垂直间隔
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 34.dp),
                        textAlign = TextAlign.Left,
                        text = stringResource(R.string.blood_pressure),
                        fontSize = 48.sp,
                        color = Color(0xFF6A59B8)
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Spacer(modifier = Modifier.height(70.dp)) // 添加16dp的垂直间隔
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        textAlign = TextAlign.Left,
                        text = bloodPressureValue,
                        fontSize = 65.sp,
                        color = Color.White,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(15.dp)) // 添加16dp的垂直间隔
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = Color(0xffacacac),
                        thickness = 2.dp,

                        )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        textAlign = TextAlign.Left,
                        text = "   " + stringResource(R.string.mmhg),
                        fontSize = 50.sp,
                        color = Color.LightGray,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
            if (isShowTime) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.update_time),
                        fontSize = 40.sp,
                        color = Color(0xffacacac),
                        fontStyle = FontStyle.Italic,
                    )
                    Text(
                        text = CurrentTimeText(),
                        fontSize = 50.sp,
                        color = Color.White,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.measuring),
                    fontSize = 40.sp,
                    color = Color.White,
                )
            }

        }
    }

    // 启动闪烁效果的 LaunchedEffect
    LaunchedEffect(key1 = isBlinking) {
        scope.launch {
            var hasGeneratedBpValue = false
            repeat(100) { // 重复5次
                if (!isBlinking) {
                    // 生成收缩压在115到155之间的随机数
                    val systolicRange = 100..135
                    val systolicValue = Random.nextInt(systolicRange.first, systolicRange.last + 1)
                    // 生成舒张压在65到90之间的随机数
                    val diastolicRange = 65..90
                    val diastolicValue =
                        Random.nextInt(diastolicRange.first, diastolicRange.last + 1)
                    // 组合收缩压和舒张压为字符串格式
                    bloodPressureValue = "$systolicValue/$diastolicValue"
                    scope.cancel()
                    return@launch // 如果 isBlinking 为 false，则立即退出协程，不再执行后续代码
                }

                bloodPressureValue = "    - / -" // 更新为闪烁的值
                delay(600) // 持续闪烁700毫秒
                bloodPressureValue = "       " // 恢复为初始值
                delay(600) // 间隔700毫秒
            }
        }
    }
    // 另一个 LaunchedEffect，用于在10秒后停止闪烁效果
    LaunchedEffect(key1 = Unit) {
        delay(BLINK_TIME)
        isBlinking = false // 设置 isBlinking 为 false，以停止闪烁效果
        isShowTime = true
    }
}


@Composable
fun CurrentTimeText(): String {
    val currentTime = LocalDateTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    return " " + currentTime.format(timeFormatter)
}




