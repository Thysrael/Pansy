#include <stdio.h>

int getint()
{
    int t;
    scanf("%d", &t);
    return t;
}

int getarray(int a[])
{
    int n;
    scanf("%d", &n);
    int i = 0;
    while (i < n)
    {
        scanf("%d", &a[i]);
        i = i + 1;
    }
    return n;
}

void putint(int a)
{
    printf("%d", a);
}

void putarray(int n, int a[])
{
    printf("%d:", n);
    int i = 0;
    while (i < n)
    {
        printf(" %d", a[i]);
        i = i + 1;
    }
    printf("\n");
}

void putch(int ch)
{
	if (ch == 32)
		printf(" ");
	if (ch == 33)
		printf("!");
	if (ch == 34)
		printf("\"");
	if (ch == 35)
		printf("#");
	if (ch == 36)
		printf("$");
	if (ch == 37)
		printf("%%");
	if (ch == 38)
		printf("&");
	if (ch == 39)
		printf("'");
	if (ch == 40)
		printf("(");
	if (ch == 41)
		printf(")");
	if (ch == 42)
		printf("*");
	if (ch == 43)
		printf("+");
	if (ch == 44)
		printf(",");
	if (ch == 45)
		printf("-");
	if (ch == 46)
		printf(".");
	if (ch == 47)
		printf("/");
	if (ch == 48)
		printf("0");
	if (ch == 49)
		printf("1");
	if (ch == 50)
		printf("2");
	if (ch == 51)
		printf("3");
	if (ch == 52)
		printf("4");
	if (ch == 53)
		printf("5");
	if (ch == 54)
		printf("6");
	if (ch == 55)
		printf("7");
	if (ch == 56)
		printf("8");
	if (ch == 57)
		printf("9");
	if (ch == 58)
		printf(":");
	if (ch == 59)
		printf(";");
	if (ch == 60)
		printf("<");
	if (ch == 61)
		printf("=");
	if (ch == 62)
		printf(">");
	if (ch == 63)
		printf("?");
	if (ch == 64)
		printf("@");
	if (ch == 65)
		printf("A");
	if (ch == 66)
		printf("B");
	if (ch == 67)
		printf("C");
	if (ch == 68)
		printf("D");
	if (ch == 69)
		printf("E");
	if (ch == 70)
		printf("F");
	if (ch == 71)
		printf("G");
	if (ch == 72)
		printf("H");
	if (ch == 73)
		printf("I");
	if (ch == 74)
		printf("J");
	if (ch == 75)
		printf("K");
	if (ch == 76)
		printf("L");
	if (ch == 77)
		printf("M");
	if (ch == 78)
		printf("N");
	if (ch == 79)
		printf("O");
	if (ch == 80)
		printf("P");
	if (ch == 81)
		printf("Q");
	if (ch == 82)
		printf("R");
	if (ch == 83)
		printf("S");
	if (ch == 84)
		printf("T");
	if (ch == 85)
		printf("U");
	if (ch == 86)
		printf("V");
	if (ch == 87)
		printf("W");
	if (ch == 88)
		printf("X");
	if (ch == 89)
		printf("Y");
	if (ch == 90)
		printf("Z");
	if (ch == 91)
		printf("[");
	if (ch == 92)
		printf("\\");
	if (ch == 93)
		printf("]");
	if (ch == 94)
		printf("^");
	if (ch == 95)
		printf("_");
	if (ch == 96)
		printf("`");
	if (ch == 97)
		printf("a");
	if (ch == 98)
		printf("b");
	if (ch == 99)
		printf("c");
	if (ch == 100)
		printf("d");
	if (ch == 101)
		printf("e");
	if (ch == 102)
		printf("f");
	if (ch == 103)
		printf("g");
	if (ch == 104)
		printf("h");
	if (ch == 105)
		printf("i");
	if (ch == 106)
		printf("j");
	if (ch == 107)
		printf("k");
	if (ch == 108)
		printf("l");
	if (ch == 109)
		printf("m");
	if (ch == 110)
		printf("n");
	if (ch == 111)
		printf("o");
	if (ch == 112)
		printf("p");
	if (ch == 113)
		printf("q");
	if (ch == 114)
		printf("r");
	if (ch == 115)
		printf("s");
	if (ch == 116)
		printf("t");
	if (ch == 117)
		printf("u");
	if (ch == 118)
		printf("v");
	if (ch == 119)
		printf("w");
	if (ch == 120)
		printf("x");
	if (ch == 121)
		printf("y");
	if (ch == 122)
		printf("z");
	if (ch == 123)
		printf("{");
	if (ch == 124)
		printf("|");
	if (ch == 125)
		printf("}");
	if (ch == 126)
		printf("~");
}
