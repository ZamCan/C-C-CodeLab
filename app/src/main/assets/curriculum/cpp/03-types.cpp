#include <iostream>

int main() {
    const int days{7};
    double hours{1.5};
    auto total = days * hours;

    std::cout << total << '\n';
    return 0;
}
/* expect:
10.5
*/
