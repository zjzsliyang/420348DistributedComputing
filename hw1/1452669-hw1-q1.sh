#! /bin/bash
log="./1452669-hw1-q1.log"
exec 2>>$log

for k in $(seq 1 100); do
	uptime >>$log
	sleep 10
done
