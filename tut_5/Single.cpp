#include "Single.h"

void Single_t::init ( val_t rand_init ) {
  this->__srand(rand_init);
  Single__lo.randomize(&__rand_seed);
  Single__pc.randomize(&__rand_seed);
  Single__imem.randomize(&__rand_seed);
  Single__regfile.randomize(&__rand_seed);
  Single__hi.randomize(&__rand_seed);
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
  fputs("$var wire 6 \x25 io_funct $end\n", f);
  fputs("$var wire 6 \x26 io_op $end\n", f);
  fputs("$var wire 10 \x27 io_pc $end\n", f);
  fputs("$var wire 10 \x28 pc $end\n", f);
  fputs("$var wire 10 \x29 io_wrAddr $end\n", f);
  fputs("$var wire 32 \x2a io_out $end\n", f);
  fputs("$var wire 32 \x2b io_inst $end\n", f);
  fputs("$var wire 32 \x2c io_lo $end\n", f);
  fputs("$var wire 32 \x2d lo $end\n", f);
  fputs("$var wire 32 \x2e io_wrData $end\n", f);
  fputs("$var wire 32 \x2f hi $end\n", f);
  fputs("$var wire 32 \x30 io_hi $end\n", f);
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
  dat_dump<1>(f, Single__io_funct, 0x25);
  Single__io_funct__prev = Single__io_funct;
  dat_dump<1>(f, Single__io_op, 0x26);
  Single__io_op__prev = Single__io_op;
  dat_dump<1>(f, Single__io_pc, 0x27);
  Single__io_pc__prev = Single__io_pc;
  dat_dump<1>(f, Single__pc, 0x28);
  Single__pc__prev = Single__pc;
  dat_dump<1>(f, Single__io_wrAddr, 0x29);
  Single__io_wrAddr__prev = Single__io_wrAddr;
  dat_dump<1>(f, Single__io_out, 0x2a);
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_inst, 0x2b);
  Single__io_inst__prev = Single__io_inst;
  dat_dump<1>(f, Single__io_lo, 0x2c);
  Single__io_lo__prev = Single__io_lo;
  dat_dump<1>(f, Single__lo, 0x2d);
  Single__lo__prev = Single__lo;
  dat_dump<1>(f, Single__io_wrData, 0x2e);
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__hi, 0x2f);
  Single__hi__prev = Single__hi;
  dat_dump<1>(f, Single__io_hi, 0x30);
  Single__io_hi__prev = Single__io_hi;
}


void Single_t::dump ( FILE* f, int t ) {
  if (t == 0) return dump_init(f);
  fprintf(f, "#%d\n", t << 1);
  if (clk.len == clk.cnt)  goto L0;
K0:  if (Single__io_valid != Single__io_valid__prev)  goto L1;
K1:  if (Single__io_boot != Single__io_boot__prev)  goto L2;
K2:  if (Single__io_isWr != Single__io_isWr__prev)  goto L3;
K3:  if (Single__io_funct != Single__io_funct__prev)  goto L4;
K4:  if (Single__io_op != Single__io_op__prev)  goto L5;
K5:  if (Single__io_pc != Single__io_pc__prev)  goto L6;
K6:  if (Single__pc != Single__pc__prev)  goto L7;
K7:  if (Single__io_wrAddr != Single__io_wrAddr__prev)  goto L8;
K8:  if (Single__io_out != Single__io_out__prev)  goto L9;
K9:  if (Single__io_inst != Single__io_inst__prev)  goto L10;
K10:  if (Single__io_lo != Single__io_lo__prev)  goto L11;
K11:  if (Single__lo != Single__lo__prev)  goto L12;
K12:  if (Single__io_wrData != Single__io_wrData__prev)  goto L13;
K13:  if (Single__hi != Single__hi__prev)  goto L14;
K14:  if (Single__io_hi != Single__io_hi__prev)  goto L15;
K15:  fprintf(f, "#%d\n", (t << 1) + 1);
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
  Single__io_funct__prev = Single__io_funct;
  dat_dump<1>(f, Single__io_funct, 0x25);
  goto K4;
L5:
  Single__io_op__prev = Single__io_op;
  dat_dump<1>(f, Single__io_op, 0x26);
  goto K5;
L6:
  Single__io_pc__prev = Single__io_pc;
  dat_dump<1>(f, Single__io_pc, 0x27);
  goto K6;
L7:
  Single__pc__prev = Single__pc;
  dat_dump<1>(f, Single__pc, 0x28);
  goto K7;
L8:
  Single__io_wrAddr__prev = Single__io_wrAddr;
  dat_dump<1>(f, Single__io_wrAddr, 0x29);
  goto K8;
L9:
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_out, 0x2a);
  goto K9;
