#include "types.h"

int main(int argc, char **argv) {
    Array< java_lang_String* >* args = new Array<java_lang_String *>(argc);
    for (int n = 0; n < argc; n++) args->set(n, cstr_byte_to_JavaString((const char *)argv[n]));
    __ENTRY_POINT_METHOD__(args);

    return 0;
}
