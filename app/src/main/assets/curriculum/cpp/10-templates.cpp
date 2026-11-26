#include <iostream>
#include <string>

template <typename T>
T larger(const T& a, const T& b) {
    return a > b ? a : b;
}

int main() {
    std::cout << larger(3, 7) << '\n';
    std::cout << larger<std::string>("Ada", "Bjarne") << '\n';
    return 0;
}
/* expect:
7
Bjarne
*/
