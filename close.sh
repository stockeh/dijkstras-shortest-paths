#Use with caution!
killall -u $USER java

for i in `cat machine_list`
do
	echo 'logging into '$i
	ssh $i "killall -u $USER java"
done

