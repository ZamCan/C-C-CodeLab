package com.zamcan.codelab;

/**
 * Authored C++ course. It builds on the C track without pretending the two languages are the same:
 * C++ inherits C's efficiency goals but adds its own type system, object lifetime rules and
 * standard library. Historical dialogue is explicitly marked as dramatisation; technical claims are
 * factual and every example is compiled by {@code tools/verify_curriculum_examples.py}.
 */
final class LessonScriptCpp {
    private LessonScriptCpp() {}

    private static String src(String file) { return "curriculum/cpp/" + file; }

    static final LessonScript.Lesson[] LESSONS = {
        new LessonScript.Lesson("cpp-meet", "Meet C++", "C with Classes became something larger", 7, src("01-meet-cpp.cpp"),
            Beat.hook("C++ is often described as 'C with classes'. That description is historically true and technically misleading. What actually changed?"),
            Beat.say("Bjarne Stroustrup · imagined dialogue", """
                    I did not want a language that ignored C's strengths. I wanted to keep the efficiency \
                    and the closeness to hardware, while making large programs easier to organise. Work \
                    on 'C with Classes' began in 1979; the name C++ arrived in 1983."""),
            Beat.say("Dennis Ritchie · imagined dialogue", """
                    A bigger toolbox still needs a careful engineer. C++ kept much of C's syntax, but its \
                    type rules and object lifetimes are its own."""),
            Beat.explain("""
                    Simula's classes influenced the object-oriented features; C supplied the foundation. \
                    C++ is not Simula renamed, and it is not merely C with a different print function: \
                    some valid C is invalid C++, and C++ adds references, classes, templates, exceptions \
                    and a large standard library."""),
            Beat.visual("cpp-lineage", "C (1972) → C with Classes (1979) → C++ (1983) → modern ISO C++."),
            Beat.code(src("01-meet-cpp.cpp"), "Hello from C++"),
            Beat.define("""
                    C++ is a general-purpose, statically typed, compiled language supporting procedural, \
                    object-oriented and generic programming. Programs are translated ahead of time, and \
                    the language does not require garbage collection."""),
            Beat.check("Who developed C++, and from what starting point?",
                    new String[]{"Bjarne Stroustrup, from work on C with Classes", "Dennis Ritchie, from B", "James Gosling, from C"},
                    0,
                    "Stroustrup began C with Classes in 1979 at Bell Labs; the language was named C++ in 1983. Ritchie created C; Gosling created Java."),
            Beat.explain("""
                    C++ supports several paradigms rather than forcing one. A small embedded routine may \
                    look exactly like C; a large application may use classes, generic algorithms and \
                    resource-owning types. Choosing a paradigm is a design decision, not a language \
                    requirement."""),
            Beat.tryIt("Open the example and change the language string. Then remove `std::` from cout and read the diagnostic.", src("01-meet-cpp.cpp")),
            Beat.reflect("Next: the first C++ program, where `std::cout` replaces printf and the type system starts to differ.")),

        new LessonScript.Lesson("cpp-streams", "Streams and the first program", "iostream, std:: and the insertion operator", 7, src("02-streams.cpp"),
            Beat.hook("In C, printf needs a format string that must match the arguments. C++ offers a different idea: let the type decide how it is written. What could go wrong?"),
            Beat.code(src("02-streams.cpp"), "Name: Ada has 12 lessons"),
            Beat.define("A stream is an ordered sequence of characters connected to a source or destination, with type-aware insertion and extraction operations."),
            Beat.explain("""
                    `#include <iostream>` provides the standard stream objects. `std::` names the standard \
                    library namespace: `std::cout` is the standard output stream, `std::cin` the standard \
                    input stream and `std::cerr` the diagnostic stream. `<<` inserts a value into a stream \
                    and returns the stream, which is why insertions can be chained."""),
            Beat.explain("""
                    Streaming is type-aware, so there is no format string to mismatch. `'\\n'` writes a \
                    newline; `std::endl` writes a newline and flushes the buffer. Prefer `'\\n'` in loops: \
                    flushing repeatedly costs time for little benefit."""),
            Beat.visual("streams", "Values flow into a stream object; the stream's overload for each type decides the textual form."),
            Beat.check("Where should diagnostics normally be written?",
                    new String[]{"std::cerr", "std::cout", "A variable named error"},
                    0,
                    "std::cerr is the standard diagnostic stream. Keeping diagnostics separate lets a caller redirect normal output without losing errors."),
            Beat.predict("`std::cout << 7 / 2;` prints what?",
                    new String[]{"3", "3.5", "3.500000"},
                    0,
                    "Both operands are integers, so integer division happens before the stream sees the value. Streams format the result; they do not change arithmetic."),
            Beat.explain("""
                    `std::cin >> learner` skips leading whitespace and reads one word. A name with a \
                    space needs `std::getline`. Mixing extraction and getline requires consuming the \
                    leftover newline, otherwise getline reads an empty line. Check the stream state before \
                    using a value: a failed extraction leaves the target unchanged."""),
            Beat.tryIt("Open the example, remove the std:: prefixes one at a time and read the compiler's message. Then restore them and add a third line.", src("02-streams.cpp")),
            Beat.reflect("Streams move formatting into the type system. The remaining responsibility is yours: validate that extraction actually succeeded.")),

        new LessonScript.Lesson("cpp-types", "Types and initialization", "Braces, const and auto", 7, src("03-types.cpp"),
            Beat.hook("`int x;` leaves x with an unspecified value in C++. Modern C++ offers a syntax that makes forgetting the initializer harder. Why does that matter?"),
            Beat.define("""
                    Initialization establishes an object's value when its lifetime begins. C++ distinguishes \
                    initialization from later assignment."""),
            Beat.explain("""
                    `const int days{7};` uses brace initialization, which rejects narrowing conversions such \
                    as `int x{2.9}`. `double hours{1.5};` initialises directly. `auto total = days * hours;` \
                    deduces the type from the initializer — here double. `auto` is static typing with \
                    deduction, not a dynamically typed variable."""),
            Beat.visual("init", "Declaration creates the object; the initializer sets its first value. A const object can be initialised once and never assigned."),
            Beat.code(src("03-types.cpp"), "10.5"),
            Beat.predict("What does `int count{3.9};` do?",
                    new String[]{"Fails to compile because of narrowing", "Stores 3", "Stores 4"},
                    0,
                    "Brace initialization rejects narrowing from double to int. `int count = 3.9;` would compile, silently truncating — one reason braces are preferred."),
            Beat.check("What does const express?",
                    new String[]{"The value cannot be changed after initialization", "The value is stored in ROM", "The variable is global"},
                    0,
                    "const is a compile-time promise about modification, not a storage location. Its address can still be observed, and it can live on the stack."),
            Beat.explain("""
                    Prefer initialising every object at declaration. It removes a class of bugs where a \
                    value is read before being set, and it lets the type be deduced honestly. Use named \
                    constants instead of repeating literals: a pass mark that appears five times is a \
                    change waiting to be missed."""),
            Beat.tryIt("Change `const int days{7}` to `int days{7}`, assign 8, then restore const and read the diagnostic. Then try `int days{7.5}`.", src("03-types.cpp")),
            Beat.reflect("Initialization is a design statement: it tells the reader what the object means before anything else touches it.")),

        new LessonScript.Lesson("cpp-references", "References and functions", "Aliases, const references and avoiding copies", 8, src("04-references.cpp"),
            Beat.hook("C passes arguments by value; changing a parameter cannot change the caller's object. Pointers solve that, but the call site stops looking like a normal call. Is there a safer notation?"),
            Beat.define("""
                    A reference is an alias for an existing object. It must be bound when created, cannot be \
                    reseated to another object, and is not a separate object with its own address."""),
            Beat.explain("""
                    `void increment(int& value)` binds value to the caller's int, so `++value` changes the \
                    caller's variable. `const std::string& text` passes without copying while forbidding \
                    modification through that reference. For large objects this avoids an expensive copy; \
                    for small scalars the copy is cheaper and clearer."""),
            Beat.visual("reference", "The reference and the original name designate the same object. There is one value, two ways to reach it."),
            Beat.code(src("04-references.cpp"), "3\n7"),
            Beat.predict("After `increment(count)` where count is 2, what is count?",
                    new String[]{"3", "2", "Undefined"},
                    0,
                    "The reference parameter is the caller's object, so the increment is visible to the caller."),
            Beat.check("When is `const T&` the better parameter type?",
                    new String[]{"When the function reads a potentially large object without copying it", "When the function must modify the argument", "When the argument is a single int"},
                    0,
                    "A const reference avoids a copy and documents read-only intent. Use a non-const reference to modify, and a value for small scalars."),
            Beat.explain("""
                    A reference must not outlive the object it aliases. Returning a reference to a local \
                    variable is a dangling reference, just as returning its address would be a dangling \
                    pointer. When ownership is unclear, return by value and let the language manage the \
                    lifetime."""),
            Beat.tryIt("Add `void reset(int& value)` that sets the argument to zero. Call it and print before and after. Then try passing a literal and read the error.", src("04-references.cpp")),
            Beat.reflect("Choose the parameter form that states the contract: value for copies, `T&` to modify, `const T&` to observe cheaply.")),

        new LessonScript.Lesson("cpp-classes", "Classes and invariants", "Protecting a rule, not just grouping data", 8, src("05-classes.cpp"),
            Beat.hook("A bank account must never have a negative balance under its own rules. Where should that rule live: in every caller, or in the type itself?"),
            Beat.define("""
                    A class is a user-defined type combining data members and operations. An invariant is a \
                    condition that every public operation must preserve."""),
            Beat.explain("""
                    Members before the first access specifier are private; `public:` opens the interface. \
                    Private data cannot be changed by arbitrary code, so the class can enforce its rule. A \
                    constructor establishes a valid initial state. A `const` member function promises not \
                    to modify the object."""),
            Beat.code(src("05-classes.cpp"), "2"),
            Beat.explain("""
                    In the example, `value` starts at 0 and increment refuses to exceed 100. Callers cannot \
                    set the counter to 500 because they cannot reach the member. If a class only stores \
                    public fields and getters, it is a structure with extra syntax; a class earns its keep \
                    by protecting a meaningful rule."""),
            Beat.visual("class", "Private state on the inside, a small public surface on the outside. Every public call must leave the invariant true."),
            Beat.predict("What is printed after two increments from a fresh Counter?",
                    new String[]{"2", "0", "100"},
                    0,
                    "Each increment adds one while the value stays below 100, so two calls produce 2."),
            Beat.check("Which statement describes an invariant?",
                    new String[]{"A rule that remains true after every public operation", "A variable whose type can change", "A comment describing the class"},
                    0,
                    "An invariant is a property of valid state that the interface preserves. Tests can check it after each operation."),
            Beat.tryIt("Add a `decrement()` that refuses to go below zero. Test it at the boundary, then make the member public and observe how the guarantee disappears.", src("05-classes.cpp")),
            Beat.reflect("Encapsulation is not secrecy for its own sake. It is the ability to keep one rule true in one place.")),

        new LessonScript.Lesson("cpp-containers", "Strings and vectors", "Managed storage you can still reason about", 8, src("06-strings-vectors.cpp"),
            Beat.hook("A class list grows during enrolment, and a name is more than one character. Manual reallocation and NUL terminators work, but they are easy to get wrong. What do the standard types take over?"),
            Beat.define("""
                    std::string owns a sequence of characters; std::vector owns a resizable contiguous \
                    sequence of elements. Both manage their storage and release it when destroyed."""),
            Beat.explain("""
                    `push_back` grows the vector, reallocating when capacity is exhausted. Growing can \
                    invalidate references, pointers and iterators into the vector, because the elements \
                    may move. `size()` returns the element count; `empty()` states intent; `at(i)` checks \
                    the index and throws `std::out_of_range`, whereas `operator[]` assumes validity."""),
            Beat.visual("vector", "Elements are contiguous; capacity may exceed size. Growth may move the block, so old references are not guaranteed to survive."),
            Beat.code(src("06-strings-vectors.cpp"), "Ada: 80\n90"),
            Beat.predict("What does `scores.at(9)` do for a three-element vector?",
                    new String[]{"Throws std::out_of_range", "Returns 0", "Reads adjacent memory"},
                    0,
                    "at() validates the index and throws. operator[] performs no check, so an invalid index is undefined behaviour."),
            Beat.check("What does `scores.size()` return?",
                    new String[]{"The number of elements as std::size_t", "The allocated capacity", "The largest index"},
                    0,
                    "size() counts elements. The largest valid index is size() - 1, and capacity() may be larger than size()."),
            Beat.explain("""
                    Averaging needs a guard: an empty vector has no average. Compute the sum in a wider \
                    type, then divide by the count only after checking `!scores.empty()`. `static_cast` \
                    states the conversion explicitly, so integer division does not surprise the reader."""),
            Beat.tryIt("Add a fourth score, compute the average of only passing scores, and handle the case where no score passes.", src("06-strings-vectors.cpp")),
            Beat.reflect("Prefer owning standard types over manual buffers. They make lifetime, size and bounds explicit without giving up direct control of algorithms.")),

        new LessonScript.Lesson("cpp-algorithms", "Algorithms", "Ranges, iterators and generic operations", 8, src("07-algorithms.cpp"),
            Beat.hook("Sorting, searching and summing appear in every program. Rewriting them for each container and element type is wasted effort and a source of defects. What if the algorithm did not care about the container?"),
            Beat.define("""
                    A standard algorithm is a function template operating on an iterator range. A range is \
                    described by a beginning iterator and an end iterator that points one past the last \
                    element."""),
            Beat.explain("""
                    `std::sort(values.begin(), values.end())` reorders the range in place. `std::find` \
                    returns an iterator to the first match, or `end()` when nothing matches. `end()` is a \
                    position, not an element: dereferencing it is undefined behaviour. The half-open range \
                    [begin, end) is what makes empty ranges and loops expressible without special cases."""),
            Beat.visual("algorithms", "A half-open range: begin points at the first element, end points one past the last."),
            Beat.code(src("07-algorithms.cpp"), "2 5 9\nfound 5"),
            Beat.predict("After sorting `{9, 2, 5}`, what is the value at `values[0]`?",
                    new String[]{"2", "9", "5"},
                    0,
                    "Ascending order by operator< puts the smallest value first. std::sort does not preserve the original positions."),
            Beat.check("What must you check before dereferencing the result of std::find?",
                    new String[]{"That the iterator is not equal to end()", "That the container is not const", "That the value is positive"},
                    0,
                    "end() signals 'not found'. Dereferencing it would read past the range."),
            Beat.explain("""
                    Prefer algorithms to hand-written loops when the intent has a name: sort, find, count, \
                    accumulate. The algorithm states the operation, and its complexity is documented. A \
                    manual loop is still appropriate when it clarifies a stateful or unusual traversal."""),
            Beat.tryIt("Sort descending with a comparison, then count how many scores exceed the average. Reuse the verified algorithm rather than writing a custom sort.", src("07-algorithms.cpp")),
            Beat.reflect("Algorithms plus ranges separate what you want from how the container stores it. That separation is the source of C++'s generic power.")),

        new LessonScript.Lesson("cpp-inheritance", "Inheritance and polymorphism", "One interface, several implementations", 9, src("08-inheritance.cpp"),
            Beat.hook("A program draws circles, rectangles and triangles. Calling `area()` on each concrete type works, but a function that accepts any shape needs a common promise. How is that promise expressed?"),
            Beat.define("""
                    Inheritance derives one class from another. A virtual function allows a derived class to \
                    override behaviour selected at runtime through a base-class reference or pointer. A pure \
                    virtual function makes the base abstract: it cannot be instantiated."""),
            Beat.explain("""
                    `virtual double area() const = 0;` declares a contract with no implementation. \
                    `Rectangle : public Shape` promises to fulfil it, and `override` makes a mismatch a \
                    compile-time error. Calling through `const Shape&` dispatches to the actual object's \
                    implementation."""),
            Beat.visual("inherit", "One abstract interface above; concrete implementations below. A base reference can point at any derived object."),
            Beat.code(src("08-inheritance.cpp"), "12"),
            Beat.predict("Why does `Shape` have a virtual destructor in this example?",
                    new String[]{"So deleting through a base pointer destroys the derived object correctly", "So area() can be inline", "So Shape becomes abstract"},
                    0,
                    "Deleting a derived object through a base pointer without a virtual destructor is undefined behaviour. The virtual destructor makes cleanup correct."),
            Beat.check("What does `override` require?",
                    new String[]{"A base virtual function with a matching signature", "A pure virtual class", "A public constructor"},
                    0,
                    "override asks the compiler to verify that the function really overrides a base virtual function. A typo becomes an error instead of a silent new function."),
            Beat.explain("""
                    Inheritance is not the default tool for reuse. Prefer composition when one type merely \
                    uses another; prefer inheritance when a genuine is-a relationship and runtime \
                    substitution are both required. Deep hierarchies become hard to change, so keep \
                    interfaces small and behaviour explicit."""),
            Beat.tryIt("Add a `Circle` class with a radius and area. Create both shapes through a reference and print both areas. Then remove `override` and reintroduce a typo in the signature.", src("08-inheritance.cpp")),
            Beat.reflect("Polymorphism moves a decision from compile time to runtime. Use it when the caller genuinely should not know the concrete type.")),

        new LessonScript.Lesson("cpp-raii", "Lifetime and RAII", "Resources tied to objects, not to discipline", 9, src("09-raii.cpp"),
            Beat.hook("Every opened file and allocated block must eventually be released — including when an error occurs halfway through. What if the language released them for you?"),
            Beat.define("""
                    RAII (resource acquisition is initialization) binds a resource to an object's lifetime: \
                    the constructor acquires, the destructor releases, and scope determines when that happens."""),
            Beat.explain("""
                    `std::vector` owns its buffer, `std::string` owns its characters and `std::unique_ptr` \
                    owns one object. When the owning object is destroyed — at the end of scope, or while \
                    unwinding an exception — the resource is released exactly once. That removes most \
                    manual `delete` calls and the double-free and leak defects that accompany them."""),
            Beat.visual("raii", "Acquire in the constructor, use while alive, release in the destructor. Every exit path performs the release."),
            Beat.code(src("09-raii.cpp"), "42\n3"),
            Beat.predict("When is the int owned by `std::make_unique<int>(42)` released?",
                    new String[]{"When its unique_ptr is destroyed", "Only after an explicit delete", "At program exit even if the pointer is local"},
                    0,
                    "The unique_ptr owns the object; destroying the pointer destroys the object. That is the point of the owning type."),
            Beat.check("Why is `std::unique_ptr` preferable to a raw owning pointer?",
                    new String[]{"It expresses single ownership and releases automatically", "It is faster at runtime", "It allows copying"},
                    0,
                    "unique_ptr encodes ownership in the type, cannot be copied accidentally, and releases its object on destruction. Moving transfers ownership explicitly."),
            Beat.explain("""
                    A raw pointer is still useful for observing an object owned elsewhere, but then it must \
                    never be deleted. Document that distinction. `std::shared_ptr` is for genuinely shared \
                    ownership; reaching for it by default hides a design question about who owns what."""),
            Beat.tryIt("Replace the unique_ptr with a raw `new int` and add an early return before the delete. Observe the leak, then restore the owning type.", src("09-raii.cpp")),
            Beat.reflect("Ownership is a design decision. Encoding it in types lets the compiler and the runtime enforce what comments cannot.")),

        new LessonScript.Lesson("cpp-templates", "Templates", "One algorithm, many types, checked at compile time", 8, src("10-templates.cpp"),
            Beat.hook("`larger(3, 7)` and `larger(\"Ada\", \"Bjarne\")` are the same idea over different types. Overloading works, but it duplicates the body. Is there a way to write it once without losing type checking?"),
            Beat.define("""
                    A template is a compile-time pattern from which the compiler generates concrete \
                    functions or classes for the types actually used."""),
            Beat.explain("""
                    `template <typename T> T larger(const T& a, const T& b)` declares a function template. \
                    Calling it with two ints instantiates a version for int; calling it with std::string \
                    instantiates another. The operation used inside — here `>` — must be valid for the type, \
                    and the error appears at compile time when it is not."""),
            Beat.code(src("10-templates.cpp"), "7\nBjarne"),
            Beat.predict("What happens if you call `larger(3, \"text\")`?",
                    new String[]{"A compile error: conflicting types for T", "It compares the text with 3", "It returns 3"},
                    0,
                    "Both parameters share one type T. Passing int and const char* conflicts, and deduction cannot produce a single T."),
            Beat.check("When is a template instantiated?",
                    new String[]{"At compile time, for the types actually used", "At runtime, on first call", "Never; it is interpreted"},
                    0,
                    "Templates are a compile-time mechanism. The generated code is ordinary typed code with no runtime dispatch cost."),
            Beat.explain("""
                    Templates power the standard containers and algorithms. Keep them small and give the \
                    requirements a name in the documentation: a type must be comparable, or copyable, or \
                    support a particular operation. A template that fails with twenty lines of diagnostics \
                    is usually one that hid its requirements."""),
            Beat.tryIt("Add a `template <typename T> T smaller(const T&, const T&)` and call it with ints and doubles. Then call it with mixed types and read the diagnostic.", src("10-templates.cpp")),
            Beat.reflect("Generic code is a contract over operations, checked by the compiler. Write the requirement down before writing the template.")),

        new LessonScript.Lesson("cpp-interop", "C interoperability and robust design", "Using C from C++, and failing honestly", 9, src("11-interop.cpp"),
            Beat.hook("Millions of lines of C libraries still matter. A C++ program must be able to call them without pretending the two languages share every rule. What exactly crosses the boundary?"),
            Beat.define("""
                    Language linkage controls how a function name is represented for linking. `extern "C"` \
                    requests C linkage, which is what a C library expects."""),
            Beat.explain("""
                    `extern "C" int c_add(int, int);` is a declaration with C linkage, so the linker looks \
                    for the C name rather than a C++ mangled name. It does not translate classes, \
                    references or exceptions: expose a C-compatible interface (plain types, explicit \
                    ownership) on the boundary. Letting a C++ exception cross into C code is undefined \
                    behaviour, so catch and translate errors before returning."""),
            Beat.visual("interop", "A narrow C-compatible boundary: plain data in, explicit status out, no exceptions crossing."),
            Beat.code(src("11-interop.cpp"), "42\n3"),
            Beat.check("What does `extern \"C\"` change?",
                    new String[]{"Language linkage for the declared functions", "The language the body is compiled as", "Whether classes are allowed"},
                    0,
                    "The body is still C++ code. Only the linkage of the declared names changes so C and C++ objects can be linked together."),
            Beat.explain("""
                    Robustness is a design property. Separate input, domain rules, storage and presentation. \
                    Validate at the boundary, return explicit status values, and test the failure paths. A \
                    program that only works for perfect input is not finished, it is untested."""),
            Beat.predict("Which failure belongs at the API boundary rather than deep inside?",
                    new String[]{"An invalid argument supplied by the caller", "A local loop counter being incremented", "A string literal being printed"},
                    0,
                    "Arguments from outside the component are untrusted. Validate once at the boundary, then let internal code rely on the documented contract."),
            Beat.check("Why must a C++ exception never propagate into C code?",
                    new String[]{"C has no mechanism to handle or unwind it safely", "It is slower than a return code", "C compilers reject all C++ syntax"},
                    0,
                    "C frames cannot participate in C++ unwinding, so the behaviour is undefined. Convert the failure into a status value at the boundary."),
            Beat.tryIt("Open the example, add a second extern \"C\" function that multiplies, and call it from main.", src("11-interop.cpp")),
            Beat.reflect("""
                    That completes the C++ track: streams, types, references, classes, containers, \
                    algorithms, inheritance, lifetime, templates and interoperability. Guided projects \
                    combine them; the compiler will tell you the truth about every experiment.""")),
    };
}
