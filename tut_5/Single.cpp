#include "Single.h"

void Single_t::init ( val_t rand_init ) {
  this->__srand(rand_init);
  Single__hi.randomize(&__rand_seed);
  Single__lo.randomize(&__rand_seed);
  Single__regfile.randomize(&__rand_seed);
  Single__imem.randomize(&__rand_seed);
  Single__pc.randomize(&__rand_seed);
  clk.len = 1;
  clk.cnt = clk.len;
  clk.values[0] = 0;
}


int Single_t::clock ( dat_t<1> reset ) {
  uint32_t min = ((uint32_t)1<<31)-1;
  if (clk.cnt < min) min = clk.cnt;
  clk.cnt-=min;
  if (clk.cnt == 0) clock_lo( reset );
  if (clk.cnt == 0) clock_hi( reset );
  if (clk.cnt == 0) clk.cnt = clk.len;
  return min;
}


void Single_t::print ( FILE* f ) {
}
void Single_t::print ( std::ostream& s ) {
}


void Single_t::dump_init ( FILE* f ) {
  fputs("$timescale 1ps $end\n", f);
  fputs("$scope module Single $end\n", f);
  fputs("$var wire 1 \x21 clk $end\n", f);
  fputs("$var wire 1 \x22 io_valid $end\n", f);
  fputs("$var wire 1 \x23 io_boot $end\n", f);
  fputs("$var wire 1 \x24 io_isWr $end\n", f);
  fputs("$var wire 10 \x25 io_wrAddr $end\n", f);
  fputs("$var wire 10 \x26 pc $end\n", f);
  fputs("$var wire 10 \x27 io_pc $end\n", f);
  fputs("$var wire 32 \x28 hi $end\n", f);
  fputs("$var wire 32 \x29 lo $end\n", f);
  fputs("$var wire 32 \x2a io_out $end\n", f);
  fputs("$var wire 32 \x2b io_wrData $end\n", f);
  fputs("$var wire 32 \x2c io_inst $end\n", f);
  fputs("$upscope $end\n", f);
  fputs("$enddefinitions $end\n", f);
  fputs("$dumpvars\n", f);
  fputs("$end\n", f);
  fputs("#0\n", f);
  dat_dump<1>(f, clk, 0x21);
  dat_dump<1>(f, Single__io_valid, 0x22);
  Single__io_valid__prev = Single__io_valid;
  dat_dump<1>(f, Single__io_boot, 0x23);
  Single__io_boot__prev = Single__io_boot;
  dat_dump<1>(f, Single__io_isWr, 0x24);
  Single__io_isWr__prev = Single__io_isWr;
  dat_dump<1>(f, Single__io_wrAddr, 0x25);
  Single__io_wrAddr__prev = Single__io_wrAddr;
  dat_dump<1>(f, Single__pc, 0x26);
  Single__pc__prev = Single__pc;
  dat_dump<1>(f, Single__io_pc, 0x27);
  Single__io_pc__prev = Single__io_pc;
  dat_dump<1>(f, Single__hi, 0x28);
  Single__hi__prev = Single__hi;
  dat_dump<1>(f, Single__lo, 0x29);
  Single__lo__prev = Single__lo;
  dat_dump<1>(f, Single__io_out, 0x2a);
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_wrData, 0x2b);
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__io_inst, 0x2c);
  Single__io_inst__prev = Single__io_inst;
}


