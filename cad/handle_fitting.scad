
InnerRadius=15.5/2;
OuterRadius=InnerRadius+4;
PLen=40;

$fn=120;

difference() {
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
