module namespace xxx = "xxx";
import module namespace yyy = "yyy" at "yyy";
declare namespace zzz = "zzz";

declare function xxx() {
    <a>
        <zzz:any a<caret>aa:any="val" xmlns:aaa="ccc"/>
    </a>
};