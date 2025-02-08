package com.duuolf.launcher.page

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.duuolf.launcher.data.settingsDataStore
import com.duuolf.launcher.module.AppBar
import com.duuolf.launcher.utils.toMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Locale


@Composable
fun SetScreen(navController: NavController) {
    val context = LocalContext.current  // 获取当前 Context
    val color = if (isSystemInDarkTheme()) Color.White else Color.Black
    var openChangePasswordDialog by remember { mutableStateOf(false) }
    var openChangeBatteryThresholdDialog by remember { mutableStateOf(false) }
    var openChangeDNDTimeDialog by remember { mutableStateOf(false) }
    val batteryThreshold by context.settingsDataStore.data.map { it.batteryThreshold }.collectAsState(initial = 0)
    val isDND by context.settingsDataStore.data.map { it.isDND }.collectAsState(initial = false)
    val homeMode by context.settingsDataStore.data.map { it.isCustomizeHome }.collectAsState(initial = false)
    Scaffold(topBar = {
        AppBar(title = { Text("设置") }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        })
    }) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                Card(
                    onClick = { openSettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row {
                        Text(text = "打开系统设置", modifier = Modifier.padding(10.dp))
                    }
                }
            }
            item {
                Card(
                    onClick = { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row {
                        Text(text = "设置默认桌面", modifier = Modifier.padding(10.dp))
                    }
                }
            }
            item {
                Card(
                    onClick = { openChangePasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row {
                        Text(text = "修改密码", modifier = Modifier.padding(10.dp))
                    }
                }
            }
            item {
                Card(
                    onClick = { openContacts(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row {
                        Text(text = "联系人设置", modifier = Modifier.padding(10.dp))
                    }
                }
            }
            item {
                Card(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            context.settingsDataStore.updateData { settings ->
                                settings.toBuilder().setIsCustomizeHome(!homeMode).build()
                            }
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "首页显示模式", modifier = Modifier.padding(10.dp))
                        Text(
                            text = if (homeMode) "仅显示部分联系人" else "显示所有联系人",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            item {
                if (homeMode)
                    Card(
                        onClick = { navController.navigate("addContact") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 1.dp)
                    ) {
                        Row {
                            Text(text = "选择联系人", modifier = Modifier.padding(10.dp))
                        }
                    }
            }
            item {
                Card(
                    onClick = { openChangeBatteryThresholdDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "低电量提醒", modifier = Modifier.padding(10.dp))
                        Text(
                            text = if (batteryThreshold == 0) "关闭" else "电量低于$batteryThreshold%时提醒",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            item {
                if(batteryThreshold != 0)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "免打扰模式", modifier = Modifier.padding(10.dp))
                        Switch(
                            checked = isDND,
                            modifier = Modifier.padding(10.dp,0.dp),
                            onCheckedChange = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    context.settingsDataStore.updateData { settings ->
                                        settings.toBuilder().setIsDND(!isDND).build()
                                    }
                                }
                            }
                        )
                    }
                }
            }
            item {
                val startTime by context.settingsDataStore.data.map { it.dndStartTime }.collectAsState(initial = 0)
                val endTime by context.settingsDataStore.data.map { it.dndEndTime }.collectAsState(initial = 0)
                if (batteryThreshold!=0&&isDND)
                    Card(
                        onClick = { openChangeDNDTimeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 1.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(text = "设置免打扰时间", modifier = Modifier.padding(10.dp))
                            Text(text = String.format(Locale.getDefault(), "%02d:%02d-%02d:%02d", startTime / 60, startTime % 60, endTime / 60, endTime % 60), modifier = Modifier.padding(10.dp))
                        }
                    }
            }
            item {
                val isVibrate by context.settingsDataStore.data.map { it.isVibrate }.collectAsState(initial = false)
                if(batteryThreshold!=0)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 1.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "震动功能", modifier = Modifier.padding(10.dp))
                        Switch(
                            checked = isVibrate,
                            modifier = Modifier.padding(10.dp,0.dp),
                            onCheckedChange = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    context.settingsDataStore.updateData { settings ->
                                        settings.toBuilder().setIsVibrate(!isVibrate).build()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    ChangePasswordDialog(
        context = context,
        openDialog = openChangePasswordDialog,
        onDismiss = { openChangePasswordDialog = false }  // 点击外部或提交时关闭弹窗
    )
    ChangeBatteryThresholdDialog(
        context = context,
        openDialog = openChangeBatteryThresholdDialog,
        onDismiss = { openChangeBatteryThresholdDialog = false }  // 点击外部或提交时关闭弹窗
    )
    ChangeDNDTimeDialog(
        context = context,
        openDialog = openChangeDNDTimeDialog,
        onDismiss = { openChangeDNDTimeDialog = false }  // 点击外部或提交时关闭弹窗
    )
}

private fun openSettings(context: Context) {
    val intent = Intent(Settings.ACTION_SETTINGS)
    context.startActivity(intent)
}

private fun openContacts(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = ContactsContract.Contacts.CONTENT_URI  // 设置联系人 URI
    context.startActivity(intent)  // 启动联系人应用
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeDNDTimeDialog(
    context: Context,
    openDialog: Boolean,
    onDismiss: () -> Unit
) {
    // 当弹窗打开时显示对话框
    if (openDialog) {
        val currentTime = Calendar.getInstance()
        val timePickerStart = rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = true,
        )
        val timePickerEnd = rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = onDismiss, // 点击外部关闭弹窗
            text = {
                Column {
                    Text("设置免打扰时间")
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "开始时间")
                        TimeInput(state = timePickerStart)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "结束时间")
                        TimeInput(state = timePickerEnd)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val startTime = timePickerStart.hour * 60 + timePickerStart.minute
                    val endTime = timePickerEnd.hour * 60 + timePickerEnd.minute
                    if (startTime > endTime) {
                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            context.settingsDataStore.updateData {
                                it.toBuilder()
                                    .setDndStartTime(startTime)
                                    .setDndEndTime(endTime)
                                    .build()
                            }
                        }
                        onDismiss()
                    } else {
                        Toast.makeText(context, "时间范围不合法", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(
                        "时间",
                        "开始时间: ${startTime}\n结束时间: $endTime"
                    )
                }
                ) {
                    Text("保存", color = MaterialTheme.colorScheme.background)
                }
            }
        )
    }
}

@Composable
private fun ChangeBatteryThresholdDialog(
    context: Context,
    openDialog: Boolean,
    onDismiss: () -> Unit
) {
    // 当弹窗打开时显示对话框
    if (openDialog) {
        var newBatteryThreshold by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDismiss, // 点击外部关闭弹窗
            text = {
                TextField(
                    value = newBatteryThreshold,
                    onValueChange = { input ->
                        val number = input.toIntOrNull()
                        if (number == null || number in 0..100) {
                            newBatteryThreshold = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("请设置电量阈值") },
                    singleLine = true,
                    placeholder = { Text("请输入0-100之间的整数\n置空或0表示关闭该功能") },
                )
            },
            confirmButton = {
                Button(onClick = {
                    (context as? ComponentActivity)?.lifecycleScope?.launch {
                        context.settingsDataStore.updateData {
                            it.toBuilder()
                                .setBatteryThreshold(newBatteryThreshold.toIntOrNull() ?: 0).build()
                        }
                    }
                    onDismiss()
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.background)
                }
            })
    }
}

@Composable
private fun ChangePasswordDialog(
    context: Context,
    openDialog: Boolean,
    onDismiss: () -> Unit
) {
    // 当弹窗打开时显示对话框
    if (openDialog) {
        val password = runBlocking { context.settingsDataStore.data.first().passwordMd5 }
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var renewPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDismiss, // 点击外部关闭弹窗
            text = {
                Column {
                    TextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入旧密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    TextField(
                        value = renewPassword,
                        onValueChange = { renewPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请重复新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oldPassword.toMD5() == password) {
                            if (newPassword == renewPassword) {
                                runBlocking {
                                    context.settingsDataStore.updateData {
                                        it.toBuilder().setPasswordMd5(newPassword.toMD5()).build()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "两次密码不一致", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "旧密码错误", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()          // 关闭弹窗
                    }
                ) {
                    Text("确认", color = MaterialTheme.colorScheme.background)
                }
            }
        )
    }
}