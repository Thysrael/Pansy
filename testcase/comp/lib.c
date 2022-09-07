#include <stdio.h>

int getint()
{
    int t;
    scanf("%d",&t);
    return t;
}

int getarray(int a[])
{
    int n;
    scanf("%d",&n);
    int i = 0;
    while (i < n)
    {
        scanf("%d",&a[i]);
        i = i + 1;
    }
    return n;
}

void putint(int a)
{
    printf("%d",a);
}

void putarray(int n, int a[])
{
    printf("%d:",n);
    int i = 0;
    while (i < n)
    {
        printf(" %d", a[i]);
        i = i + 1;
    }
    printf("\n");
}