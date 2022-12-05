@STR0 = dso_local constant [12 x i8] c"(20373335)\0a\00"
@STR1 = dso_local constant [38 x i8] c"C level,test a code has decl,no func\0a\00"
@STR2 = dso_local constant [9 x i8] c"con1 is \00"
@STR3 = dso_local constant [2 x i8] c"\0a\00"
@STR4 = dso_local constant [9 x i8] c"con2 is \00"
@STR5 = dso_local constant [10 x i8] c",con3 is \00"
@STR6 = dso_local constant [17 x i8] c"the left val is \00"
@STR7 = dso_local constant [21 x i8] c"var1 greater than 1\0a\00"
@STR8 = dso_local constant [20 x i8] c"var5 equal to con1\0a\00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local i32 @main() {
b0:
	%v1 = getelementptr inbounds [12 x i8], [12 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v1)
	%v2 = getelementptr inbounds [38 x i8], [38 x i8]* @STR1, i32 0, i32 0
	call void @putstr(i8* %v2)
	%v3 = getelementptr inbounds [9 x i8], [9 x i8]* @STR2, i32 0, i32 0
	call void @putstr(i8* %v3)
	call void @putint(i32 44)
	%v4 = getelementptr inbounds [2 x i8], [2 x i8]* @STR3, i32 0, i32 0
	call void @putstr(i8* %v4)
	%v5 = getelementptr inbounds [9 x i8], [9 x i8]* @STR4, i32 0, i32 0
	call void @putstr(i8* %v5)
	call void @putint(i32 678)
	%v6 = getelementptr inbounds [10 x i8], [10 x i8]* @STR5, i32 0, i32 0
	call void @putstr(i8* %v6)
	call void @putint(i32 61)
	call void @putstr(i8* %v4)
	%v11 = call i32 @getint()
	%v25 = getelementptr inbounds [17 x i8], [17 x i8]* @STR6, i32 0, i32 0
	call void @putstr(i8* %v25)
	%v18 = mul i32 %v11, 678
	%v20 = sdiv i32 %v18, 105
	%v21 = mul i32 %v20, 105
	%v22 = sub i32 %v18, %v21
	%v15 = mul i32 %v11, 61
	%v16 = sub i32 61, %v15
	%v23 = add i32 %v16, %v22
	call void @putint(i32 %v23)
	call void @putstr(i8* %v4)
	%v35 = sdiv i32 %v18, 105
	%v36 = mul i32 %v35, 105
	%v37 = sub i32 %v18, %v36
	%v38 = add i32 %v16, %v37
	%v47 = icmp sgt i32 %v23, 1
	br i1 %v47, label %b45, label %b44
b43:
	%v48 = getelementptr inbounds [21 x i8], [21 x i8]* @STR7, i32 0, i32 0
	call void @putstr(i8* %v48)
	br label %b44
b44:
	br i1 1, label %b52, label %b51
b45:
	br label %b43
b49:
	%v55 = sdiv i32 %v23, 44
	%v58 = add i32 %v55, 74725
	br label %b50
b50:
	br i1 1, label %b67, label %b66
b51:
	br i1 1, label %b61, label %b60
b52:
	br label %b49
b59:
	%v64 = call i32 @getint()
	br label %b60
b60:
	%p1 = phi i32 [ 61, %b51 ],  [ %v64, %b59 ]
	br label %b50
b61:
	br label %b59
b65:
	br i1 0, label %b72, label %b71
b66:
	br label %b90
b67:
	br label %b65
b70:
	%v75 = getelementptr inbounds [20 x i8], [20 x i8]* @STR8, i32 0, i32 0
	call void @putstr(i8* %v75)
	br label %b71
b71:
	br i1 1, label %b78, label %b77
b72:
	br label %b70
b76:
	br label %b77
b77:
	br label %b66
b78:
	br label %b76
b90:
	br i1 1, label %b93, label %b92
b91:
	br i1 1, label %b100, label %b99
b92:
	ret i32 0
b93:
	br label %b91
b97:
	br label %b92
b99:
	br label %b90
b100:
	br label %b97
}
