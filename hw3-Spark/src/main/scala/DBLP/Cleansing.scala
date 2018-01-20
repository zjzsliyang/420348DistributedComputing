import java.io._
import org.apache.commons.lang.StringEscapeUtils

object Cleansing {
  def main(args: Array[String]): Unit = {
    val inputFile = "data/dblp.xml"
    val outputFile = "data/dblp_output.xml"

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
        .replace("proceedings", "article")
        .replace("book", "article")
        .replace("incollection", "article")
        .replace("phdthesis", "article")
        .replace("mastersthesis", "article")
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
