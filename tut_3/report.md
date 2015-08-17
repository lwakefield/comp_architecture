# Question 1: Unsigned Multiplier version 3 Hand Solution

Let us expand on the algorithm, by adding initilization and completion.

```
def mul(n):
    # Initilization
    set multiplicand register to multiplicand input
    initilize left half of product register to zeroes
    set right half of product register to multiplier input

    # Do the multiplication
    for i from 0 to n:
        if the last bit in the product register is '1':
            add the multiplicand to the left half of the product register
        shift the product register to the right 1 bit

    # Write the result
    set the output to the product register
```

| Multiplicand |   Product   |                 Comment                                     |
|--------------|-------------|-------------------------------------------------------------|
|    ZZZZ      |   ZZZZ ZZZZ | Uninitialized                                               |
|    1010      |   0000 1101 | Initilized                                                  |
|              |   1010 1101 | First loop, add multiplicand to product                     |
|              |   0101 0110 | Shift product right                                         |
|              |   0010 1011 | Second loop, shift product right                            |
|              |   1100 1011 | Third loop, add multiplicand to product                     |
|              |   0110 0101 | Shift product right                                         |
|              | 1 0000 0101 | Fourth loop, add multiplicand to product, note the overflow |
|              |   1000 0010 | Shift product right                                         |
