package com.duuolf.launcher.page

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.duuolf.launcher.data.getAllContact
import com.duuolf.launcher.data.getSelectedContacts
import com.duuolf.launcher.data.settingsDataStore
import com.duuolf.launcher.module.AppBar
import com.duuolf.launcher.utils.toMD5
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isCustomizeHome by context.settingsDataStore.data.map { it.isCustomizeHome }
        .collectAsState(initial = false) // 设置初始值，防止 UI 崩溃
    var openDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (context.settingsDataStore.data.first().passwordMd5 == "") navController.navigate("init")
        }
    }

    val callList = if (isCustomizeHome) getSelectedContacts(context) else getAllContact()

    Scaffold(
        topBar = {
            AppBar(title = {
                Text(
                    text = "首页",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.pointerInput(Unit){
                        detectTapGestures(onLongPress = {
                            openDialog = true
                        })
                    }
                )
            })
        },
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(paddingValues),
            content = {
                items(callList) { call -> call.ShowContactItem() }
            }
        )
    }
    InputDialog(
        openDialog = openDialog,
        onDismiss = { openDialog = false },
        onSubmit = { input ->
            toSetPage(context, input, navController)
        }
    )
}

@Composable
fun InputDialog(
    openDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    if (openDialog) {
        var inputText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Column {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmit(inputText)
                        onDismiss()
                    }
                ) {
                    Text("确认", color = MaterialTheme.colorScheme.background)
                }
            }
        )
    }
}

private fun toSetPage(context: Context, password: String, navController: NavController) {
    if (password.toMD5() == runBlocking { context.settingsDataStore.data.first().passwordMd5 }) {
        navController.navigate("set")
    } else {
        Toast.makeText(context, "密码错误", LENGTH_SHORT).show()
    }
}