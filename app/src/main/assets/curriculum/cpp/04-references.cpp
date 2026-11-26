#include <iostream>
#include <string>

void increment(int& value) {
    ++value;
}

std::size_t length(const std::string& text) {
    return text.size();
}

int main() {
    int count{2};
    increment(count);
    std::cout << count << '\n';
    std::cout << length("CodeLab") << '\n';
    return 0;
}
/* expect:
3
7
*/
