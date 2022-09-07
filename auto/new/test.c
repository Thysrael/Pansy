#include <stdio.h>

void f(int a[100][3])
{
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 3; j++)
        {
            printf("%d ", a[i][j]);
        }
    }

    printf("\n");
}

int main()
{
    int a[2][3] = {{1, 2, 3}, {4, 5, 6}};

    f(a);

    void* p1 = a;
    void* p2 = a + 1;
    void *p3 = *(a + 1) + 2;

    printf("%d %d\n", p2 - p1, p3 - p1); 
    return 0;
}