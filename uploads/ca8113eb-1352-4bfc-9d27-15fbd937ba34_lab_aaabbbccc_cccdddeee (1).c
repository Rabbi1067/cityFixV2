#include <stdio.h>

int main() {

    for (int i = 0; i <= 42; i++) {
        
        if (i != 0 && i % 9 == 0) {
            printf(" ");
        }printf("%c",'a' + (i % 9) / 3+i/9*2);

    }
}
