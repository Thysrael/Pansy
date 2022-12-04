@singleVarDecl = dso_local global i32 -10
@singleVarDecl_0 = dso_local global i32 23
@singleVarDecl_1 = dso_local global i32 10
@singleVarDecl_2 = dso_local global i32 0
@STR0 = dso_local constant [13 x i8] c"print int : \00"
@STR1 = dso_local constant [2 x i8] c"\0a\00"
@STR2 = dso_local constant [10 x i8] c"19373479\0a\00"

declare dso_local void @putstr(i8* %a0)
declare dso_local void @putint(i32 %a0)
declare dso_local i32 @getint()
define dso_local void @funcDef_void() {
b0:
	ret void
}
define dso_local i32 @funcDef_0(i32 %a0) {
b1:
	%v5 = mul i32 %a0, 10
	ret i32 %v5
}
define dso_local i32 @funcDef_1(i32 %a0, i32 %a1) {
b7:
	%v21 = icmp ne i32 %a1, 0
	%v13 = mul i32 %a0, %a1
	br i1 %v21, label %b19, label %b18
b16:
	%v25 = sdiv i32 %a0, %a1
	%v26 = mul i32 %v25, %a1
	%v27 = sub i32 %a0, %v26
	%v28 = add i32 %v13, %v27
	br label %b17
b17:
	%p1 = phi i32 [ %v28, %b16 ],  [ %v34, %b18 ]
	%p0 = phi i32 [ %v25, %b16 ],  [ %v36, %b18 ]
	%v39 = sub i32 %p1, %v13
	%v42 = add i32 %v13, %v39
	%v47 = icmp slt i32 %v13, 0
	br i1 %v47, label %b45, label %b44
b18:
	%v36 = sdiv i32 %a0, 2
	%v34 = add i32 %v13, %a0
	br label %b17
b19:
	br label %b16
b43:
	%v49 = sub i32 0, %v13
	br label %b44
b44:
	%p2 = phi i32 [ %v13, %b17 ],  [ %v49, %b43 ]
	%v54 = add i32 %v39, %p0
	%v51 = add i32 %p2, 1
	%v55 = mul i32 %v51, %v54
	ret i32 %v55
b45:
	br label %b43
}
define dso_local void @printInt(i32 %a0) {
b56:
	%v59 = getelementptr inbounds [13 x i8], [13 x i8]* @STR0, i32 0, i32 0
	call void @putstr(i8* %v59)
	call void @putint(i32 %a0)
	%v60 = getelementptr inbounds [2 x i8], [2 x i8]* @STR1, i32 0, i32 0
	call void @putstr(i8* %v60)
	ret void
}
define dso_local i32 @main() {
b61:
	%v62 = getelementptr inbounds [10 x i8], [10 x i8]* @STR2, i32 0, i32 0
	call void @putstr(i8* %v62)
	%v68 = call i32 @getint()
	%v69 = call i32 @getint()
	%v70 = call i32 @getint()
	%v71 = call i32 @getint()
	%v76 = icmp sgt i32 %v68, 5
	br i1 %v76, label %b74, label %b73
b72:
	br label %b73
b73:
	%p10 = phi i32 [ %v68, %b61 ],  [ 5, %b72 ]
	br label %b77
b74:
	br label %b72
b77:
	%p11 = phi i32 [ 10, %b73 ],  [ %v84, %b115 ]
	%p9 = phi i32 [ %v69, %b73 ],  [ %p8, %b115 ]
	%p7 = phi i32 [ %v70, %b73 ],  [ %p6, %b115 ]
	%p3 = phi i32 [ %v71, %b73 ],  [ %p5, %b115 ]
	%v82 = icmp ne i32 %p11, 0
	br i1 %v82, label %b80, label %b79
b78:
	%v84 = sub i32 %p11, 1
	%v90 = icmp sge i32 %p9, %v84
	br i1 %v90, label %b87, label %b86
b79:
	%v142 = icmp eq i32 %p11, 0
	%v143 = zext i1 %v142 to i32
	%v144 = icmp ne i32 %v143, 0
	br i1 %v144, label %b140, label %b139
b80:
	br label %b78
b85:
	%v93 = add i32 %v84, 1
	%v94 = sdiv i32 %p9, %v93
	%v96 = add i32 %v94, %v84
	br label %b86
b86:
	%p8 = phi i32 [ %p9, %b78 ],  [ %v96, %b85 ]
	%v103 = icmp sle i32 %p7, %v84
	br i1 %v103, label %b100, label %b99
b87:
	br label %b85
b97:
	%v106 = mul i32 %p7, %v84
	br label %b98
b98:
	%p6 = phi i32 [ %v106, %b97 ],  [ %v112, %b99 ]
	br label %b113
b99:
	%v109 = add i32 %v84, 3
	%v110 = sdiv i32 %p7, %v109
	%v111 = mul i32 %v110, %v109
	%v112 = sub i32 %p7, %v111
	br label %b98
b100:
	br label %b97
b113:
	%p4 = phi i32 [ %p3, %b98 ],  [ %v137, %b129 ],  [ %v122, %b130 ]
	%v119 = icmp slt i32 %p4, %p6
	br i1 %v119, label %b116, label %b115
b114:
	%v122 = add i32 %p4, %v84
	%v128 = icmp eq i32 %v122, %p10
	br i1 %v128, label %b125, label %b124
b115:
	%p5 = phi i32 [ %p4, %b113 ],  [ %v122, %b123 ]
	br label %b77
b116:
	br label %b114
b123:
	br label %b115
b124:
	%v134 = icmp ne i32 %v122, %p8
	br i1 %v134, label %b131, label %b130
b125:
	br label %b123
b129:
	%v137 = add i32 %p8, %v122
	br label %b113
b130:
	br label %b113
b131:
	br label %b129
b138:
	call void @printInt(i32 %p11)
	br label %b139
b139:
	call void @printInt(i32 %p10)
	call void @printInt(i32 %p9)
	call void @printInt(i32 %p7)
	call void @printInt(i32 %p3)
	call void @funcDef_void()
	%v153 = call i32 @funcDef_1(i32 %p3, i32 %p7)
	call void @printInt(i32 %v153)
	%v157 = call i32 @funcDef_0(i32 %p9)
	%v158 = call i32 @funcDef_1(i32 %v153, i32 %v157)
	call void @printInt(i32 %v158)
	%v161 = load i32, i32* @singleVarDecl
	%v162 = load i32, i32* @singleVarDecl_2
	%v163 = call i32 @funcDef_1(i32 %v161, i32 %v162)
	%v164 = call i32 @funcDef_1(i32 13, i32 3)
	%v165 = call i32 @funcDef_1(i32 %v163, i32 %v164)
	store i32 %v165, i32* @singleVarDecl_2
	%v166 = load i32, i32* @singleVarDecl_2
	call void @printInt(i32 %v166)
	ret i32 0
b140:
	br label %b138
}
