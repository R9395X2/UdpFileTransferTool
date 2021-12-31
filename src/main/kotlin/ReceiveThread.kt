import androidx.compose.runtime.MutableState
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket

class ReceiveThread(
    private val bufferSize: Int,
    private val port: Int,
    private val path: String,
    private var progress: MutableState<Float>,
    private var fileSizeUI: MutableState<Long>,
    private val message: MutableState<String>
) : Thread() {
    private val socket = DatagramSocket(port)

    override fun run() {
        val buffer = ByteArray(bufferSize)
        var packet = DatagramPacket(buffer, 0, buffer.size)
        var length: Int
        socket.receive(packet)//收到文件名和大小,以|分割
        val fileMessage=String(packet.data, 0, packet.length).trim().split("|")
        val fileName =fileMessage[0]
        val filepath="$path\\receive_$fileName"
        val fileSize =fileMessage[1].toLong()
        fileSizeUI.value=fileSize
        val times = fileSize / (buffer.size / 1000) //发送次数
        val onceProgress = 1 / times.toFloat() //发送一次进度百分比
        println(filepath)
        println(fileSize)
        val fos = FileOutputStream(filepath)


        while (true) {
            if (progress.value < 1f) {
                progress.value += onceProgress
            }
            socket.receive(packet)
            length = packet.length
            if (length > 0) {
                val data = packet.data
                fos.write(data)
                fos.flush()
                if (String(data, 0, packet.length) == "bye") {
                    break
                }
            }
        }
    }
}

