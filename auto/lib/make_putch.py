f = open("./chlib.c", 'w')

f.write("void putch(int ch)\n")
f.write("{\n")

for i in range(32, 127):
    if i == 34:
        f.write('\tif (ch == 34) printf("\\\"");\n')
    elif i == 37:
        f.write('\tif (ch == 37) printf("%%");\n')
    elif i == 92:
        f.write('\tif (ch == 92) printf("\\\\");\n')
    else: 
        f.write("\tif (ch == %d) printf(\"%c\");\n" % (i, i))

f.write("}\n")