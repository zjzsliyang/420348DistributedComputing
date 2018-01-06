from string import punctuation
from operator import add, itemgetter

from pyspark import SparkConf, SparkContext
from pyspark.sql import SQLContext, SparkSession
from pyspark.sql.types import *
from pyspark.sql.functions import *
from pyspark.streaming import *
import re

conf = (SparkConf()
    .setMaster('local[*]')
    .setAppName('distcomp-h4')
    .set('spark.jars.packages', 'com.databricks:spark-xml_2.11:0.4.1')
    .set('spark.executor.memory', '4g'))
sc = SparkContext(conf=conf)
sqlContext = SQLContext(sc)
streamingContext = StreamingContext(sc, 5)
schema = StructType([
    StructField('title', StringType(), True),
    StructField('author', ArrayType(StringType()), True),
    StructField('year', IntegerType(), True),
    StructField('url', StringType(), True)])


def handle(instruction):
    if instruction.lower().startswith('author: '):
        print('case 1')
        author = instruction[8:]
        return author
    elif instruction.lower().startswith('coauthor: '):
        print('case 2')
        author = instruction[10:]
        return author
    elif instruction.lower().startswith('clique: '):
        print('case 3')
        author = re.split(',[ ]*', instruction[8:-3])
        return author, instruction[-1]
    return ""


def main():
    # TODO: case insensitive: .lower()
    # TODO: get context instance
    lines = streamingContext.socketTextStream('localhost', 9999)
    lines = lines.flatMap(lambda x: x.split('\n'))

    article = sqlContext.read.format('com.databricks.spark.xml') \
        .options(rowTag='article', excludeAttribute=True, charset='utf-8') \
        .load('data/dblp_converted.xml', schema=schema)

    inproceedings = sqlContext.read.format('com.databricks.spark.xml') \
        .options(rowTag='inproceedings', excludeAttribute=True, charset='utf-8') \
        .load('data/dblp_converted.xml', schema=schema)

    pvldb = article.filter(article.url.contains('journals/acta'))
    sigmod = inproceedings.filter(inproceedings.url.contains('conf/sigmod/'))
    icde = inproceedings.filter(inproceedings.url.contains('conf/icde/'))

    getArticleNo = lambda author: pvldb.select('author').rdd.map(lambda x: x[0]).map(
        lambda x: 1 if author in x else 0).reduce(add)
    getArticleTitle = lambda author: pvldb.filter(array_contains('author', author)).orderBy(pvldb.year.desc()).select(
        'title').rdd.map(lambda x: x[0]).collect()
    print(getArticleNo('Junhu Wang'))
    print(getArticleTitle('Nathan Goodman'))

    getCoNo = lambda author: pvldb.filter(array_contains('author', author)).select('author').rdd.map(
        lambda x: len(x[0]) - 1).collect()
    getCoArticle = lambda author: pvldb.filter(array_contains('author', author)).select('author').rdd.map(
        lambda x: x[0]).flatMap(lambda authors: [(x, 1) for x in authors]).reduceByKey(add).sortBy(lambda x: -x[1]) \
        .collect()
    print(getCoNo('Nathan Goodman'))
    print(getCoArticle('Nathan Goodman'))

    def filters(df, au):
        for item in au:
            df = df.filter(item)
        return df

    getClique = lambda aus, no: 'YES' if filters(pvldb,
                                                 [array_contains('author', x) for x in aus]).count() > no else 'NO'
    getCliqueArticle = lambda aus: filters(pvldb, [array_contains('author', x) for x in aus]).orderBy(
        pvldb.year.desc()).select('title', 'author', 'year').rdd.collect()
    print(getClique(['Nathan Goodman', 'Christian Lengauer'], 5))
    print(getCliqueArticle(['Nathan Goodman', 'Christian Lengauer']))

    articleNo = lines.map(handle)
    articleNo.pprint()

    streamingContext.start()
    streamingContext.awaitTermination()


if __name__ == '__main__':
    main()
