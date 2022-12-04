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
	%v300 = getelementptr inbounds [2 x i8], [2 x i8]* @STR1, i32 0, i32 0
	%v299 = getelementptr inbounds [13 x i8], [13 x i8]* @STR0, i32 0, i32 0
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
	br label %b298
b298:
	call void @putstr(i8* %v299)
	call void @putint(i32 %p11)
	call void @putstr(i8* %v300)
	br label %b297
b297:
	br label %b139
b139:
	br label %b302
b302:
	call void @putstr(i8* %v299)
	call void @putint(i32 %p10)
	call void @putstr(i8* %v300)
	br label %b301
b301:
	br label %b306
b306:
	call void @putstr(i8* %v299)
	call void @putint(i32 %p9)
	call void @putstr(i8* %v300)
	br label %b305
b305:
	br label %b310
b310:
	call void @putstr(i8* %v299)
	call void @putint(i32 %p7)
	call void @putstr(i8* %v300)
	br label %b309
b309:
	br label %b314
b314:
	call void @putstr(i8* %v299)
	call void @putint(i32 %p3)
	call void @putstr(i8* %v300)
	br label %b313
b313:
	br label %b173
b173:
	%v182 = icmp ne i32 %p7, 0
	%v181 = mul i32 %p3, %p7
	br i1 %v182, label %b177, label %b176
b174:
	%v192 = sdiv i32 %p3, %p7
	%v193 = mul i32 %v192, %p7
	%v194 = sub i32 %p3, %v193
	%v195 = add i32 %v181, %v194
	br label %b175
b175:
	%p14 = phi i32 [ %v192, %b174 ],  [ %v184, %b176 ]
	%p13 = phi i32 [ %v195, %b174 ],  [ %v183, %b176 ]
	%v185 = sub i32 %p13, %v181
	%v186 = add i32 %v181, %v185
	%v187 = icmp slt i32 %v181, 0
	br i1 %v187, label %b180, label %b179
b176:
	%v184 = sdiv i32 %p3, 2
	%v183 = add i32 %v181, %p3
	br label %b175
b177:
	br label %b174
b178:
	%v191 = sub i32 0, %v181
	br label %b179
b179:
	%p15 = phi i32 [ %v181, %b175 ],  [ %v191, %b178 ]
	%v189 = add i32 %v185, %p14
	%v188 = add i32 %p15, 1
	%v190 = mul i32 %v188, %v189
	br label %b172
b180:
	br label %b178
b172:
	br label %b170
b170:
	%v171 = mul i32 %p9, 10
	br label %b169
b169:
	br label %b198
b198:
	%v207 = icmp ne i32 %v171, 0
	%v206 = mul i32 %v190, %v171
	br i1 %v207, label %b202, label %b201
b199:
	%v217 = sdiv i32 %v190, %v171
	%v218 = mul i32 %v217, %v171
	%v219 = sub i32 %v190, %v218
	%v220 = add i32 %v206, %v219
	br label %b200
b200:
	%p18 = phi i32 [ %v217, %b199 ],  [ %v209, %b201 ]
	%p17 = phi i32 [ %v220, %b199 ],  [ %v208, %b201 ]
	%v210 = sub i32 %p17, %v206
	%v211 = add i32 %v206, %v210
	%v212 = icmp slt i32 %v206, 0
	br i1 %v212, label %b205, label %b204
b201:
	%v209 = sdiv i32 %v190, 2
	%v208 = add i32 %v206, %v190
	br label %b200
b202:
	br label %b199
b203:
	%v216 = sub i32 0, %v206
	br label %b204
b204:
	%p19 = phi i32 [ %v206, %b200 ],  [ %v216, %b203 ]
	%v214 = add i32 %v210, %p18
	%v213 = add i32 %p19, 1
	%v215 = mul i32 %v213, %v214
	br label %b197
b205:
	br label %b203
b197:
	br label %b168
b168:
	br label %b167
b167:
	br label %b318
b318:
	call void @putstr(i8* %v299)
	call void @putint(i32 %v190)
	call void @putstr(i8* %v300)
	br label %b317
