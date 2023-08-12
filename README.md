# StackFlow
This is a programming language I made because I got the idea and didn't want to write tokenization code. THIS IS NOT MEANT TO BE A REAL LANGUAGE.

StackFlow is an interpreted language which runs on the JVM. It is basically the idea of "What if code was like RPN?". Each line is dedicated to a single instruction or data. Data lines push data onto the top of the stack, and instructions manipulate the stack. Multiple data/instructions cannot be placed on the same line.

Lines which begin with `//` are considered comments and are not parsed. At the moment, comments cannot be written on the same lines as other data/instruction code.

Indentation is ignored.

Note: Looking through the code, it looks like some of the Context access checks are a bit weird. I don't have time to go through and investigate right now, but if someone wants me to do it. Contact me or make and issue and I'll get around to it.

## Data
As mentioned above, StackFlow is, unsurprisingly, built around the idea of a stack. Every data line pushes new data to the stack, so the script
```
0
'STRING'
false
```
Would make the stack read `[false, 'STRING', 0]`.

The basic types are booleans, strings, numbers, and decimals, and null. Numbers are implemented with Java's `BigInteger` and `BigDecimal` classes, so they can be arbitrarily large. To force a number to be a decimal, you must append `.0`. The `d` postfix is not supported. All numbers are read in base 10.

Additional types can be created through the use of instructions. A full list of types is listed below:
`BOOLEAN, STRING, NUMBER, DECIMAL, ARRAY, TYPE, JAVA_OBJECT, JAVA_CLASS, JAVA_METHOD, JAVA_CONSTRUCTOR, OBJECT, CLASS, FUNCTION, VARIABLE, NULL`

The following syntax is used to define data:
`null`: Pushes `null` onto the stack
`"<any text>"`: Parses the text in the same format as a Java String and pushes it onto the stack
`'<any text>'`: Pushes the raw text onto the stack without further processing
`<any integer>`: Pushes the integer onto the stack
`<any decimal>`: Pushes the decimal number onto the stack
`noop`: Pushes a noop function onto the stack

## Functions
Function definition is accomplished through the use of the `beginFunc` keyword. The interpreter will interpret all code between `beginFunc` and `endFunc` as a function. A FUNCTION object is then pushed onto the stack which is able to run the function code. Each function also keeps a reference to the context in which it was CREATED. This means that any function can access variables from the context it was defined in, but not necessarily the context in which it is called.

## Instructions
Instructions perform operations on the stack. A full list of instructions is provided below. Some of the argument types are listed as "STRING/\*Any\*", This means that they can accept any value, but that value will be converted to a string, so using a string-like value is recommended.

### Instruction List

#### `call`
Runs the provided FUNCTION
##### Inputs
Function to call (FUNCTION)

#### `jcall`
Runs the provided JAVA_FUNCTION by attempting to read as many arguments as necessary from the stack in REVERSE ORDER. This means that the last argument to the function is at the top of the stack (behind the JAVA_FUNCTION object itself). If the method is non-static, an object to call the method on must be placed on the stack behind the JAVA_FUNCTION. Type conversion is attempted as follows:
| Java Expects | Supported StackFlow Types |
| - | - |
| An Object | An object of the required type (`JAVA_OBJECT`s will be unwrapped) |
| An Array | An `ARRAY` populated with objects which can be coerced (by these same rules) to the necessary type |
| String | *Any* |
| `char`/`Character` | `NUMBER` or `STRING` (Only the first character will be passed) |
| `byte`/`short`/`int`/`long`/`Byte`/`Short`/`Integer`/`Long` | `NUMBER` |
| `float`/`double`/`Float`/`Double` | `DECIMAL` |

All other parameters will be passed as they are stored on the stack. `null` will always be passed as `null`.

If the function's return value is non-void, it will be converted back into a valid StackFlow type and pushed onto the stack.
##### Inputs
```
<PARAMETERS>
( Object to call the function on (*Any*) )?
The Function (JAVA_FUNCTION)
```

#### `jNew`
Runs the provided JAVA_CONSTRUCTOR by attempting to read as many arguments as necessary from the stack in REVERSE ORDER. This means that the last argument to the constructor is at the top of the stack (behind the JAVA_CONSTRUCTOR object itself). Type conversion is attempted in the same manner as `jcall`.

