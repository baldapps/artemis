# ![Artemis](../master/Artemis/artemis110x80.png) 
Artemis - The bugs hunter

## What is it?

Artemis is an Eclipse plugin based on CODAN framework of Eclipse CDT plugin, i.e.
the basic framework to manage C/C++ projects.

## Features

Artemis is fully integrated with Eclipse and you don't need other dependency to use it.

Here a list of all additional checks perfomed by Artemis in real-time.

* Virtual method call in constructor/destructor
* Throw exception in destructor
* Global variables in constructor
* Avoid magic numbers
* Local variable shadowing
* ~~Lack of copyright information~~ (included now in Eclipse 2019-06, deprecated in Artemis 2.x)
* Miss copy constructor or assignment operator
* Static variable in header file
* Variable without initialization
* Multiple variable declaration
* Direct float comparison
* Miss self check in assignment operator
* Miss reference return value in assignment operator
* ~~Goto statement used~~ (included now in Eclipse 2019-06, deprecated in Artemis 2.x)
* ~~Miss cases in switch~~ (included now in Eclipse 2019-06, deprecated in Artemis 2.x)
* ~~Miss default in switch~~ (included now in Eclipse 2019-06, deprecated in Artemis 2.x)
* Returning the address of a local variable
* Encapsulation violation
* Operator assign no return this
* Class members cannot be used in static methods
* Method should be static
* Class members cannot be written in constant methods
* Method should be constant
* Deletion using 'this' pointer
* Pointer reset after deletion
* Dynamic allocation of array
* Delete using void pointers
* Naming convention for classes
* Order convention for visibility labels
* Class fields visibility
* Avoid using 'catch all'
* 'catch all' position
* Avoid empty 'catch'
* Empty 'throw'
* Sizeof applied to arrays
* Nested sizeof operators
* Sizeof applied to void type
* Avoid trigraphs
* Avoid lambda default capture
* Hook to std namespace
* Float counter in for loop
* Usage of auto pointer
* Function or method in blacklist
* Memory compare with composite types
* Wrong use of c_str
* Miss brace in control statements
* Break in for loop
* Sizeof without parenthesis
* Miss init static variables class members
* Avoid use global variables
* Avoid inline costructor and destructor
* Avoid virtual functions inline
* Avoid virtual functions without virtual keyword
* Avoid overload&& overload|| overload,
* Abstract classes should be non-copyable
* Define at least one constructor for each class
* Avoid classes with multiple inheritance
* Avoid Classes with a copy Constructor and the default Destructor or assignment operator
* Avoid Classes with an assignment operator and the default Destructor or copy constructor
* Each constructor must call all base classes constructors
* Don't modify for loop counter in for loop body
* Avoid using structures
* Avoid to define functions with parameters passed by value
* Avoid hiding fields
* Use all fields in assignment operator
* Returning no const class field from const method
* Declared class fields but without any constructor
* Use explicit keyword where possible

## Configuration

For rules configuration look for "Code analisys" in Eclipse preferences in C++ menu.

## Bugs

Please report bugs in the GitHub issue tracker.

## License

Licensed under the Eclipse Public License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You can
see the LICENSE files for any detail. You may obtain a copy of the License at

https://www.eclipse.org/legal/epl-2.0/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
