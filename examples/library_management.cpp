#include <iostream>
#include <string>
#include <vector>

struct Book {
    int id;
    std::string title;
    bool borrowed = false;
};

int main() {
    std::vector<Book> books = {{1, "C Fundamentals"}, {2, "C++ Foundations"}};
    for (;;) {
        std::cout << "\n1. List books\n2. Borrow\n3. Return\n0. Exit\nChoice: ";
        int choice;
        if (!(std::cin >> choice)) return 0;
        if (choice == 0) break;
        if (choice == 1) {
            for (const auto& b : books)
                std::cout << b.id << " " << b.title << " [" << (b.borrowed ? "Borrowed" : "Available") << "]\n";
            continue;
        }
        if (choice != 2 && choice != 3) { std::cout << "Invalid choice\n"; continue; }
        std::cout << "Book ID: ";
        int id;
        if (!(std::cin >> id)) return 0;
        bool found = false;
        for (auto& b : books) if (b.id == id) {
            found = true;
            if (choice == 2 && !b.borrowed) { b.borrowed = true; std::cout << "Borrowed\n"; }
            else if (choice == 3 && b.borrowed) { b.borrowed = false; std::cout << "Returned\n"; }
            else std::cout << "Operation unavailable\n";
            break;
        }
        if (!found) std::cout << "Book not found\n";
    }
}