The new object will be converted back into a valid StackFlow type and pushed onto the stack.
##### Inputs
```
<PARAMETERS>
The Constructor (JAVA_CONSTRUCTOR)
```

#### `findClass`
Searches for a Java class with the specified name, and pushes it onto the stack.
##### Inputs
```
Fully-Qualified Class Name (STRING/*Any*)
```

#### `findField`
Searches for a Java field with the specified name, coerces its current value into a valid StackFlow type, and pushes it onto the stack.
##### Inputs
```
The object to retrieve the field from (*Any*)
The field name (STRING/*Any*)
Class containing the field (JAVA_CLASS)
```

#### `findStaticField`
Searches for a static Java field with the specified name, coerces its current value into a valid StackFlow type, and pushes it onto the stack.
##### Inputs
```
The field name (STRING/*Any*)
Class containing the field (JAVA_CLASS)
```

#### `findMethod`
Searches for a Java method with the specified name, and pushes it onto the stack. Keep in mind that the types passed to this method are in FORWARD ORDER. This means that the first type of the function is at the top of the stack (behind the parameter count).
##### Inputs
```
The parameter types (JAVA_CLASS)
The number of parameters the method requires (NUMBER)
Class containing the method (JAVA_CLASS)
```

#### `findConstructor`
Searches for a Java constructor with the specified name, and pushes it onto the stack. Keep in mind that the types passed to this method are in FORWARD ORDER. This means that the first type of the constructor is at the top of the stack (behind the parameter count).
##### Inputs
```
The parameter types (JAVA_CLASS)
The number of parameters the constructor requires (NUMBER)
Class containing the constructor (JAVA_CLASS)
```

#### `define`
Sets the value of a variable in the current context. If the variable is not found this context, but is found in a super-context of the current context, that variable is set instead. Otherwise, the variable is created in the current context and set to the specified value.
##### Inputs
```
The value to set the variable to (*Any*)
The variable name (STRING/*Any*)
```

#### `set`
Sets the value of a variable.
##### Inputs
```
The value to set the variable to (*Any*)
The variable (VARIABLE)
```

#### `val`
Gets the value of a variable.
##### Inputs
```
The variable (VARIABLE)
```

#### `var`
Retrieves a variable in the current context. If the variable is not found this context, but is found in a super-context of the current context, that variable is retrieved instead.
##### Inputs
```
The variable name (STRING/*Any*)
```

#### `eval`
Retrieves a variable in the current context and gets its value. If the variable is not found this context, but is found in a super-context of the current context, that variable's value is retrieved instead.
##### Inputs
```
The variable name (STRING/*Any*)
```

#### `delete`
Deletes a variable in the current context. If the variable is not found this context, but is found in a super-context of the current context, that variable is deleted instead.
##### Inputs
```
The variable name (STRING/*Any*)
```

#### `pushCtx`
Enters a new context with the previous context as a super-context. All variables defined in the old context are still accessible, but any new variables will only be defined in the new context.

#### `pushRootCtx`
Enters a new root context with the previous context as a super-context. All variables defined in the old context are not accessible. Any new variables will only be defined in the new context. Calling `popCtx` will return to the previous context.

#### `popCtx`
Returns to the previous super-context. All variables defined in the old sub-context are no longer accessible.

#### `pushStack`
Pushes the current stack onto the stack stack. After this operation the stack is cleared.

#### `popStack`
Pops the most recent stack off of the stack stack, overwriting the current stack. This operation will throw an error if `pushStack` has not been called

#### `clearStack`
Clears the stack

#### `swp`
Swaps the two top elements on the stack

#### `pull`
Pulls the n-th item on the stack (0 indexed) to the top of the stack. (ex. `0` `pull` is a noop)
##### Inputs
```
The index to pull from (NUMBER)
```

#### `typeOf`
Pushes the type of the item at the top of the stack onto the stack.
##### Inputs
```
The object to retrieve the type of (*Any*)
```

#### `string`
Attempts to convert the item at the top of the stack to a string.
##### Inputs
```
The object to convert to a string (*Any*)
```

