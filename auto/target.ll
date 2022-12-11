@a_to_the_a = dso_local global i32 0
@STR0 = dso_local constant [2 x i8] c"\0a\00"
@STR1 = dso_local constant [10 x i8] c"Exptest: \00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
b11:
	%v27 = sub i32 1, 0
	%v30 = sub i32 2, 100005
	br label %b17
b17:
	store i32 100, i32* @a_to_the_a
	%v18 = load i32, i32* @a_to_the_a
	%v28 = add i32 %v18, 1
	%v21 = mul i32 %v28, 101
	%v22 = sdiv i32 %v21, 3
	%v31 = sub i32 %v22, -100003
	call void @putint(i32 %v31)
	%v25 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v25)
	%v19 = sub i32 0, %v18
	%v26 = add i32 %v18, 1
	%v23 = sub i32 %v22, 2
	%v29 = add i32 %v22, 100005
	br label %b16
b16:
	%v13 = getelementptr inbounds [10 x i8], [10 x i8]* @STR1, i32 0, i32 0
	call void @putstr(i8* %v13)
	call void @putint(i32 0)
	call void @putstr(i8* %v25)
	ret i32 0
}
