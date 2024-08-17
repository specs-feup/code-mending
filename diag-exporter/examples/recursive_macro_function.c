#define h_func i_func
#define g_func h_func
#define f_func g_func

void unused_func() {
    return f_func();
}