#### `type`
Attempts to push a TYPE object with the specified name onto the stack. Pushes `null` if no such TYPE is found.
##### Inputs
```
The type name to retrieve (*Any*)
```

#### `array`
Attempts to create push an array with the specified values onto the stack. Keep in mind that the values are in REVERSE ORDER. This means that the last value is at the top of the stack (behind the array length).
##### Inputs
```
The elements to store in the array (*Any*)
The number of elements in the array (NUMBER)
```

#### `len`
Pushes the length of the ARRAY at the top of the stack onto the stack.
##### Inputs
```
The array to retrieve the length of (ARRAY)
```

#### `get`
Pushes the n-th element of the ARRAY at the top of the stack onto the stack. (0 indexed)
##### Inputs
```
The array to retrieve from (ARRAY)
The index to retrieve from (NUMBER)
```

#### `put`
Sets the n-th element of an ARRAY to a new value. (0 indexed)
##### Inputs
```
The array to set (ARRAY)
The new value to set (*Any*)
The index to set (NUMBER)
```

#### `for`
Runs a basic for loop with the specified parameters. The command accepts four functions: The initialization function, the check function, the increment function, and the loop body. All the functions are run in a combined context with the super-context being the one captured by the LOOP BODY function. This means that the other three functions cannot access variables defined in their own respective super-contexts. The initialization function is run once before the loop begins. The check function is run before each iteration of the loop. It must leave either `true` or `false` on the stack. If the check function leaves the value `false`, the loop is exited. The increment function is run after each iteration of the loop. The loop body is run once per iteration of the loop.
##### Inputs
```
The loop body (FUNCTION)
The initialization function (FUNCTION)
The check function (FUNCTION)
The increment function (FUNCTION)
```

#### `while`
Runs a basic while loop with the specified parameters. The command accepts two functions: The check function and the loop body. All the functions are run in a combined context with the super-context being the one captured by the LOOP BODY function. This means that the other function cannot access variables defined in their own respective super-contexts. The check function is run before each iteration of the loop. It must leave either `true` or `false` on the stack. If the check function leaves the value `false`, the loop is exited. The loop body is run once per iteration of the loop.
##### Inputs
```
The loop body (FUNCTION)
The check function (FUNCTION)
```

#### `doWhile`
Runs a basic do-while loop with the specified parameters. The command accepts two functions: The check function and the loop body. All the functions are run in a combined context with the super-context being the one captured by the LOOP BODY function. This means that the other function cannot access variables defined in their own respective super-contexts. The check function is run after each iteration of the loop. It must leave either `true` or `false` on the stack. If the check function leaves the value `false`, the loop is exited. The loop body is run once per iteration of the loop.
##### Inputs
```
The loop body (FUNCTION)
The check function (FUNCTION)
```

#### `break`
Breaks out of the most recent loop. If this is called from within a function which is not running a loop, it will force that function to immediately return and break out of the loop which called it.

#### `continue`
Continues to the next iteration of the most recent loop. If this is called from within a function which is not running a loop, it will force that function to immediately return and continue to the next iteration of the loop which called it.

#### `return`
Returns from the current function. If this is called from within a running a loop, it will force that loop to immediately break.

#### `if`
Runs a function if and only if a condition is met.
##### Inputs
```
The function to run if the condition is met (FUNCTION)
The condition (BOOLEAN)
```

#### `ifElse`
Runs a function if a condition is met, and another function if it is not.
##### Inputs
```
The function to run if the condition is met (FUNCTION)
The function to run if the condition is not met (FUNCTION)
The condition (BOOLEAN)
```

#### `tryCatch`
Attempts to run a function, and if an error is thrown, runs another function after pushing the error onto the stack.
##### Inputs
```
The function to run (FUNCTION)
The function to run if an error is thrown (FUNCTION)
```

#### `tryFinally`
Attempts to run a function, always running another function afterwards regardless of whether or not an error is thrown.
##### Inputs
```
The function to run (FUNCTION)
The function to run afterwards (FUNCTION)
```

#### `tryCatchFinally`
Attempts to run a function, and if an error is thrown, runs another function after pushing the error onto the stack. Another function is always run afterwards regardless of whether or not an error is thrown.
##### Inputs
```
The function to run (FUNCTION)
The function to run if an error is thrown (FUNCTION)
The function to run afterwards (FUNCTION)
```

