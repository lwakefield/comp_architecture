
#all: $(addsuffix .out, $(basename $(wildcard *.scala)))
all: Mul.out Mulv3.out

%.out: $(wildcard *.scala)
	sbt "run $* --backend c --targetDir ./emulator --compile --test --genHarness --vcd" 

%.v: $(wildcard *.scala)
	sbt "run $* --backend v --targetDir ./verilog --genHarness"

clean:
	rm -rf target project emulator verilog *.out
