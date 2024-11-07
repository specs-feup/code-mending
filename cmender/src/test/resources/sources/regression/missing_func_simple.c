int main() {
    f(); // we assume that 'f' is a function because 'type (*var)[5]' can also be interpreted as a function call
    g(1, 2, "");
    h(1, 2, f());
}
