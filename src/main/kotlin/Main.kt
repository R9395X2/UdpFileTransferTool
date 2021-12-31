// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JLabel

val gray_color = Color(85, 100, 118)
val lightgray_color = Color(222, 227, 227)
val white_color = Color(236, 240, 241)


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {
    var bufferSize = remember { mutableStateOf(4096) } //缓冲区
    val progress = remember { mutableStateOf(0F) } //进度
    val tags = arrayOf("服务端", "客户端") //两种模式
    val selectedTag = remember { mutableStateOf("未选中") } //被选中的模式
    val hostReceivePort = remember { mutableStateOf("") } //本机接收端口
    val hostSendPort = remember { mutableStateOf("") } //本机发送端口
    val toIp = remember { mutableStateOf("") } //目的IP
    val toPort = remember { mutableStateOf("") } //目的端口
    val path = remember { mutableStateOf("") } //文件路径
    val fileSize = remember { mutableStateOf(0L) } //文件大小
    val message = remember {
        mutableStateOf(
            "\n" +
                    "1、先开客户端接收，再开服务端发送\n2、发送端口目前没用，是为了提前建立连接准备的。可以在发送文件前先互相发送一段建立连接的消息，再建个列表把每个建立连接的线程放到列表里，这样就能看到每个发送线程的进度\n3、目前使用发送端每次延迟1ms的方式来减少丢包，想要更好的效果可以把包加上序号，每次接收端判断接受的数据的长度是否为包头+包长，如果不是就发送消息申请重传"
        )
    }
    var alertdialogEnabled = remember { mutableStateOf(false) } //警告窗口
    val button_Color = ButtonDefaults.buttonColors(gray_color, white_color) //取消下划线名字会冲突
    val outlinedTextField_Color = TextFieldDefaults.outlinedTextFieldColors( //取消下划线名字会冲突
        focusedBorderColor = gray_color, focusedLabelColor = gray_color, cursorColor = gray_color
    )
    val textField_Color = TextFieldDefaults.textFieldColors(
        focusedIndicatorColor = gray_color, focusedLabelColor = gray_color,
        cursorColor = gray_color, backgroundColor = lightgray_color
    )

    DesktopMaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(white_color)) {
            Spacer(modifier = Modifier.height(10.dp))
            //第一行 模式选择和文件位置选择
            Row(modifier = Modifier.fillMaxWidth().weight(1F)) {
                //模式选择
                Row(modifier = Modifier.weight(3F).align(CenterVertically)) {
                    Spacer(modifier = Modifier.width(60.dp))
                    tags.forEach {
                        Row(modifier = Modifier.weight(1F)) {
                            RadioButton(
                                selected = it == selectedTag.value, onClick = { selectedTag.value = it },
                                modifier = Modifier.padding(2.dp), colors = RadioButtonDefaults.colors(gray_color)
                            )
                            Text(it, modifier = Modifier.align(CenterVertically), color = gray_color)
                        }
                    }
                }
                //文件位置选择
                Row(modifier = Modifier.weight(4F).align(CenterVertically)) {
                    TextField(
                        value = path.value, onValueChange = { path.value = it },
                        label = {
                            if (selectedTag.value == "服务端") {
                                Text("发送的文件位置(选择文件)")
                            } else {
                                Text("接收的文件位置(选择文件夹)")
                            }
                        },
                        modifier = Modifier.align(CenterVertically).weight(8f), colors = textField_Color
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            progress.value = 0f; path.value = fileChooser()
                            fileSize.value = File(path.value).length() / 1024
                        },
                        modifier = Modifier.align(CenterVertically).weight(1f), colors = button_Color
                    ) { Text("选择", textAlign = TextAlign.Center) }
                    Spacer(modifier = Modifier.width(30.dp))
                }
            }

            //中间，根据模式显示对应控件
            Row(modifier = Modifier.weight(5F)) {
                //左
                Box(modifier = Modifier.weight(3F).fillMaxHeight()) {
                    Column(modifier = Modifier.align(Center).paddingFromBaseline(0.dp, 40.dp)) {
                        Row(modifier = Modifier.align(CenterHorizontally)) {
                            OutlinedTextField(
                                value = hostSendPort.value, onValueChange = { hostSendPort.value = it },
                                label = { Text(text = "发送端口") }, shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(100.dp).align(CenterVertically), maxLines = 1,
                                colors = outlinedTextField_Color
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = hostReceivePort.value, onValueChange = { hostReceivePort.value = it },
                                label = { Text(text = "接收端口") }, shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(100.dp).align(CenterVertically), maxLines = 1,
                                colors = outlinedTextField_Color
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        if (selectedTag.value == "服务端") {
                            Row {
                                OutlinedTextField(
                                    value = toIp.value, onValueChange = { toIp.value = it },
                                    label = { Text(text = "目的ip") }, shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.width(150.dp), maxLines = 1, colors = outlinedTextField_Color
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = toPort.value, onValueChange = { toPort.value = it },
                                    label = { Text(text = "目的端口") }, shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.width(100.dp), maxLines = 1, colors = outlinedTextField_Color
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        Row(modifier = Modifier.align(CenterHorizontally)) {
                            OutlinedTextField(
                                value = bufferSize.value.toString(), onValueChange = { bufferSize.value = it.toInt() },
                                label = { Text(text = "缓冲大小") }, shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(100.dp), maxLines = 1,
                                colors = outlinedTextField_Color
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    if (selectedTag.value == "服务端") {
                                        if (hostReceivePort.value == "" || toIp.value == "" || toPort.value == "" || path.value == "") {
                                            alertdialogEnabled.value = true
                                        } else {
                                            progress.value = 0f
                                            SendThread(
                                                bufferSize.value, hostReceivePort.value.toInt(),
                                                toIp.value, toPort.value.toInt(),
                                                path.value, progress, fileSize.value, message
                                            ).start()
                                        }
                                    } else {
                                        if (hostReceivePort.value == "" || path.value == "") {
                                            alertdialogEnabled.value = true
                                        } else {
                                            ReceiveThread(
                                                bufferSize.value, hostReceivePort.value.toInt(),
                                                path.value, progress, fileSize, message
                                            ).start()
                                        }
                                    }
                                },
                                modifier = Modifier.size(100.dp, 55.dp).align(CenterVertically)
                                    .padding(0.dp, 7.dp, 0.dp), colors = button_Color
                            ) {
                                if (selectedTag.value == "服务端") {
                                    Text("开始发送")
                                } else {
                                    Text("开始接收")
                                }
                            }
                        }
                    }
                }
                //右
                Column(modifier = Modifier.weight(4F).fillMaxHeight().padding(0.dp, 0.dp, 30.dp, 0.dp)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("文件大小：${fileSize.value.toFloat() / 1024}M", color = gray_color)
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = message.value, onValueChange = {}, label = { Text("一些信息") },
                        modifier = Modifier.fillMaxSize().align(CenterHorizontally), colors = textField_Color
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            //最后一行 进度条
            Box(modifier = Modifier.weight(1f).padding(30.dp, 10.dp, 30.dp).fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = progress.value, backgroundColor = lightgray_color,
                    modifier = Modifier.height(20.dp).align(TopCenter).fillMaxWidth(), color = gray_color
                )
            }
        }
    }
    if (alertdialogEnabled.value) {
        AlertDialog(
            onDismissRequest = {}, title = { Text("注意：") },
            text = { Text("    有字段为空!", fontSize = 23.sp) },
            confirmButton = {
                TextButton(onClick = { alertdialogEnabled.value = false }) {
                    Text("确认", fontSize = 15.sp, color = gray_color)
                }
            },
            dismissButton = {}, modifier = Modifier.size(300.dp, 150.dp)
        )
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(
            WindowPlacement.Floating,
            false,
            WindowPosition.PlatformDefault,
            WindowSize(800.dp, 500.dp)
        ),
        resizable = false,
        title = "计算机网络小学期UDP传文件"
    ) {
        App()
    }
}

//没找到Compose里的文件选择器只能用Swing的了
fun fileChooser(): String {
    val jfc = JFileChooser()
    jfc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
    jfc.showDialog(JLabel(), "选择")
    val file = jfc.selectedFile
    return if (file != null)
        file.absolutePath
    else
        ""
}