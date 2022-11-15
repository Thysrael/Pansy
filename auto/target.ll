@STR0 = dso_local constant [2 x i8] c"\0a\00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
b0:
	%v1 = sdiv i32 778, 389
	call void @putint(i32 %v1)
	%v2 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v2)
	ret i32 0
}