void Single_t::dump ( FILE* f, int t ) {
  if (t == 0) return dump_init(f);
  fprintf(f, "#%d\n", t << 1);
  if (clk.len == clk.cnt)  goto L0;
K0:  if (Single__io_valid != Single__io_valid__prev)  goto L1;
K1:  if (Single__io_boot != Single__io_boot__prev)  goto L2;
K2:  if (Single__io_isWr != Single__io_isWr__prev)  goto L3;
K3:  if (Single__io_wrAddr != Single__io_wrAddr__prev)  goto L4;
K4:  if (Single__pc != Single__pc__prev)  goto L5;
K5:  if (Single__io_pc != Single__io_pc__prev)  goto L6;
K6:  if (Single__hi != Single__hi__prev)  goto L7;
K7:  if (Single__lo != Single__lo__prev)  goto L8;
K8:  if (Single__io_out != Single__io_out__prev)  goto L9;
K9:  if (Single__io_wrData != Single__io_wrData__prev)  goto L10;
K10:  if (Single__io_inst != Single__io_inst__prev)  goto L11;
K11:  fprintf(f, "#%d\n", (t << 1) + 1);
  if (clk.len == clk.cnt)  goto Z0;
C0:  return;
L0:
  clk.values[0] = 1;
  dat_dump<1>(f, clk, 0x21);
  goto K0;
L1:
  Single__io_valid__prev = Single__io_valid;
  dat_dump<1>(f, Single__io_valid, 0x22);
  goto K1;
L2:
  Single__io_boot__prev = Single__io_boot;
  dat_dump<1>(f, Single__io_boot, 0x23);
  goto K2;
L3:
  Single__io_isWr__prev = Single__io_isWr;
  dat_dump<1>(f, Single__io_isWr, 0x24);
  goto K3;
L4:
  Single__io_wrAddr__prev = Single__io_wrAddr;
  dat_dump<1>(f, Single__io_wrAddr, 0x25);
  goto K4;
L5:
  Single__pc__prev = Single__pc;
  dat_dump<1>(f, Single__pc, 0x26);
  goto K5;
L6:
  Single__io_pc__prev = Single__io_pc;
  dat_dump<1>(f, Single__io_pc, 0x27);
  goto K6;
L7:
  Single__hi__prev = Single__hi;
  dat_dump<1>(f, Single__hi, 0x28);
  goto K7;
L8:
  Single__lo__prev = Single__lo;
  dat_dump<1>(f, Single__lo, 0x29);
  goto K8;
L9:
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_out, 0x2a);
  goto K9;
L10:
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__io_wrData, 0x2b);
  goto K10;
L11:
  Single__io_inst__prev = Single__io_inst;
  dat_dump<1>(f, Single__io_inst, 0x2c);
  goto K11;
Z0:
  clk.values[0] = 0;
  dat_dump<1>(f, clk, 0x21);
  goto C0;
}




