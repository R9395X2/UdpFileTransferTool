import androidx.compose.runtime.MutableState
import java.io.FileInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit



class SendThread(
    private val bufferSize: Int,
    private val fromPort: Int,
    private val toIp: String,
    private val toPort: Int,
    private val path: String,
    private var progress: MutableState<Float>,
    private val fileSize: Long,
    private val message: MutableState<String>
) : Thread() {
    private val socket = DatagramSocket(fromPort)

    override fun run() {
        val fis = FileInputStream(path)
        var buffer = ByteArray(bufferSize) //一次4k
        val times = fileSize / (buffer.size / 1000) //发送次数
        val onceProgress =1/times.toFloat() //发送一次进度百分比
        val fileName=path.split("\\").last()//文件名
        val fileSize=fileSize.toString()//文件大小
        val fileMessageByteArray="$fileName|$fileSize".toByteArray()//用|分割
        socket.send(DatagramPacket(fileMessageByteArray,0, fileMessageByteArray.size, InetSocketAddress(toIp, toPort)))

        while (fis.read(buffer) != -1) {
            if (progress.value < 1f) {
                progress.value += onceProgress
            }

            val packet = DatagramPacket(buffer, 0, buffer.size, InetSocketAddress(toIp, toPort))//接收方
            socket.send(packet)
            TimeUnit.MICROSECONDS.sleep(1) //延迟1ms减少丢包
        }
        //传完结束
        val shutdown = "bye".toByteArray()
        socket.send(DatagramPacket(shutdown, 0, shutdown.size, InetSocketAddress(toIp, toPort)))
        socket.close()
        fis.close()
    }
}