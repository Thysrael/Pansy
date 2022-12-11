@STR0 = dso_local constant [2 x i8] c"\0a\00"
@STR1 = dso_local constant [3 x i8] c", \00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @fib(i32 %a0) {
b0:
	%v6 = icmp eq i32 %a0, 1
	br i1 %v6, label %b4, label %b3
b2:
	ret i32 1
b3:
	%v11 = icmp eq i32 %a0, 2
	br i1 %v11, label %b9, label %b8
b4:
	br label %b2
b7:
	ret i32 2
b8:
	%v16 = sub i32 %a0, 2
	%v17 = call i32 @fib(i32 %v16)
	%v13 = sub i32 %a0, 1
	%v14 = call i32 @fib(i32 %v13)
	%v18 = add i32 %v14, %v17
	ret i32 %v18
b9:
	br label %b7
}
define dso_local i32 @main() {
b19:
	%v30 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v30)
	call void @putint(i32 2)
	%v31 = getelementptr inbounds [3 x i8], [3 x i8]* @STR1, i32 0, i32 0
	call void @putstr(i8* %v31)
	%v22 = call i32 @fib(i32 10)
	%v23 = sub i32 0, %v22
	%v24 = add i32 %v23, 1
	call void @putint(i32 %v24)
	call void @putstr(i8* %v31)
	call void @putint(i32 -6)
	call void @putstr(i8* %v30)
	ret i32 0
}
