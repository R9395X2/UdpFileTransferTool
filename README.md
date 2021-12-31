# UdpFileTransferTool
A udp file transfer tool using Compose for Desktop

计网小学期的作业，使用Compose for Desktop和kotlin完成

1、先开客户端接收，再开服务端发送

2、发送端口目前没用，是为了提前建立连接准备的。可以在发送文件前先互相发送一段建立连接的消息，再建个列表把每个建立连接的线程放到列表里，这样就能看到每个发送线程的进度
        
