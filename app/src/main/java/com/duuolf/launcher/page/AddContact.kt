package com.duuolf.launcher.page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.duuolf.launcher.data.getAllContact
import com.duuolf.launcher.data.getSelectedContacts
import com.duuolf.launcher.data.updateContacts
import com.duuolf.launcher.module.AppBar
import com.duuolf.launcher.module.Contact
import kotlinx.coroutines.launch


@SuppressLint("MutableCollectionMutableState")
@Composable
fun AddContactScreen(navController: NavController) {
    val context = LocalContext.current
    val allContact = getAllContact()
    println("allContact")
    println(allContact)
    val selContact = getSelectedContacts(context)
    println("selContact")
    println(selContact.toList())
    for(contact in allContact){
        if(selContact.find{ it.id == contact.id} != null){contact.isSelected = true}
    }
    println(allContact)
    Scaffold(
        topBar = { AppBar(title = {Text("选择联系人")}, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回"
            )
        }
        },
            actions = {
                IconButton(onClick = {
                    val contactList:MutableList<com.duuolf.launcher.data.Contact> = mutableListOf()
                    // 将选中的联系人转换为保存格式
                    allContact.filter { it.isSelected }.forEach { contact ->
                        val newContact = com.duuolf.launcher.data.Contact.newBuilder()
                            .setId(contact.id)
                            .setName(contact.name)
                            .setPhoneNumber(contact.phoneNumber)
                            .setPhotoURI(if(contact.photoURI==null) "" else contact.photoURI)
                            .build()
                        contactList.add(newContact)
                    }
                    saveData(context,contactList)
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "保存"
                    )
                }}) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(allContact) {
                ContactSelect(it)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactSelect(call: Contact) {
    val context = LocalContext.current
    val isChecked = remember { mutableStateOf(call.isSelected) }

    // 适配当前主题颜色
    val textColor = MaterialTheme.colorScheme.onSurface
    val borderColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)), // 添加边框适配主题
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface) // 适配主题颜色
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        openEditContact(context, call.contactId)
                    }
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (call.photoURI != null) {
                    AsyncImage(
                        model = call.photoURI,
                        contentDescription = call.name.toString()
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 联系人信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.name.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = call.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            // 选择框
            Checkbox(
                checked = isChecked.value,
                onCheckedChange = { isCheckedValue ->
                    isChecked.value = isCheckedValue
                    call.setIsSelected(isCheckedValue)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}


// 启动联系人编辑界面
fun openEditContact(context: Context, contactId: Long) {
    val contactUri: Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId.toString())
    val intent = Intent(Intent.ACTION_EDIT).apply {
        data = contactUri
    }
    context.startActivity(intent)
}
fun saveData(context: Context, contactList: List<com.duuolf.launcher.data.Contact>) {
    (context as? ComponentActivity)?.lifecycleScope?.launch {
        updateContacts(context, contactList)
        Toast.makeText(context, "联系人更新成功", Toast.LENGTH_SHORT).show()
    }
}

