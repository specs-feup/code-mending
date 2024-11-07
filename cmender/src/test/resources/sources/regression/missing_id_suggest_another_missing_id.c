// Here we have a lot missing ids that look like each other
// When we declare the first one, all other diagnostics will become err_undeclared_var_use_suggest instead of err_undeclared_var_use
// This err_undeclared_var_use_suggest will have a DeclarationNameArg as first param instead of IdentifierInfo
char *chat_macros[] =
{
    HUSTR_CHATMACRO0,
    HUSTR_CHATMACRO1,
    HUSTR_CHATMACRO2,
    HUSTR_CHATMACRO3,
    HUSTR_CHATMACRO4,
    HUSTR_CHATMACRO5,
    HUSTR_CHATMACRO6,
    HUSTR_CHATMACRO7,
    HUSTR_CHATMACRO8,
    HUSTR_CHATMACRO9
};
