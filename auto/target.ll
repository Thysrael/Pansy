@test = dso_local global i32 0
@STR0 = dso_local constant [13 x i8] c"Hello World\0a\00"

declare dso_local void @putstr(i8* %0)
declare dso_local void @putint(i32 %0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
0:
	%1 = getelementptr inbounds [13 x i8], [13 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %1)
	%2 = call i32 @getint()
	store i32 %2, i32* @test
	%3 = load i32, i32* @test
	call void @putint(i32 %3)
	ret i32 0
}
