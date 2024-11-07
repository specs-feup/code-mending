/*union A {

};*/

/*struct A {

};
int main() {
    //union A a;
    //a.memb = 2;

    int a;

    const char *const b[2];

    return a * b;

    struct A obj1;
    //struct A obj2;

    obj1 *a;

    //obj2 * b;

}
*/

typedef int cmender_type_alias_0;

struct cmender_tag_type_6 {};

struct cmender_tag_type_5 {};

typedef int cmender_type_alias_9;

struct cmender_tag_type_9 {
    cmender_type_alias_9 midtexture;
};

typedef int cmender_type_alias_1;

typedef struct cmender_tag_type_6 cmender_type_alias_4;

struct cmender_tag_type_3 {};

struct cmender_tag_type_7 {};

struct cmender_tag_type_10 {};

struct cmender_tag_type_11 {};

typedef struct cmender_tag_type_11 * cmender_type_alias_10;

typedef struct cmender_tag_type_9 *cmender_type_alias_8;

struct cmender_tag_type_1 {};

struct cmender_tag_type_2 {};

struct cmender_tag_type_8 {};

typedef struct cmender_tag_type_8 cmender_type_alias_6;

typedef struct cmender_tag_type_8 cmender_type_alias_7;

typedef struct cmender_tag_type_6 cmender_type_alias_5;

struct cmender_tag_type_4 {
    cmender_type_alias_5 frontsector;
    cmender_type_alias_7 backsector;
    cmender_type_alias_8 sidedef;
};

typedef struct cmender_tag_type_4 *cmender_type_alias_2;

typedef struct cmender_tag_type_4 *cmender_type_alias_3;

struct cmender_tag_type_0 {
    cmender_type_alias_3 curline;
};

typedef struct cmender_tag_type_0 drawseg_t;

cmender_type_alias_4 frontsector;

cmender_type_alias_1 col;

cmender_type_alias_10 texturetranslation;

cmender_type_alias_6 backsector;

cmender_type_alias_0 column_t;

cmender_type_alias_2 curline;



void
R_RenderMaskedSegRange
( drawseg_t*	ds,
  int		x1,
  int		x2 )
{
    unsigned	index;
    column_t*	col;
    int		lightnum;
    int		texnum;

    // Calculate light table.
    // Use different light tables
    //   for horizontal / vertical / diagonal. Diagonal?
    // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
    curline = ds->curline;
    frontsector = curline->frontsector;
    backsector = curline->backsector;
    //curline->sidedef->midtexture + 1;
    texnum = texturetranslation[curline->sidedef->midtexture + 1];
}
