import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.Scanner

fun main() {
    val url = URL("https://pipedapi.kavin.rocks/streams/dQw4w9WgXcQ")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
    println("Piped Response Code: " + conn.responseCode)
    
    val cobaltUrl = URL("https://api.cobalt.tools/api/json")
    val cobaltConn = cobaltUrl.openConnection() as HttpURLConnection
    cobaltConn.requestMethod = "POST"
    cobaltConn.setRequestProperty("Accept", "application/json")
    cobaltConn.setRequestProperty("Content-Type", "application/json")
    cobaltConn.doOutput = true
    val writer = OutputStreamWriter(cobaltConn.outputStream)
    writer.write("{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\",\"downloadMode\":\"audio\",\"audioFormat\":\"best\"}")
    writer.flush()
    writer.close()
    println("Cobalt Response Code: " + cobaltConn.responseCode)
}
main()
