
#all: $(addsuffix .out, $(basename $(wildcard *.scala)))
all: Single.out

%.out: $(wildcard *.scala)
	sbt "run $* --backend c --compile --test --genHarness --vcd" 

%.v: $(wildcard *.scala)
	sbt "run $* --backend v --targetDir ./verilog --genHarness"

clean:
	rm -rf target project emulator verilog *.out *.cpp *.[coh] Single *.vcd