L10:
  Single__io_inst__prev = Single__io_inst;
  dat_dump<1>(f, Single__io_inst, 0x2b);
  goto K10;
L11:
  Single__io_lo__prev = Single__io_lo;
  dat_dump<1>(f, Single__io_lo, 0x2c);
  goto K11;
L12:
  Single__lo__prev = Single__lo;
  dat_dump<1>(f, Single__lo, 0x2d);
  goto K12;
L13:
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__io_wrData, 0x2e);
  goto K13;
L14:
  Single__hi__prev = Single__hi;
  dat_dump<1>(f, Single__hi, 0x2f);
  goto K14;
L15:
  Single__io_hi__prev = Single__io_hi;
  dat_dump<1>(f, Single__io_hi, 0x30);
  goto K15;
Z0:
  clk.values[0] = 0;
  dat_dump<1>(f, clk, 0x21);
  goto C0;
}




void Single_t::clock_lo ( dat_t<1> reset ) {
  { Single__io_valid.values[0] = 0x0L;}
  val_t T0;
  { T0 = Single__regfile.get(0x3L, 0);}
  { Single__io_out.values[0] = T0;}
  val_t T1;
  { T1 = Single__pc.values[0] >> 2;}
  T1 = T1 & 0xffL;
  val_t T2;
  { T2 = T1 | 0x0L << 8;}
  val_t Single__inst;
  { Single__inst = Single__imem.get(T2, 0);}
  { Single__io_inst.values[0] = Single__inst;}
  { Single__io_pc.values[0] = Single__pc.values[0];}
  val_t Single__funct;
  { Single__funct = Single__inst;}
  Single__funct = Single__funct & 0x3fL;
  { Single__io_funct.values[0] = Single__funct;}
  val_t Single__op;
  { Single__op = Single__inst >> 26;}
  Single__op = Single__op & 0x3fL;
  { Single__io_op.values[0] = Single__op;}
  { Single__io_lo.values[0] = Single__lo.values[0];}
  { Single__rti.values[0] = Single__inst >> 16;}
  Single__rti.values[0] = Single__rti.values[0] & 0x1fL;
  val_t T3;
  { T3 = Single__regfile.get(Single__rti.values[0], 0);}
  val_t T4;
  T4 = Single__rti.values[0] == 0x0L;
  val_t Single__rb;
  { Single__rb = TERNARY(T4, 0x0L, T3);}
  val_t Single__rsi;
  { Single__rsi = Single__inst >> 21;}
  Single__rsi = Single__rsi & 0x1fL;
  val_t T5;
  { T5 = Single__regfile.get(Single__rsi, 0);}
  val_t T6;
  T6 = Single__rsi == 0x0L;
  val_t Single__ra;
  { Single__ra = TERNARY(T6, 0x0L, T5);}
  val_t T7;
  T7 = Single__ra * Single__rb;
  val_t T8;
  { T8 = T7;}
  T8 = T8 & 0xffffffffL;
  val_t T9;
  T9 = 0x19L == Single__funct;
  val_t T10;
  T10 = Single__op == 0x0L;
  val_t T11;
  { T11 = Single__io_isWr.values[0] | Single__io_boot.values[0];}
  val_t T12;
  { T12 = T11 ^ 0x1L;}
  { T13.values[0] = T12 & T10;}
  val_t T14;
  { T14 = T13.values[0] & T9;}
  val_t T15;
  { T15 = TERNARY_1(T14, T8, Single__lo.values[0]);}
  { T16.values[0] = TERNARY(reset.values[0], 0x0L, T15);}
  val_t T17;
  { T17 = Single__io_isWr.values[0] ^ 0x1L;}
  val_t T18;
  { T18 = T17 & Single__io_boot.values[0];}
  val_t T19;
  { T19 = TERNARY(T18, 0x0L, Single__pc.values[0]);}
  val_t T20;
  { T20 = Single__pc.values[0]+0x4L;}
  T20 = T20 & 0x3ffL;
  val_t T21;
  { T21 = TERNARY_1(T12, T20, T19);}
  { T22.values[0] = TERNARY(reset.values[0], 0x0L, T21);}
  val_t T23;
  { T23 = Single__ra+Single__rb;}
  T23 = T23 & 0xffffffffL;
  val_t T24;
  T24 = 0x21L == Single__funct;
  val_t T25;
  { T25 = T13.values[0] & T24;}
  val_t T26;
  { T26 = TERNARY(T25, T23, 0x0L);}
  val_t T27;
  { T27 = Single__ra+Single__rb;}
  T27 = T27 & 0xffffffffL;
  val_t T28;
  T28 = 0x23L == Single__funct;
  val_t T29;
  { T29 = T13.values[0] & T28;}
  val_t T30;
  { T30 = TERNARY_1(T29, T27, T26);}
  val_t T31;
  { T31 = Single__ra | Single__rb;}
  val_t T32;
  T32 = 0x25L == Single__funct;
  val_t T33;
  { T33 = T13.values[0] & T32;}
  val_t T34;
  { T34 = TERNARY_1(T33, T31, T30);}
  val_t T35;
  T35 = Single__rb<Single__ra;
  val_t T36;
  { T36 = T35 | 0x0L << 1;}
  val_t T37;
  T37 = 0x2aL == Single__funct;
  val_t T38;
  { T38 = T13.values[0] & T37;}
  val_t T39;
  { T39 = TERNARY_1(T38, T36, T34);}
  val_t T40;
  T40 = 0x12L == Single__funct;
  val_t T41;
  { T41 = T13.values[0] & T40;}
  val_t T42;
  { T42 = TERNARY_1(T41, Single__lo.values[0], T39);}
  val_t T43;
  T43 = 0x10L == Single__funct;
  val_t T44;
  { T44 = T13.values[0] & T43;}
  val_t T45;
  { T45 = TERNARY_1(T44, Single__hi.values[0], T42);}
  val_t Single__shamt;
  { Single__shamt = Single__inst >> 6;}
  Single__shamt = Single__shamt & 0x1fL;
  val_t T46;
  { T46 = Single__funct | Single__shamt << 6;}
  { Single__rdi.values[0] = Single__inst >> 11;}
  Single__rdi.values[0] = Single__rdi.values[0] & 0x1fL;
  val_t T47;
  { T47 = T46 | Single__rdi.values[0] << 11;}
  val_t T48;
  { T48 = T47 | 0x0L << 16;}
  val_t T49;
  T49 = 0x9L == Single__op;
  val_t T50;
  { T50 = T10 ^ 0x1L;}
  { T51.values[0] = T12 & T50;}
  val_t T52;
  { T52 = T51.values[0] & T49;}
  val_t T53;
  { T53 = TERNARY_1(T52, T48, T45);}
  { Single__rc.values[0] = T53;}
  val_t T54;
  { T54 = T7 >> 32;}
  T54 = T54 & 0xffffffffL;
  val_t T55;
  { T55 = TERNARY_1(T14, T54, Single__hi.values[0]);}
  { T56.values[0] = TERNARY(reset.values[0], 0x0L, T55);}
  { Single__io_hi.values[0] = Single__hi.values[0];}
}


