from operator import add

import re
from pyspark import SparkConf, SparkContext, SQLContext
from pyspark.sql.functions import udf
from pyspark.sql.types import StructType, StructField, StringType, ArrayType, IntegerType, BooleanType
from pyspark.streaming import StreamingContext

schema = StructType([
    StructField('title', StringType(), True),
    StructField('author', ArrayType(StringType()), True),
    StructField('year', IntegerType(), True),
    StructField('url', StringType(), True)])


def case_insensitive_array_contains(name, names_arr):
    lower_name = name.lower()
    if names_arr is not None:
        for names_ in names_arr:
            if lower_name in names_.lower():
                return True
    return False


def case_insensitive_arrays_contains(names, names_arr):
    cnt = 0
    flag = False
    for name in names:
        lower_name = name.lower()
        if names_arr is not None:
            for name_ in names_arr:
                if lower_name in name_.lower():
                    flag = True
        if flag is True:
            cnt += 1
            flag = False
    if cnt == len(names):
        return True
    else:
        return False


def main():
    conf = (SparkConf()
        .setMaster('local[*]')
        .setAppName('distcomp-h4')
        .set('spark.jars.packages', 'com.databricks:spark-xml_2.11:0.4.1')
        .set('spark.executor.memory', '4g'))
    sc = SparkContext(conf=conf)
    streaming_context = StreamingContext(sc, 5)
    lines = streaming_context.socketTextStream('localhost', 9999)

    def handle(rdd):
        input = rdd.collect()
        scc = SparkContext.getOrCreate(conf)
        sql = SQLContext(scc)

        udf(case_insensitive_array_contains, BooleanType())
        udf(case_insensitive_arrays_contains, BooleanType())
        # article = sql.read.format('com.databricks.spark.xml') \
        #     .options(rowTag='article', excludeAttribute=True, charset='utf-8') \
        #     .load('data/dblp_output.xml', schema=schema)
        # article.write.parquet('article')
        article = sql.read.parquet('article')

        output = []
        if len(input) > 0:
            if input[0].lower().startswith('author: '):
                # case 1:
                author = input[0][8:]
                print(article.rdd.filter(lambda x: case_insensitive_array_contains(author, x['author'])).filter(
                    lambda x: len(x['author']) > 1).map(
                    lambda x: x['title']).collect().__len__())
                output = article.orderBy(article.year.desc()).rdd.filter(
                    lambda x: case_insensitive_array_contains(author, x['author'])).map(
                    lambda x: x['title']).collect()
                for item in output:
                    print(item)
            elif input[0].lower().startswith('coauthor: '):
                # case 2:
                author = input[0][10:]
                print(
                    article.select('author').rdd.filter(
                        lambda x: case_insensitive_array_contains(author, x['author'])).map(
                        lambda x: x['author']).flatMap(lambda x: x).distinct().count() - 1)
                output = article.select('author').rdd.filter(
                    lambda x: case_insensitive_array_contains(author, x['author'])).map(
                    lambda x: x['author']).flatMap(lambda au: [(x, 1) for x in au]).reduceByKey(add).sortBy(
                    lambda x: -x[1]).collect()
                for item in output:
                    if author.lower() not in item[0].lower():
                        print(item)

            elif input[0].lower().startswith('clique: '):
                # case 3:
                author = re.split(',[ ]*', re.split('[\[\]]', input[0])[0][8:])
                no = int(re.split('[\[\]]', input[0])[-2])
                print('YES' if article.rdd.filter(
                    lambda x: case_insensitive_arrays_contains(author, x['author'])).count() >= no else 'NO')
                output = article.rdd.filter(lambda x: case_insensitive_arrays_contains(author, x['author'])).map(
                    lambda x: [x[i] for i in ['title', 'author', 'year']]).collect()
                for item in output:
                    print(item)
            else:
                print('illegal input pattern.')

    lines.foreachRDD(handle)
    streaming_context.start()
    streaming_context.awaitTermination()


if __name__ == '__main__':
    main()
