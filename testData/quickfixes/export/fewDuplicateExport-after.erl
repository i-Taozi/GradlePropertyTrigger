-export([foo/0, foo/0, foo/0, tar/0]).
-export([bar/0, <caret>foo/0]).

foo() -> ok.
bar() -> ok.
tar() -> ok.