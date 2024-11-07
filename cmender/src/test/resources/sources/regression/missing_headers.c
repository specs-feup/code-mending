// Purpose: Test for missing headers.
// Expected errors: 2
//     - err_pp_file_not_found(975): 'missing.h' file not found
//     - err_pp_file_not_found(975): 'sys/a' file not found
// Expected result:
//     - create inside include path: sys/a
//     - create inside include path: missing.h
#include <sys/a>
#include "missing.h"

int main() { }
