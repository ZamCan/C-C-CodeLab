#include <algorithm>
#include <iostream>
#include <vector>

int main() {
    std::vector<int> values{9, 2, 5};
    std::sort(values.begin(), values.end());

    const char *separator = "";
    for (int value : values) {
        std::cout << separator << value;
        separator = " ";
    }
    std::cout << '\n';

    auto found = std::find(values.begin(), values.end(), 5);
    if (found != values.end()) {
        std::cout << "found " << *found << '\n';
    }
    return 0;
}
/* expect:
2 5 9 
found 5
*/
