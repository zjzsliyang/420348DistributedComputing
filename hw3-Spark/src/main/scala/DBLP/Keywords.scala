package DBLP

import java.io.{File, PrintWriter}

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

import scala.io.Source

object Keywords {
  def main(args: Array[String]): Unit = {
    val conferences = Array("db/journals/pvldb", "db/conf/sigmod", "db/conf/icde")
    val inputFile = "data/dblp_output.xml"
    val outputFile = "data/hw3-1452669-db-top100keywords.txt"
    val filterFile = "data/stopwords.txt"
    val master = "local[*]"

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val stopwords = Source.fromFile(filterFile).getLines.toArray

    val ss = SparkSession
      .builder()
      .appName("Keywords")
      .master(master)
      .config("spark.executor.memory", "8g")
      .config("spark.driver.memory", "8g")
      .getOrCreate()

    val schema = (new StructType)
      .add("url", StringType, true)
      .add("title", StringType, true)
      .add("year", IntegerType, true)

    val df =ss.read
      .format("com.databricks.spark.xml")
      .option("rowTag", "article")
      .schema(schema)
      .load(inputFile)

    val words = df.filter(
      df("year") >= 2000 &&
        conferences.map(x => df("url").contains(x)).reduceLeft(_ || _)
    ).select("title")
      .collect()
      .map(_.get(0).toString.replaceAll("""[\p{Punct}]""", "").toLowerCase)
      .flatMap(_.split(" "))
      .filter(!stopwords.contains(_))

    val hotwords = words.distinct.map(x => (x, words.count(_ == x))).sortBy(- _._2).take(100)

    val pw = new PrintWriter(new File(outputFile))
    hotwords.foreach(x => pw.println(x._1 + ": " + x._2))
    pw.close()
  }
}
