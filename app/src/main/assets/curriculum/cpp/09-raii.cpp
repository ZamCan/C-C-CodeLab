#include <iostream>
#include <memory>
#include <vector>

int main() {
    auto value = std::make_unique<int>(42);
    std::vector<int> scores{1, 2, 3};

    std::cout << *value << '\n';
    std::cout << scores.size() << '\n';
    return 0;
}
/* expect:
42
3
*/
