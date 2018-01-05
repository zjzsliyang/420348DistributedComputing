import java.io._
import org.apache.commons.lang.StringEscapeUtils

object Cleansing {
  def main(args: Array[String]): Unit = {
    val inputFile = "data/books.xml"
    val outputFile = "data/books_output.xml"

    val fr = new FileReader(inputFile)
    var fw = new FileWriter(outputFile)

    val br = new BufferedReader(fr)
    val bw = new BufferedWriter(fw)
    var line = br.readLine()
    var no = 0
    while (line != null) {
      var newLine = line.replace("inproceedings", "article")
        .replace("<www", "<article")
        .replace("/www", "/article")
      bw.write(StringEscapeUtils.unescapeHtml(newLine) + "\n")
      line = br.readLine()
      println(no)
      no += 1
    }
    bw.flush()
    br.close()
    bw.close()
    fr.close()
    fw.close()
  }
}
