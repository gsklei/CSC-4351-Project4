#include <stdlib.h>
#include <stdio.h>
#include <string.h>
int _x1;
int _x2;

int __geaux_globals__() {
return 0;
}

int add() {
return 15;
}

int main() {
_x1 = __geaux_globals__();
_x2 = add();
printf("%d\n", _x2);
return 0;
}

