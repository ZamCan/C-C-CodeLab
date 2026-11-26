#include <iostream>

class Shape {
public:
    virtual ~Shape() = default;
    virtual double area() const = 0;
};

class Rectangle : public Shape {
    double width, height;
public:
    Rectangle(double width, double height) : width{width}, height{height} {}
    double area() const override { return width * height; }
};

int main() {
    Rectangle rectangle{3.0, 4.0};
    const Shape& shape = rectangle;
    std::cout << shape.area() << '\n';
    return 0;
}
/* expect:
12
*/
