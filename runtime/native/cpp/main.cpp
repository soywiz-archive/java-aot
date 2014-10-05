
int main(int argc, char **argv) {
    printf("Start!\n");
    /*
    std::shared_ptr< Array< lava_lang_String > > args = std::shared_ptr<Array<lava_lang_String>>(new Array<java_lang_String>(argc));

    //args->set(<#int index#>, <#java_lang_String *value#>)
    for (int n = 0; n < argc; n++) {
        args->set(n, cstr_byte_to_JavaString((const char *)argv[n]));
    }
    */
    std::shared_ptr< Array< std::shared_ptr< java_lang_String > > > args;
    __ENTRY_POINT_METHOD__(args);
    return 0;
}
