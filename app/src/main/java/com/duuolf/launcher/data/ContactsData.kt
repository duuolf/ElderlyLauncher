package com.duuolf.launcher.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.duuolf.launcher.module.Contact
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val contacts_columns1 = arrayOf(
    ContactsContract.CommonDataKinds.Phone._ID,        // ID
    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,  // 姓名
    ContactsContract.CommonDataKinds.Phone.NUMBER,        // 电话号码
    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,    // 联系人 ID
    ContactsContract.Contacts.PHOTO_URI                   // 头像 URI
)
@SuppressLint("Range")
private fun getAllContacts(context: Context?):MutableList<Contact>{
    val callList: MutableList<Contact> = ArrayList()
    try{
        val cursor = context?.contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contacts_columns1, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))  //姓名
                val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))  //号码
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) // 头像 URI
                val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) // 联系人 ID
                val mCall = Contact()
                mCall.id = id
                mCall.name = name
                mCall.phoneNumber = number.replace(" ", "")
                mCall.photoURI = photoUri
                mCall.contactId = contactId
                callList.add(mCall)
            }
            cursor.close()
        }}catch (e: Exception){
        Toast.makeText(context, "获取联系人失败", Toast.LENGTH_SHORT).show()
    }
    return callList
}
@Composable
fun getAllContact(): MutableList<Contact>{
    val context = LocalContext.current
    var callList by remember { mutableStateOf<List<Contact>>(emptyList()) }
    //var callList = mutableListOf<Contact>()

    // 创建权限请求启动器
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            callList = getAllContacts(context)
        }
    }
    // 只在 Composable 第一次加载时检查权限
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            callList = getAllContacts(context)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }
    return callList.toMutableList()
}
// 获取已选联系人
fun getSelectedContacts(context: Context): List<Contact> {
    var settings: Settings = Settings.getDefaultInstance()
    (context as? ComponentActivity)?.lifecycleScope?.launch {
        settings = context.settingsDataStore.data.first()
    }
    val selectedContacts = mutableListOf<Contact>()
    for (contact in settings.contactsList) {
        val _contact = Contact()
        _contact.id = contact.id
        _contact.name = contact.name
        _contact.phoneNumber = contact.phoneNumber
        _contact.photoURI = contact.photoURI
        selectedContacts.add(_contact)
    }
    return selectedContacts
}
// 添加联系人
suspend fun addContact(context: Context, newContact: com.duuolf.launcher.data.Contact) {
    context.settingsDataStore.updateData { currentSettings   ->
        currentSettings  .toBuilder().addContacts(newContact).build()
    }
}
// 移除联系人
suspend fun removeContact(context: Context,contactId: Long) {
    context.settingsDataStore.updateData { currentSettings ->
        val updatedContacts = currentSettings.contactsList.filterNot { it.id == contactId }
        currentSettings.toBuilder()
            .clearContacts()
            .addAllContacts(updatedContacts)
            .build()
    }
}
// 完全覆盖联系人列表
suspend fun updateContacts(context: Context, newContacts: List<com.duuolf.launcher.data.Contact>) {
    context.settingsDataStore.updateData { currentSettings ->
        currentSettings.toBuilder()
            .clearContacts()
            .addAllContacts(newContacts)
            .build()
    }
}