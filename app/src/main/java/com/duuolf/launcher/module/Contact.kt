package com.duuolf.launcher.module

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.duuolf.launcher.utils.makeCall

class Contact {
    var id:Long = 0
    var name: String? = null
    lateinit var phoneNumber: String
    var isSelected: Boolean = false
    var photoURI: String? = null
    var contactId:Long = 0

    fun setIsSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    fun number(): String {
        phoneNumber = phoneNumber.replace(" ", "")
        return phoneNumber
    }

    @Composable
    fun ShowContactItem1() {
        val context = LocalContext.current
        val hasCallPermission = remember { mutableStateOf(false) }
        val color=if(isSystemInDarkTheme())Color.White else Color.Black

        // 监听权限状态，确保 UI 及时更新
        LaunchedEffect(Unit) {
            hasCallPermission.value = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        }

        // 创建拨打电话权限请求
        val requestPermissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    hasCallPermission.value = true  // ✅ 这里更新状态，UI 会自动刷新
                    makeCall(context, phoneNumber)
                } else {
                    Toast.makeText(context, "未授予拨打电话权限", Toast.LENGTH_SHORT).show()
                }
            }

        Button(
            onClick = {
                if (hasCallPermission.value) {
                    makeCall(context, phoneNumber)
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = color,
            ),
            border = BorderStroke(1.dp, color),
            ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                AsyncImage(model = photoURI, contentDescription = name.toString())
                Text(
                    text = name.toString(),
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize * 1.5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE, // 允许文本显示尽可能多的行
                    softWrap = true, // 启用自动换行
                    style = LocalTextStyle.current.merge(
                        TextStyle(
                            lineHeight = 1.2.em
                        )
                    )
                )
            }
        }

    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ShowContactItem() {
        val context = LocalContext.current
        val hasCallPermission = remember { mutableStateOf(false) }

        // 监听权限状态，确保 UI 及时更新
        LaunchedEffect(Unit) {
            hasCallPermission.value = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        }

        // 创建拨打电话权限请求
        val requestPermissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    hasCallPermission.value = true
                    makeCall(context, phoneNumber)
                } else {
                    Toast.makeText(context, "未授予拨打电话权限", Toast.LENGTH_SHORT).show()
                }
            }

        // **获取当前主题颜色**
        val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        val contentColor = MaterialTheme.colorScheme.onSurface
        val borderColor = MaterialTheme.colorScheme.outline

        // **使用 Card 组件**
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .combinedClickable(
                    onClick = {if (hasCallPermission.value) {
                            makeCall(context, phoneNumber)
                        } else {
                            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                        }},
                    onLongClick = {if (hasCallPermission.value) {
                            makeCall(context, phoneNumber)
                        } else {
                            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                        }})
//                .pointerInput(Unit){
//                    detectTapGestures(onTap = {
//                        if (hasCallPermission.value) {
//                            makeCall(context, phoneNumber)
//                        } else {
//                            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
//                        }
//                    },
//                    onLongPress = {
//                        if (hasCallPermission.value) {
//                            makeCall(context, phoneNumber)
//                        } else {
//                            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
//                        }
//                    })
//                }
                ,
//                .clickable {
//                    if (hasCallPermission.value) {
//                        makeCall(context, phoneNumber)
//                    } else {
//                        requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
//                    }
//                },
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = BorderStroke(1.dp, borderColor),
            shape = RoundedCornerShape(12.dp), // 圆角
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 适度阴影
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // **头像**
                if (!photoURI.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoURI,
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape) // 圆形头像
                    )
                }
                // **联系人姓名**
                Text(
                    text = name ?: "未知联系人",
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize * 1.5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor
                )
                // **电话号码**
                Text(
                    text = phoneNumber,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    color = contentColor
                )
            }
        }
    }
}