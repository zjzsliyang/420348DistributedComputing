#! /bin/bash
log="./1452669-hw1-q2.log"
exec 2>>$log

# $1: 1452669-hw1-q1.log

cat $1 | wc -l >>$log

start_time=$(awk '(NR==1) {print $1}' $1)
end_time=$(awk 'END {print $1}' $1)
start_time_stamp=$(date +%s -d $start_time)
end_time_stamp=$(date +%s -d $end_time)
interval_time=$(($end_time_stamp - $start_time_stamp))
echo $interval_time >>$log

load=$(awk -F"[, ]*" '{if($12!=0.00) {count++; print count}}' $1)
echo $load | rev | cut -d ' ' -f 1 | rev >>$log

sed -n '2~2p' $1 | wc -m >>$log
