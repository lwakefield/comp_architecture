= Lab 3: Building a multipler =

Please note that any code is available on the following repository: http://gitlab.iamlawrence.me/lawrence/comp_architecture/tree/master/tut_3

== Part 1 ==
In this part, I walk through an example of the hardware multiplier algorithm (v3).

Let us expand on the algorithm, by adding initilization and completion.

{{{
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
  }}}

Below we follow the data in the multiplicand and product registers, for the example 0b1010 * 1101 (d10 * d13). Note that overflow happens on the addition in step 80. We will need to take this into account when designing the multiplier, this will be done by adding an extra bit for the product register.

{{{
    |------|--------------|-------------|-------------------------------------------------------------|
    | Step | Multiplicand |   Product   |                           Comment                           |
    |------|--------------|-------------|-------------------------------------------------------------|
    |   10 | ZZZZ         | ZZZZ ZZZZ   | Uninitialized                                               |
    |   20 | 1010         | 0000 1101   | Initilized                                                  |
    |   30 |              | 1010 1101   | First loop, add multiplicand to product                     |
    |   40 |              | 0101 0110   | Shift product right                                         |
    |   50 |              | 0010 1011   | Second loop, shift product right                            |
    |   60 |              | 1100 1011   | Third loop, add multiplicand to product                     |
    |   70 |              | 0110 0101   | Shift product right                                         |
    |   80 |              | 1 0000 0101 | Fourth loop, add multiplicand to product, note the overflow |
    |   90 |              | 1000 0010   | Shift product right                                         |
    |------|--------------|-------------|-------------------------------------------------------------|

}}}

== Part 2: Building a better multipler ==

The code provided in Mul.vcd uses the first algorithm, which takes 2n clock cycles to complete. The goal in this part is to build a new multiplier Mulv3.vcd, which uses the algorithm 3 that is outlined in part 1 in order to achieve n clock cycles to complete a multiplication.

The gtkwave output for Mul.vcd can be seen below.

The first thing that was done was to change the test bench to make sure that the multiplier completed in n clock cycles. The logic for Mulv3.vcd is actually simpler than Mul.vcd, removing the left shift logic and removing the multiplier register, as we now only need two registers to perform the multiplication. The git diff for the multiplier logic is shown below.

{{{
-  when (state === UInt(0)) { 
+  when (io.load) {
+    state := UInt(0)
     m.io.load := io.load
-  } .elsewhen (state(0) === UInt(0)) { 
+  }.
+  elsewhen (m.io.z(0) === UInt(0)) { 
     m.io.rshift := Bool(true)
-    m.io.lshift := Bool(true)
-  } .elsewhen (state(0) === UInt(1)) { 
+  }.
+  elsewhen (m.io.z(0) === UInt(1)) { 
m.io.write := Bool(true)
}
}}}

The output of Mulv3.vcd can be seen in the gtkwave screenshot shown below.

=== Part 3: Signed multiplication ===

The goal of this part is to modify Mulv3.vcd to perform signed multiplication, specifically while still taking n clock cycles to complete. Booth's multiplication algorithm was used to implement signed multiplication. This algorithm is outlined below.

Given we want to compute z = y * x
{{{
let a = y | (n+1 zeroes)
let s = (the twos complement of y) | (n+1 zeroes)
let p = (n zeroes) | x | 0
for i from 0 to n:
    if the last two bits of p == 01:
        p += a
    else if the last two bits of p == 10:
        p += s
    p >> 1
z = the left 2n bits of p
}}}

Note in the above algorithm that the shift, must be an arithmetic shift. That is the new left most bit will be equal to the last left most bit eg. 1000 >> 1 = 1100 or 0100 >> 1 = 0010. To do an arithmetic shift in with the chisel library, you must make sure that the variable is of SInt type, which (strangely?) can only be shifted by a UInt. This specific piece of information, which is not documented in the chisel documentation, caused a large amount of time trying to debug why the arithmetic shift (>>) was performing as a logical shift (>>>).

In this part, to simplify the design, the Muldpath module was removed, such that there only exists a single Mulv4 module and it's respective tester. The logic of Mulv4 is shown below.

{{{
when (io.load) {
    state := SInt(0)
    product := Cat(SInt(0, width=n), io.y, SInt(0))
    add := Cat(io.x, SInt(0, width=n+1))
    sub := Cat(~io.x + SInt(1), SInt(0, width=n+1))
}.elsewhen (product(1,0) === SInt(1)) {
    io.add := Bool(true)
    product := (product + add) >> UInt(1)
}.elsewhen (product(1,0) === SInt(2)) {
    io.sub := Bool(true)
    product := (product + sub) >> UInt(1)
}.otherwise {
    product := product >> UInt(1)
}
io.z := product(2*n, 1)
state := state + UInt(1)
}}}

It was unclear how to generate random signed numbers with ease for the test bench. It had to be taken into account that we loose a bit of magnitude when using signed integers, this had to be taken into account when generating random signed numbers. The code used to generate random numbers is show below, it is considered convoluted and not good practice. It is worth researching how to do this properly at a later point in time.

{{{
val ran = scala.util.Random
val inX = ran.nextInt((1 << (n-1)) - 1) * -1
val x = BigInt(inX)
val y = BigInt(n-1, scala.util.Random)
}}}

The output of Mulv4.vcd can be seen in the gtkwave screenshot shown below.

