
use <LEGO.scad>;

InnerRadius=15.5/2;
OuterRadius=InnerRadius+4;
PLen=40;

$fn=120;

/*difference() {
    union() {
        cylinder(h=PLen,r=OuterRadius);
        sphere(r=OuterRadius);
        rotate([90,0,0]) cylinder(h=PLen, r=OuterRadius);
    }
    union() {
        cylinder(h=PLen+0.1,r=InnerRadius);
        sphere(r=InnerRadius);
        rotate([90,0,0]) cylinder(h=PLen+0.1, r=InnerRadius);
    }
}
*/

union() {
uncenter(1, 5) block(
    width=1,
    length=5,
    horizontal_holes=true,
    reinforcement=true
);
rotate([0,0,90]) uncenter(1, 5) block(
    width=5,
    length=1,
    horizontal_holes=true,
    reinforcement=true,
    wall_play=0
);
place(5,-1,0) uncenter(1, 5) block(
    width=1,
    length=5,height=1,
    horizontal_holes=true,
    reinforcement=true
);
}
