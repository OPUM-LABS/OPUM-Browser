package com.mycompany.app.crop

/** Touch handles of the crop frame: the four corners, the four sides and the center. */
enum class Handle(internal val helper: HandleHelper) {
    b(CornerHandleHelper(Edge.c, Edge.b)),
    c(CornerHandleHelper(Edge.c, Edge.d)),
    d(CornerHandleHelper(Edge.e, Edge.b)),
    e(CornerHandleHelper(Edge.e, Edge.d)),
    f(VerticalHandleHelper(Edge.b)),
    g(HorizontalHandleHelper(Edge.c)),
    h(VerticalHandleHelper(Edge.d)),
    i(HorizontalHandleHelper(Edge.e)),
    j(CenterHandleHelper(null, null));
}