#### `throw`
Throws an error.
##### Inputs
```
The error to throw (JAVA_OBJECT - Must be an instance of Throwable)
```

#### `assert`
Asserts that a condition is true. If the condition is false, an error is thrown.
##### Inputs
```
The condition (BOOLEAN)
```

#### `load`
Loads a script from a file and runs it. This has the same effect as if the script was written in the current script.
##### Inputs
```
The path to the script to load (STRING/*Any*)
```

#### `include`
Load a script from a file and runs it in a sub-context of the current-context. After the script is run, the context is stored in an OBJECT with no associated CLASS, pushed onto the stack, and popped.
##### Inputs
```
The path to the script to load (STRING/*Any*)
```

#### `import`
Load a script from a file and runs it in a sub-context of the current-context on a new stack. After the script is run, the context is stored in an OBJECT with no associated CLASS, pushed onto the stack, and popped.
##### Inputs
```
The path to the script to load (STRING/*Any*)
```

**TODO: document classes, objects, remaining instructions**

#### `copy`
Copies the top element of the stack and pushes it onto the stack. For objects/functions, this will copy a reference to the object/function, not the object/function itself. For arrays, this will copy the array, but not the elements of the array. For all other types, this will copy the value.
##### Inputs
```
The element to copy (*Any*)
```

#### `pop`
Pops the top element of the stack and discards it.
##### Inputs
```
The element to pop (*Any*)
```

#### `expand`
Expands the ARRAY at the top of the stack into its elements. This will push each element of the array onto the stack in REVERSE ORDER. This means that the last element of the array will be at the top of the stack.
##### Inputs
```
The array to expand (ARRAY)
```

