#! /bin/bash
log="./1452669-hw1-q3.log"
exec 2>>$log

# $1: User Name
# $2: IP Address
# $3: sh & log path
# $4: student ID
ssh $1@$2 "/bin/bash $3/$4-hw1-q1.sh"
scp ./$4-hw1-q1.log $1@$2:$4-hw1-q1.log
b_start_time=$(ls -lrt | grep $4-hw1-q1.log | cut -d ' ' -f 8-10)
a_start_time=$(ls -lrt | grep 1452669-hw1-q1.log | cut -d ' ' -f 8-10)
b_start_time_stamp=$(date +%s -d "$b_start_time")
a_start_time_stamp=$(date +%s -d "$a_start_time")
interval_time=$(($b_start_time_stamp - $a_start_time_stamp))
echo $interval_time >>$log
