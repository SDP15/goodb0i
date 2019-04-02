
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
rotate([0,0,90]) uncenter(1, 5.1) block(
    width=5.1,
    length=1,
    horizontal_holes=true,
    reinforcement=true
);
translate([-7.8,40,0]) uncenter(1, 5.1) block(
    width=1,
    length=5.1,height=1,
    horizontal_holes=true,
    reinforcement=true
);
translate([40-0.2,7.8,0]) rotate([0,0,90]) uncenter(1, 5.025) block(
    width=5.025,
    length=1,
    horizontal_holes=true,
    reinforcement=true
);

translate([8*2,8*3-0.1,5]) difference() {
    union() {
        cube([8*4,8*4+0.2,10], center=true);
        rotate([15,0,0]) cylinder(h=PLen,r=OuterRadius);
    }
    rotate([15,0,0]) cylinder(h=PLen+1,r=InnerRadius);
}
}
