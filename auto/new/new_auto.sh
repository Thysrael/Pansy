#!/bin/bash

echo "start..."

for ((i=1; i<=6; i++))
do
  echo $i
    TEST="testfile$i"

    touch "input$i.txt"
    
    cat ./lib.c > "$TEST.c"
    cat "$TEST.txt" >> "$TEST.c"
    gcc "$TEST.c" -o a.out
    ./a.out <"input$i.txt" >"output$i.txt"
    rm a.out
    rm "$TEST.c"
done

zip test.zip *.txt 
