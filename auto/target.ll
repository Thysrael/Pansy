@STR0 = dso_local constant [2 x i8] c"\0a\00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
b0:
	br label %b3
b3:
	%p1 = phi i32 [ 0, %b0 ],  [ %v18, %b10 ]
	%p0 = phi i32 [ 0, %b0 ],  [ %v16, %b10 ]
	%v8 = icmp slt i32 %p1, 100
	br i1 %v8, label %b6, label %b5
b4:
	%v13 = icmp eq i32 %p1, 50
	br i1 %v13, label %b11, label %b10
b5:
	call void @putint(i32 %p0)
	%v20 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v20)
	ret i32 %p0
b6:
	br label %b4
b9:
	br label %b5
b10:
	%v16 = add i32 %p0, %p1
	%v18 = add i32 %p1, 1
	br label %b3
b11:
	br label %b9
}
