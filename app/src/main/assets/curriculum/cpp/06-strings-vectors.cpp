#include <iostream>
#include <string>
#include <vector>

int main() {
    std::vector<int> scores{70, 80};
    scores.push_back(90);
    std::string learner = "Ada";

    int total = 0;
    for (int score : scores) {
        total += score;
    }
    std::cout << learner << ": " << total / static_cast<int>(scores.size()) << '\n';
    std::cout << scores.at(2) << '\n';
    return 0;
}
/* expect:
Ada: 80
90
*/