void Single_t::clock_lo ( dat_t<1> reset ) {
  { Single__io_valid.values[0] = 0x0L;}
  val_t T0;
  { T0 = Single__pc.values[0] >> 2;}
  T0 = T0 & 0xffL;
  val_t T1;
  { T1 = T0 | 0x0L << 8;}
  val_t Single__inst;
  { Single__inst = Single__imem.get(T1, 0);}
  { Single__rti.values[0] = Single__inst >> 16;}
  Single__rti.values[0] = Single__rti.values[0] & 0x1fL;
  val_t T2;
  { T2 = Single__regfile.get(Single__rti.values[0], 0);}
  val_t T3;
  T3 = Single__rti.values[0] == 0x0L;
  val_t Single__rb;
  { Single__rb = TERNARY(T3, 0x0L, T2);}
  val_t Single__rsi;
  { Single__rsi = Single__inst >> 21;}
  Single__rsi = Single__rsi & 0x1fL;
  val_t T4;
  { T4 = Single__regfile.get(Single__rsi, 0);}
  val_t T5;
  T5 = Single__rsi == 0x0L;
  val_t Single__ra;
  { Single__ra = TERNARY(T5, 0x0L, T4);}
  val_t T6;
  T6 = Single__ra * Single__rb;
  val_t T7;
  T7 = T6 >> 0x20L;
  val_t Single__funct;
  { Single__funct = Single__inst;}
  Single__funct = Single__funct & 0x3fL;
  val_t T8;
  T8 = 0x4L == Single__funct;
  val_t Single__op;
  { Single__op = Single__inst >> 26;}
  Single__op = Single__op & 0x3fL;
  val_t T9;
  T9 = Single__op == 0x1L;
  val_t T10;
  T10 = Single__op != 0x1L;
  val_t T11;
  { T11 = T10 ^ 0x1L;}
  val_t T12;
  { T12 = T11 & T9;}
  val_t T13;
  { T13 = Single__io_isWr.values[0] | Single__io_boot.values[0];}
  val_t T14;
  { T14 = T13 ^ 0x1L;}
  { T15.values[0] = T14 & T12;}
  val_t T16;
  { T16 = T15.values[0] & T8;}
  val_t T17;
  { T17 = TERNARY_1(T16, T7, Single__hi.values[0]);}
  { T18.values[0] = TERNARY(reset.values[0], 0x0L, T17);}
  val_t T19;
  { T19 = Single__lo.values[0] | 0x0L << 32;}
  val_t T20;
  T20 = Single__ra * Single__rb;
  val_t T21[2];
  { val_t __c = 0; val_t __w = 0x20L / 64; val_t __s = 0x20L % 64; val_t __r = 64 - __s; val_t __v0 = MASK(T20, (0 >= __w) & (0 < __w + 1)); T21[0] = __v0 << __s | __c; __c = MASK(__v0 >> __r, __s != 0); val_t __v1 = MASK(T20, (1 >= __w) & (1 < __w + 1)); T21[1] = __v1 << __s | __c; __c = MASK(__v1 >> __r, __s != 0);}
  T21[1] = T21[1] & 0xffffffffL;
  val_t T22;
  { unsigned int __amount = 0x20L; const unsigned int __in_words = 2; int __in_width = 96; val_t __d0[2]; rsh_n(__d0, &T21[0], __amount, __in_words); T22 = __d0[0];}
  val_t T23;
  { T23 = TERNARY_1(T16, T22, T19);}
  val_t T24;
  { T24 = TERNARY(reset.values[0], 0x0L, T23);}
  { T25.values[0] = T24;}
  T25.values[0] = T25.values[0] & 0xffffffffL;
  { Single__rdi.values[0] = Single__inst >> 11;}
  Single__rdi.values[0] = Single__rdi.values[0] & 0x1fL;
  val_t T26;
  { T26 = Single__rdi.values[0] | 0x0L << 5;}
  val_t T27;
  { T27 = Single__ra+T26;}
  T27 = T27 & 0xffffffffL;
  val_t T28;
  T28 = 0x0L == Single__op;
  { T29.values[0] = T14 & T10;}
  val_t T30;
  { T30 = T29.values[0] & T28;}
  val_t T31;
  { T31 = TERNARY(T30, T27, 0x0L);}
  val_t T32;
  { T32 = Single__ra+Single__rb;}
  T32 = T32 & 0xffffffffL;
  val_t T33;
  T33 = 0x0L == Single__funct;
  val_t T34;
  { T34 = T15.values[0] & T33;}
  val_t T35;
  { T35 = TERNARY_1(T34, T32, T31);}
  val_t T36;
  { T36 = Single__ra+Single__rb;}
  T36 = T36 & 0xffffffffL;
  val_t T37;
  T37 = 0x1L == Single__funct;
  val_t T38;
  { T38 = T15.values[0] & T37;}
  val_t T39;
  { T39 = TERNARY_1(T38, T36, T35);}
  val_t T40;
  { T40 = Single__ra | Single__rb;}
  val_t T41;
  T41 = 0x2L == Single__funct;
  val_t T42;
  { T42 = T15.values[0] & T41;}
  val_t T43;
  { T43 = TERNARY_1(T42, T40, T39);}
  val_t T44;
  T44 = Single__rb<Single__ra;
  val_t T45;
  { T45 = T44 | 0x0L << 1;}
  val_t T46;
  T46 = 0x3L == Single__funct;
  val_t T47;
  { T47 = T15.values[0] & T46;}
  val_t T48;
  { T48 = TERNARY_1(T47, T45, T43);}
  val_t T49;
  T49 = 0x5L == Single__funct;
  val_t T50;
  { T50 = T15.values[0] & T49;}
  val_t T51;
  { T51 = TERNARY_1(T50, Single__lo.values[0], T48);}
  val_t T52;
  T52 = 0x6L == Single__funct;
  val_t T53;
  { T53 = T15.values[0] & T52;}
  val_t T54;
  { T54 = TERNARY_1(T53, Single__hi.values[0], T51);}
  { Single__rc.values[0] = T54;}
  val_t T55;
  { T55 = Single__regfile.get(0x3L, 0);}
  { Single__io_out.values[0] = T55;}
  { Single__io_inst.values[0] = Single__inst;}
  val_t T56;
  { T56 = Single__io_isWr.values[0] ^ 0x1L;}
  val_t T57;
  { T57 = T56 & Single__io_boot.values[0];}
  val_t T58;
  { T58 = TERNARY(T57, 0x0L, Single__pc.values[0]);}
  val_t T59;
  { T59 = Single__pc.values[0]+0x4L;}
  T59 = T59 & 0x3ffL;
  val_t T60;
  { T60 = TERNARY_1(T14, T59, T58);}
  { T61.values[0] = TERNARY(reset.values[0], 0x0L, T60);}
  { Single__io_pc.values[0] = Single__pc.values[0];}
}


