cp ../llvm_ir.txt ./target.ll 
clang -c target.ll 
clang -o target target.o ../lib/sylib.o
./target