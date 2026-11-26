#include <iostream>

class Counter {
    int value{0};
public:
    void increment() {
        if (value < 100) {
            ++value;
        }
    }
    int get() const { return value; }
};

int main() {
    Counter counter;
    counter.increment();
    counter.increment();
    std::cout << counter.get() << '\n';
    return 0;
}
/* expect:
2
*/