void Single_t::clock_hi ( dat_t<1> reset ) {
  dat_t<32> Single__hi__shadow = T18;
  dat_t<32> Single__lo__shadow = T25;
  { if (T15.values[0]) Single__regfile.put(Single__rdi.values[0], 0, Single__rc.values[0]);}
  { if (T29.values[0]) Single__regfile.put(Single__rti.values[0], 0, Single__rc.values[0]);}
  { if (Single__io_isWr.values[0]) Single__imem.put(Single__io_wrAddr.values[0], 0, Single__io_wrData.values[0]);}
  dat_t<10> Single__pc__shadow = T61;
  Single__hi = T18;
  Single__lo = T25;
  Single__pc = T61;
}


void Single_api_t::init_sim_data (  ) {
  sim_data.inputs.clear();
  sim_data.outputs.clear();
  sim_data.signals.clear();
  Single_t* mod = dynamic_cast<Single_t*>(module);
  assert(mod);
  sim_data.inputs.push_back(new dat_api<1>(&mod->Single__io_isWr));
  sim_data.inputs.push_back(new dat_api<10>(&mod->Single__io_wrAddr));
  sim_data.inputs.push_back(new dat_api<32>(&mod->Single__io_wrData));
  sim_data.inputs.push_back(new dat_api<1>(&mod->Single__io_boot));
  sim_data.outputs.push_back(new dat_api<1>(&mod->Single__io_valid));
  sim_data.outputs.push_back(new dat_api<32>(&mod->Single__io_out));
  sim_data.outputs.push_back(new dat_api<32>(&mod->Single__io_inst));
  sim_data.outputs.push_back(new dat_api<10>(&mod->Single__io_pc));
  sim_data.signals.push_back(new dat_api<5>(&mod->Single__rti));
  sim_data.signal_map["Single.rti"] = 0;
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__hi));
  sim_data.signal_map["Single.hi"] = 1;
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__lo));
  sim_data.signal_map["Single.lo"] = 2;
  sim_data.signals.push_back(new dat_api<5>(&mod->Single__rdi));
  sim_data.signal_map["Single.rdi"] = 3;
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__rc));
  sim_data.signal_map["Single.rc"] = 4;
  std::string Single__regfile_path = "Single.regfile";
  for (size_t i = 0 ; i < 32 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__regfile.contents[i]));
    sim_data.signal_map[Single__regfile_path+"["+itos(i,false)+"]"] = 5+i;
  }
  std::string Single__imem_path = "Single.imem";
  for (size_t i = 0 ; i < 1024 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__imem.contents[i]));
    sim_data.signal_map[Single__imem_path+"["+itos(i,false)+"]"] = 37+i;
  }
  sim_data.signals.push_back(new dat_api<10>(&mod->Single__pc));
  sim_data.signal_map["Single.pc"] = 1061;
  sim_data.clk_map["clk"] = new clk_api(&mod->clk);
}
