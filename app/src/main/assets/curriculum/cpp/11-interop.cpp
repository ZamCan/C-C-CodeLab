#include <cstdio>
#include <iostream>

extern "C" int c_add(int a, int b) {
    return a + b;
}

int main() {
    std::cout << c_add(20, 22) << '\n';
    std::printf("%d\n", c_add(1, 2));
    return 0;
}
/* expect:
42
3
*/
