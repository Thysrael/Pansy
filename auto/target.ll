@a = dso_local global [3 x [4 x i32]] [[4 x i32] [i32 0, i32 1, i32 2, i32 3], [4 x i32] [i32 4, i32 5, i32 6, i32 7], [4 x i32] [i32 8, i32 9, i32 10, i32 11]]
@STR0 = dso_local constant [2 x i8] c" \00"
@STR1 = dso_local constant [2 x i8] c"\0a\00"
@STR2 = dso_local constant [2 x i8] c" \00"
@STR3 = dso_local constant [2 x i8] c"\0a\00"

declare dso_local void @putstr(i8* %0)
declare dso_local void @putint(i32 %0)
declare dso_local i32 @getint()
define dso_local void @printMatrix([4 x i32]* %0) {
1:
	%2 = alloca i32
	%3 = alloca i32
	%4 = alloca [4 x i32]*
	store [4 x i32]* %0, [4 x i32]** %4
	store i32 0, i32* %3
	store i32 0, i32* %2
	br label %5
5:
	%6 = load i32, i32* %3
	%7 = icmp slt i32 %6, 3
	br i1 %7, label %10, label %9
8:
	store i32 0, i32* %2
	br label %11
9:
	ret void
10:
	br label %8
11:
	%12 = load i32, i32* %2
	%13 = icmp slt i32 %12, 4
	br i1 %13, label %27, label %23
14:
	%15 = load [4 x i32]*, [4 x i32]** %4
	%16 = load i32, i32* %3
	%17 = load i32, i32* %2
	%18 = getelementptr inbounds [4 x i32], [4 x i32]* %15, i32 %16, i32 %17
	%19 = load i32, i32* %18
	call void @putint(i32 %19)
	%20 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %20)
	%21 = load i32, i32* %2
	%22 = add i32 %21, 1
	store i32 %22, i32* %2
	br label %11
23:
	%24 = getelementptr inbounds [2 x i8], [2 x i8]* @STR1, i32 0, i32 0
	call void @putstr(i8* %24)
	%25 = load i32, i32* %3
	%26 = add i32 %25, 1
	store i32 %26, i32* %3
	br label %5
27:
	br label %14
}
define dso_local void @printVector(i32* %0) {
1:
	%2 = alloca i32
	%3 = alloca i32*
	store i32* %0, i32** %3
	store i32 0, i32* %2
	br label %4
4:
	%5 = load i32, i32* %2
	%6 = icmp slt i32 %5, 4
	br i1 %6, label %17, label %15
7:
	%8 = load i32*, i32** %3
	%9 = load i32, i32* %2
	%10 = getelementptr inbounds i32, i32* %8, i32 %9
	%11 = load i32, i32* %10
	call void @putint(i32 %11)
	%12 = getelementptr inbounds [2 x i8], [2 x i8]* @STR2, i32 0, i32 0
	call void @putstr(i8* %12)
	%13 = load i32, i32* %2
	%14 = add i32 %13, 1
	store i32 %14, i32* %2
	br label %4
15:
	%16 = getelementptr inbounds [2 x i8], [2 x i8]* @STR3, i32 0, i32 0
	call void @putstr(i8* %16)
	ret void
17:
	br label %7
}
define dso_local i32 @main() {
0:
	%1 = getelementptr inbounds [3 x [4 x i32]], [3 x [4 x i32]]* @a, i32 0, i32 1
	%2 = getelementptr inbounds [4 x i32], [4 x i32]* %1, i32 0, i32 0
	call void @printVector(i32* %2)
	ret i32 0
}
