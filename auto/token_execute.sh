testfile=$1

if [ ! -d "./out/" ];then
    mkdir out
fi

java -jar pansy.jar 2>jre.txt
cp output.txt ./out/${testfile}.out
