This is a set of eclipse plugins for developing aldor code.

Early stages, don't expect much beyond very basic functionality.

To Use:
1) Install the plug in (the 'aldor' project contains the 'Aldor' feature).
2) Create a new project
3) Do 'Configure/Enable Aldor Builder' from the project's context menu
4) Go to project properties and set the location of your aldor executable.
5) Create a .as file and edit away.  The file will be built every time you save the file.

Compilation errors are displayed in the 'Problems' view.  All aldor output (including errors) is sent to
the console tab.

Dependencies between files are created by adding --DEPS: comments at the top of the file.
So for example

pb_ring.as contains:
#include "aldor"

PBRing: Category == with {
	+: (%, %) -> %;
}

pbfuncs.as contains:

--DEPS: pb_ring

f(T: PBRing, x: T): T == x+x;

This will set up a dependency between pbfuncs and pb_ring - meaning that pbfuncs will be compiled with pb_ring
in scope.