b317:
	br label %b322
b322:
	call void @putstr(i8* %v299)
	call void @putint(i32 %v215)
	call void @putstr(i8* %v300)
	br label %b321
b321:
	%v161 = load i32, i32* @singleVarDecl
	%v162 = load i32, i32* @singleVarDecl_2
	br label %b223
b223:
	%v232 = icmp ne i32 %v162, 0
	%v231 = mul i32 %v161, %v162
	br i1 %v232, label %b227, label %b226
b224:
	%v242 = sdiv i32 %v161, %v162
	%v243 = mul i32 %v242, %v162
	%v244 = sub i32 %v161, %v243
	%v245 = add i32 %v231, %v244
	br label %b225
b225:
	%p22 = phi i32 [ %v242, %b224 ],  [ %v234, %b226 ]
	%p21 = phi i32 [ %v245, %b224 ],  [ %v233, %b226 ]
	%v235 = sub i32 %p21, %v231
	%v236 = add i32 %v231, %v235
	%v237 = icmp slt i32 %v231, 0
	br i1 %v237, label %b230, label %b229
b226:
	%v234 = sdiv i32 %v161, 2
	%v233 = add i32 %v231, %v161
	br label %b225
b227:
	br label %b224
b228:
	%v241 = sub i32 0, %v231
	br label %b229
b229:
	%p23 = phi i32 [ %v231, %b225 ],  [ %v241, %b228 ]
	%v239 = add i32 %v235, %p22
	%v238 = add i32 %p23, 1
	%v240 = mul i32 %v238, %v239
	br label %b222
b230:
	br label %b228
b222:
	br label %b248
b248:
	br i1 1, label %b252, label %b251
b249:
	br label %b250
b250:
	%p26 = phi i32 [ 4, %b249 ],  [ 6, %b251 ]
	%p25 = phi i32 [ 40, %b249 ],  [ 52, %b251 ]
	%v260 = sub i32 %p25, 39
	%v261 = add i32 39, %v260
	br i1 0, label %b255, label %b254
b251:
	br label %b250
b252:
	br label %b249
b253:
	br label %b254
b254:
	%p27 = phi i32 [ 39, %b250 ],  [ -39, %b253 ]
	%v264 = add i32 %v260, %p26
	%v263 = add i32 %p27, 1
	%v265 = mul i32 %v263, %v264
	br label %b247
b255:
	br label %b253
b247:
	br label %b273
b273:
	%v282 = icmp ne i32 %v265, 0
	%v281 = mul i32 %v240, %v265
	br i1 %v282, label %b277, label %b276
b274:
	%v292 = sdiv i32 %v240, %v265
	%v293 = mul i32 %v292, %v265
	%v294 = sub i32 %v240, %v293
	%v295 = add i32 %v281, %v294
	br label %b275
b275:
	%p30 = phi i32 [ %v292, %b274 ],  [ %v284, %b276 ]
	%p29 = phi i32 [ %v295, %b274 ],  [ %v283, %b276 ]
	%v285 = sub i32 %p29, %v281
	%v286 = add i32 %v281, %v285
	%v287 = icmp slt i32 %v281, 0
	br i1 %v287, label %b280, label %b279
b276:
	%v284 = sdiv i32 %v240, 2
	%v283 = add i32 %v281, %v240
	br label %b275
b277:
	br label %b274
b278:
	%v291 = sub i32 0, %v281
	br label %b279
b279:
	%p31 = phi i32 [ %v281, %b275 ],  [ %v291, %b278 ]
	%v289 = add i32 %v285, %p30
	%v288 = add i32 %p31, 1
	%v290 = mul i32 %v288, %v289
	br label %b272
b280:
	br label %b278
b272:
	store i32 %v290, i32* @singleVarDecl_2
	%v166 = load i32, i32* @singleVarDecl_2
	br label %b326
b326:
	call void @putstr(i8* %v299)
	call void @putint(i32 %v166)
	call void @putstr(i8* %v300)
	br label %b325
b325:
	ret i32 0
b140:
	br label %b138
}
