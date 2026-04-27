#include <stdlib.h>
#include <stdio.h>
#include <string.h>
int _x1;
int _x2;
int _x3;
int _x4;

int __geaux_globals__() {
return 0;
}

int main() {
_x1 = __geaux_globals__();
_x2 = 5;
_x3 = 10;
_x4 = (_x2 + _x3);
printf("%d\n", _x4);
return 0;
}

