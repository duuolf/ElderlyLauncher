package com.duuolf.launcher.page

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.duuolf.launcher.data.settingsDataStore
import com.duuolf.launcher.utils.toMD5
import kotlinx.coroutines.launch

@Composable
fun InitScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("初次见面，设置一下密码吧。")
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请输入密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            TextField(
                value = rePassword,
                onValueChange = { rePassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请重复密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (password == "") {
                        Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show()
                    } else if (password == rePassword) {
                        coroutineScope.launch {
                            context.settingsDataStore.updateData {
                                it.toBuilder().setPasswordMd5(password.toMD5()).build()
                            }
                            navController.navigateUp()
                        }
                    } else {
                        Toast.makeText(context, "两次密码不一致", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("确认", color = MaterialTheme.colorScheme.background)
            }
        }
    }
}