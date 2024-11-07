unsigned int n1;
const char *const n2[] = {};
//const char (*n2)(int, int);
/*struct A {
    int a;
    char b;
};

struct A n2;*/

/*typedef enum A TYPECHAR;
typedef const TYPECHAR *const TYPE1;
typedef TYPE1 TYPE;
TYPE n2;*/

/*const char *n3[23];
n3 = "332r2r;
n3[2]= '3';*/

void func(const int param) {
    const char *const n3[param] = {};

	n3 + "";
}

int main() {
    n1 = 1;
    //n2 = "abc";



    return n1 * n2;
}