### Operators
There are two types of operators, unary and binary. Unary instructions only alter the first element of the stack, whereas binary instructions reduce the first two to a single element. Running operators on any type not listed here is unsupported. A table of supported operators is listed below:
| Instruction | Type | Effect on BOOLEANs | Effect on STRINGs | Effect on NUMBERs | Effect on DECIMALs | Effect on ARRAYs | Effect on FUNCTIONs |
| - | - | - | - | - | - | - | - |
| `int` | Unary | `true` -> `1` and `false` -> `0` | Gets the STRING length | *noop* | Truncates to a NUMBER | Gets the ARRAY length | *Unsupported* |
| `add` | Binary | Logical OR | Concatenates the STRINGs | Addition | Addition | Concatenates the ARRAYs | Concatenates the FUNCTIONs to a new FUNCTION which runs both FUNCTIONs in series |
| `inc` | Unary | Logical NOT | Increments the NUMBER | Increments the DECIMAL | *Unsupported* | Creates a new ARRAY with an additional `null` element appended to the end | *Unsupported* |
| `sub` | Binary | Logical XOR | *Unsupported* | Subtraction | Subtraction | *Unsupported* | *Unsupported* |
| `dec` | Unary | Logical NOT | Decrements the NUMBER | Decrements the DECIMAL | *Unsupported* | Creates a new ARRAY with the last element removed | *Unsupported* |
| `mult` | Binary | *Unsupported* | Multiplication | Multiplication | *Unsupported* | *Unsupported* | *Unsupported* |
| `div` | Binary | *Unsupported* | Division | Division | *Unsupported* | *Unsupported* | *Unsupported* |
| `mod` | Binary | *Unsupported* | Modulus | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `not` | Binary | Logical NOT | Logical NOT | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `bitShift` | Binary | *Unsupported* | Left bit-shift (Can be negative to perform a right shift) | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `gt` | Binary | `true` if and only if `true` is compared to `false` (`false` otherwise) | `true` if [String.compareTo](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareTo-java.lang.String-) would return a positive number for the given operands (`false` otherwise) | `true` if the first argument is greater than the second (`false` otherwise) | `true` if the first argument is greater than the second (`false` otherwise) | True if the first array is longer than the second (`false` otherwise) | *Unsupported* |
| `lt` | Binary | `true` if and only if `false` is compared to `true` (`false` otherwise) | `true` if [String.compareTo](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareTo-java.lang.String-) would return a negative number for the given operands (`false` otherwise) | `true` if the first argument is less than the second (`false` otherwise) | `true` if the first argument is less than the second (`false` otherwise) | True if the first array is shorter than the second (`false` otherwise) | *Unsupported* |
| `gteq` | Binary | `false` if and only if `false` is compared to `true` (`true` otherwise) | `true` if [String.compareTo](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareTo-java.lang.String-) would return a positive number or zero for the given operands (`false` otherwise) | `true` if the first argument is greater than or equal to the second (`false` otherwise) | `true` if the first argument is greater than or equal to the second (`false` otherwise) | True if the first array is longer or the same length as the second (`false` otherwise) | *Unsupported* |
| `lteq` | Binary | `false` if and only if `true` is compared to `false` (`false` otherwise) | `true` if [String.compareTo](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareTo-java.lang.String-) would return a negative number or zero for the given operands (`false` otherwise) | `true` if the first argument is less than or equal to the second (`false` otherwise) | `true` if the first argument is less than or equal to the second (`false` otherwise) | True if the first array is shorter or the same length as the second (`false` otherwise) | *Unsupported* |
| `eq` | Binary | `true` if the two BOOLEANs have the same value (`false` otherwise) | `true` and only if the STRINGs are the same (`false` otherwise) | `true` if the two NUMBERs are the same (`false` otherwise) | `true` if the two DECIMALs are the same (`false` otherwise) | True if the two ARRAYs are the same length and have identical elements (`false` otherwise) (This means that nested arrays would have to contain the same array object which is not always guaranteed. Please do not use on nested arrays.) | *Unsupported* |
| `neq` | Binary | `true` if the two BOOLEANs do not have the same value (`false` otherwise) | `true` and only if the STRINGs are not the same (`false` otherwise) | `true` if the two NUMBERs are not the same (`false` otherwise) | `true` if the two DECIMALs are not the same (`false` otherwise) | True if the two ARRAYs are not the same length or do not have identical elements (`false` otherwise) (This is the same as `eq` followed by `not` and the same note about nested arrays applies.) | *Unsupported* |
| `and` | Binary | Logical AND | Logical AND | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `or` | Binary | Logical OR | Logical OR | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `nand` | Binary | Logical NAND | Logical NAND | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `nor` | Binary | Logical NOR | Logical NOR | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `xor` | Binary | Logical XOR | Logical XOR | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `xnor` | Binary | Logical XNOR | Logical XNOR | *Unsupported* | *Unsupported* | *Unsupported* | *Unsupported* |
| `neg` | Unary | Logical NOT | Flips Sign | Flips Sign | *Unsupported* | *Unsupported* | *Unsupported* |
| `abs` | Unary | *Unsupported* | Gets the absolute value of the NUMBER | Gets the absolute value of the DECIMAL | *Unsupported* | *Unsupported* | *Unsupported* |
| `pow` | Binary | *Unsupported* | Raises one NUMBER to the power of another | Raises one DECIMAL to the power of another | *Unsupported* | *Unsupported* | *Unsupported* |

Special Behaviors:
* Basic type coercion (using decimal calculations when combining DECIMALs and NUMBERs, converting types to STRING, etc.)
* Automatic unboxing of variables (VARIABLE objects are converted to their value)
* TYPE objects are automatically converted to STRINGs
* Operator overloading is supported. CLASSes and OBJECTs can define methods with one of the above names and they will be called on the class/object on the TOP of the stack

`null` is only supported on binary operators and will be interpreted based on the type of the other element as follows:
| Type of the other element | Interpreted Value |
| - | - |
| ARRAY | An empty array |
| BOOLEAN | `false` |
| NUMBER/DECIMAL | `0` |
| FUNCTION | `noop` |
| STRING | `""` |
| TYPE | `NULL` (The type) |
| VARIABLE | A variable with value `null` (This will be unboxed and converted to the type of the other variable) |
| All other types | *Unsupported* |

## Running the Code
The interpreter accepts a single argument: The path to the file you wish to run. It can be executed via the command `./gradlew run --args="<YOUR SCRIPT HERE (Absolute path or relative from the build directory)"` or `java -jar <built application jar> <YOUR SCRIPT HERE>`.
