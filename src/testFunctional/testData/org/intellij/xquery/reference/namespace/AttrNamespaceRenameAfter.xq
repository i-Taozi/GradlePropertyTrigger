module namespace aaa = "xxx";
import module namespace yyy = "yyy" at "yyy";
declare namespace zzz = "zzz";

declare function xxx() {
    <zzz:any aaa:any="val"/>
};