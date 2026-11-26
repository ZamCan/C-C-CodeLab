#include <cstdio>
#include <iostream>

int main() {
    int lessons = 0;

    std::cout << "Lessons: ";
    if (!(std::cin >> lessons)) {
        std::fprintf(stderr, "No number supplied\n");
        return 1;
    }
    std::cout << "You have " << lessons << " lessons\n";
    return 0;
}
/* expect:
Lessons: You have 12 lessons
*/