void Single_t::clock_hi ( dat_t<1> reset ) {
  dat_t<32> Single__lo__shadow = T16;
  dat_t<10> Single__pc__shadow = T22;
  { if (Single__io_isWr.values[0]) Single__imem.put(Single__io_wrAddr.values[0], 0, Single__io_wrData.values[0]);}
  { if (T51.values[0]) Single__regfile.put(Single__rti.values[0], 0, Single__rc.values[0]);}
  { if (T13.values[0]) Single__regfile.put(Single__rdi.values[0], 0, Single__rc.values[0]);}
  dat_t<32> Single__hi__shadow = T56;
  Single__lo = T16;
  Single__pc = T22;
  Single__hi = T56;
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
  sim_data.outputs.push_back(new dat_api<6>(&mod->Single__io_funct));
  sim_data.outputs.push_back(new dat_api<6>(&mod->Single__io_op));
  sim_data.outputs.push_back(new dat_api<32>(&mod->Single__io_lo));
  sim_data.outputs.push_back(new dat_api<32>(&mod->Single__io_hi));
  sim_data.signals.push_back(new dat_api<5>(&mod->Single__rti));
  sim_data.signal_map["Single.rti"] = 0;
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__lo));
  sim_data.signal_map["Single.lo"] = 1;
  sim_data.signals.push_back(new dat_api<10>(&mod->Single__pc));
  sim_data.signal_map["Single.pc"] = 2;
  std::string Single__imem_path = "Single.imem";
  for (size_t i = 0 ; i < 1024 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__imem.contents[i]));
    sim_data.signal_map[Single__imem_path+"["+itos(i,false)+"]"] = 3+i;
  }
  sim_data.signals.push_back(new dat_api<5>(&mod->Single__rdi));
  sim_data.signal_map["Single.rdi"] = 1027;
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__rc));
  sim_data.signal_map["Single.rc"] = 1028;
  std::string Single__regfile_path = "Single.regfile";
  for (size_t i = 0 ; i < 32 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__regfile.contents[i]));
    sim_data.signal_map[Single__regfile_path+"["+itos(i,false)+"]"] = 1029+i;
  }
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__hi));
  sim_data.signal_map["Single.hi"] = 1061;
  sim_data.clk_map["clk"] = new clk_api(&mod->clk);
}
