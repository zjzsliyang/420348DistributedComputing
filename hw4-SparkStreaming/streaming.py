from string import punctuation
from operator import add, itemgetter

from pyspark import SparkConf, SparkContext
from pyspark.sql import SQLContext
from pyspark.sql.types import *
from pyspark.streaming import *

conf = (SparkConf()
        .setMaster('local[2]')
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
    elif instruction.lower().startswith('coauthor: '):
        print('case 2')
    elif instruction.lower().startswith('clique: '):
        print('case 3')


def main():
    lines = streamingContext.socketTextStream('localhost', 9999)
    lines = lines.flatMap(lambda x: x.split('\n'))
    lines.foreachRDD(lambda rdd: rdd.foreach(handle))

    article = sqlContext.read.format('com.databricks.spark.xml') \
        .options(rowTag='article', excludeAttribute=True, charset='utf-8') \
        .load('dblp_converted.xml', schema=schema)

    streamingContext.start()
    streamingContext.awaitTermination()

    #
    #
    #
    #
    #
    #
    # article = sqlContext.read.format('com.databricks.spark.xml') \
    #     .options(rowTag='article', excludeAttribute=True, charset='utf-8') \
    #     .load('dblp_converted.xml', schema=schema)
    # article = article.filter(article.year >= 2000)
    #
    # inproceedings = sqlContext.read.format('com.databricks.spark.xml') \
    #     .options(rowTag='inproceedings', excludeAttribute=True, charset='utf-8') \
    #     .load('dblp_converted.xml', schema=schema)
    # inproceedings = inproceedings.filter(inproceedings.year >= 2000)
    #
    # pvldb = article.filter(article.url.contains('journals/pvldb/')).select('title')
    # sigmod = inproceedings.filter(inproceedings.url.contains('conf/sigmod/')).select('title')
    # icde = inproceedings.filter(inproceedings.url.contains('conf/icde/')).select('title')
    #
    # translator = str.maketrans('', '', punctuation)  # strip punctuation marks from a string
    # titles = pvldb.union(sigmod).union(icde)
    # keywords = titles.rdd.flatMap(lambda x: x[0].translate(translator).split()) \
    #     .map(lambda x: x.lower()).filter(lambda x: x not in stopwords) \
    #     .map(lambda x: (x, 1)).reduceByKey(add) \
    #     .sortBy(itemgetter(1), ascending=False).take(100)
    #
    # with open('hw3-1552699-db-top100keywords.txt', 'w') as f:
    #     for keyword in keywords:
    #         f.write('{}: {}\n'.format(keyword[0], keyword[1]))
    #

if __name__ == '__main__':
    main()
