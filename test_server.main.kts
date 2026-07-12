import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)
    server.start()
    println("Server started")
    server.stop(0)
}
main()
