@STR0 = dso_local constant [2 x i8] c"\0a\00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local void @setArray2D([10 x i32]* %a0, i32 %a1) {
b0:
	%v5 = alloca i32
	%v4 = alloca i32
	%v3 = alloca i32
	%v2 = alloca i32
	%v1 = alloca [10 x i32]*
	store [10 x i32]* %a0, [10 x i32]** %v1
	store i32 %a1, i32* %v2
	store i32 0, i32* %v3
	store i32 0, i32* %v4
	%v6 = call i32 @getint()
	store i32 %v6, i32* %v5
	br label %b7
b7:
	%v11 = load i32, i32* %v3
	%v12 = load i32, i32* %v2
	%v13 = icmp slt i32 %v11, %v12
	br i1 %v13, label %b10, label %b9
b8:
	store i32 0, i32* %v4
	br label %b14
b9:
	ret void
b10:
	br label %b8
b14:
	%v18 = load i32, i32* %v4
	%v19 = icmp slt i32 %v18, 10
	br i1 %v19, label %b17, label %b16
b15:
	%v20 = load [10 x i32]*, [10 x i32]** %v1
	%v21 = load i32, i32* %v3
	%v22 = load i32, i32* %v4
	%v23 = getelementptr inbounds [10 x i32], [10 x i32]* %v20, i32 %v21, i32 %v22
	%v24 = load i32, i32* %v3
	%v25 = load i32, i32* %v4
	%v26 = mul i32 %v24, %v25
	%v27 = load i32, i32* %v5
	%v28 = sdiv i32 %v26, %v27
	%v29 = mul i32 %v28, %v27
	%v30 = sub i32 %v26, %v29
	store i32 %v30, i32* %v23
	%v31 = load i32, i32* %v4
	%v32 = add i32 %v31, 1
	store i32 %v32, i32* %v4
	br label %b14
b16:
	%v33 = load i32, i32* %v3
	%v34 = add i32 %v33, 1
	store i32 %v34, i32* %v3
	br label %b7
b17:
	br label %b15
}
define dso_local i32 @getArray1DSum(i32* %a0, i32 %a1) {
b35:
	%v39 = alloca i32
	%v38 = alloca i32
	%v37 = alloca i32
	%v36 = alloca i32*
	store i32* %a0, i32** %v36
	store i32 %a1, i32* %v37
	store i32 0, i32* %v38
	store i32 0, i32* %v39
	br label %b40
b40:
	%v44 = load i32, i32* %v39
	%v45 = load i32, i32* %v37
	%v46 = icmp slt i32 %v44, %v45
	br i1 %v46, label %b43, label %b42
b41:
	%v47 = load i32, i32* %v38
	%v48 = load i32*, i32** %v36
	%v49 = load i32, i32* %v39
	%v50 = getelementptr inbounds i32, i32* %v48, i32 %v49
	%v51 = load i32, i32* %v50
	%v52 = add i32 %v47, %v51
	store i32 %v52, i32* %v38
	%v53 = load i32, i32* %v39
	%v54 = add i32 %v53, 1
	store i32 %v54, i32* %v39
	br label %b40
b42:
	%v55 = load i32, i32* %v38
	ret i32 %v55
b43:
	br label %b41
}
define dso_local i32 @getArray2DSum([10 x i32]* %a0, i32 %a1) {
b56:
	%v60 = alloca i32
	%v59 = alloca i32
	%v58 = alloca i32
	%v57 = alloca [10 x i32]*
	store [10 x i32]* %a0, [10 x i32]** %v57
	store i32 %a1, i32* %v58
	store i32 0, i32* %v59
	store i32 0, i32* %v60
	br label %b61
b61:
	%v65 = load i32, i32* %v60
	%v66 = load i32, i32* %v58
	%v67 = icmp slt i32 %v65, %v66
	br i1 %v67, label %b64, label %b63
b62:
	%v68 = load i32, i32* %v59
	%v69 = load [10 x i32]*, [10 x i32]** %v57
	%v70 = load i32, i32* %v60
	%v71 = getelementptr inbounds [10 x i32], [10 x i32]* %v69, i32 %v70
	%v72 = getelementptr inbounds [10 x i32], [10 x i32]* %v71, i32 0, i32 0
	%v73 = call i32 @getArray1DSum(i32* %v72, i32 10)
	%v74 = add i32 %v68, %v73
	store i32 %v74, i32* %v59
	%v75 = load i32, i32* %v60
	%v76 = add i32 %v75, 1
	store i32 %v76, i32* %v60
	br label %b61
b63:
	%v77 = load i32, i32* %v59
	ret i32 %v77
b64:
	br label %b62
}
define dso_local i32 @main() {
b78:
	%v80 = alloca i32
	%v79 = alloca [10 x [10 x i32]]
	%v81 = getelementptr inbounds [10 x [10 x i32]], [10 x [10 x i32]]* %v79, i32 0, i32 0
	call void @setArray2D([10 x i32]* %v81, i32 10)
	%v82 = getelementptr inbounds [10 x [10 x i32]], [10 x [10 x i32]]* %v79, i32 0, i32 0
	%v83 = call i32 @getArray2DSum([10 x i32]* %v82, i32 10)
	store i32 %v83, i32* %v80
	%v84 = load i32, i32* %v80
	call void @putint(i32 %v84)
	%v85 = getelementptr inbounds [2 x i8], [2 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v85)
	ret i32 0
}
