#include <iostream>
#include <string>

int main() {
    std::string learner;
    int lessons = 0;

    std::cout << "Name: ";
    if (!(std::cin >> learner)) {
        std::cerr << "No name supplied\n";
        return 1;
    }
    std::cin >> lessons;
    std::cout << learner << " has " << lessons << " lessons\n";
    return 0;
}
/* expect:
Name: Ada has 12 lessons
*/
