
declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
b0:
	%v3 = alloca [2 x i32]
	%v1 = alloca [1 x i32]
	%v2 = getelementptr inbounds [1 x i32], [1 x i32]* %v1, i32 0, i32 0
	store i32 2, i32* %v2
	%v4 = getelementptr inbounds [2 x i32], [2 x i32]* %v3, i32 0, i32 0
	store i32 1, i32* %v4
	%v5 = getelementptr inbounds i32, i32* %v4, i32 1
	store i32 2, i32* %v5
	%v6 = getelementptr inbounds [2 x i32], [2 x i32]* %v3, i32 0, i32 0
	%v7 = load i32, i32* %v6
	call void @putint(i32 %v7)
	ret i32 0
}
