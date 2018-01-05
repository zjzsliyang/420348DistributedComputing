package DBLP

import java.io._

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{ArrayType, StringType, IntegerType, StructType}
import org.apache.spark.sql.functions._

object Author {
  def main(args: Array[String]) {
    val conferences = Array("db/journals/pvldb", "db/conf/sigmod", "db/conf/icde")
    val inputFile = "data/dblp_output.xml"
    val outputFile = "data/hw3-1452669-db-top100authors.txt"
    val master = "local[*]"

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val ss = SparkSession
      .builder()
      .appName("Author")
      .master(master)
      .config("spark.executor.memory", "8g")
      .config("spark.driver.memory", "8g")
      .getOrCreate()

    val schema = (new StructType)
      .add("url", StringType, true)
      .add("author", ArrayType.apply(StringType), true)
      .add("year", IntegerType, true)

    val df =ss.read
      .format("com.databricks.spark.xml")
      .option("rowTag", "article")
      .schema(schema)
      .load(inputFile)

    val result = df.filter(
      df("year") >= 2000 &&
        conferences.map(x => df("url").contains(x)).reduceLeft(_ || _)
    )

    val topAuthor = result.select(explode(result("author")).as("author"))
      .groupBy("author")
      .count()
      .orderBy(desc("count")).cache()
      .take(100)

    val pw = new PrintWriter(new File(outputFile))
    topAuthor.foreach(x => pw.println(x.get(0).toString + ": " + x.get(1).toString))
    pw.close()

  }
}