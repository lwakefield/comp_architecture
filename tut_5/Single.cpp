#include "Single.h"

void Single_t::init ( val_t rand_init ) {
  this->__srand(rand_init);
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
  fputs("$var wire 32 \x28 io_out $end\n", f);
  fputs("$var wire 32 \x29 io_wrData $end\n", f);
  fputs("$var wire 32 \x2a io_inst $end\n", f);
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
  dat_dump<1>(f, Single__io_out, 0x28);
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_wrData, 0x29);
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__io_inst, 0x2a);
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
K6:  if (Single__io_out != Single__io_out__prev)  goto L7;
K7:  if (Single__io_wrData != Single__io_wrData__prev)  goto L8;
K8:  if (Single__io_inst != Single__io_inst__prev)  goto L9;
K9:  fprintf(f, "#%d\n", (t << 1) + 1);
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
  Single__io_out__prev = Single__io_out;
  dat_dump<1>(f, Single__io_out, 0x28);
  goto K7;
L8:
  Single__io_wrData__prev = Single__io_wrData;
  dat_dump<1>(f, Single__io_wrData, 0x29);
  goto K8;
L9:
  Single__io_inst__prev = Single__io_inst;
  dat_dump<1>(f, Single__io_inst, 0x2a);
  goto K9;
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
  val_t Single__rdi;
  { Single__rdi = Single__inst >> 11;}
  Single__rdi = Single__rdi & 0x1fL;
  val_t T2;
  { T2 = Single__rdi | 0x0L << 5;}
  val_t Single__rsi;
  { Single__rsi = Single__inst >> 21;}
  Single__rsi = Single__rsi & 0x1fL;
  val_t T3;
  { T3 = Single__regfile.get(Single__rsi, 0);}
  val_t T4;
  T4 = Single__rsi == 0x0L;
  val_t Single__ra;
  { Single__ra = TERNARY(T4, 0x0L, T3);}
  val_t T5;
  { T5 = Single__ra+T2;}
  T5 = T5 & 0xffffffffL;
  val_t Single__op;
  { Single__op = Single__inst >> 26;}
  Single__op = Single__op & 0x3fL;
  val_t T6;
  T6 = 0x9L == Single__op;
  val_t T7;
  { T7 = Single__io_isWr.values[0] | Single__io_boot.values[0];}
  { T8.values[0] = T7 ^ 0x1L;}
  val_t T9;
  { T9 = T8.values[0] & T6;}
  val_t T10;
  { T10 = TERNARY(T9, T5, 0x0L);}
  { Single__rc.values[0] = T10;}
  { Single__rti.values[0] = Single__inst >> 16;}
  Single__rti.values[0] = Single__rti.values[0] & 0x1fL;
  val_t T11;
  { T11 = Single__regfile.get(0x3L, 0);}
  { Single__io_out.values[0] = T11;}
  { Single__io_inst.values[0] = Single__inst;}
  val_t T12;
  { T12 = Single__io_isWr.values[0] ^ 0x1L;}
  val_t T13;
  { T13 = T12 & Single__io_boot.values[0];}
  val_t T14;
  { T14 = TERNARY(T13, 0x0L, Single__pc.values[0]);}
  val_t T15;
  { T15 = Single__pc.values[0]+0x4L;}
  T15 = T15 & 0x3ffL;
  val_t T16;
  { T16 = TERNARY_1(T8.values[0], T15, T14);}
  { T17.values[0] = TERNARY(reset.values[0], 0x0L, T16);}
  { Single__io_pc.values[0] = Single__pc.values[0];}
}


void Single_t::clock_hi ( dat_t<1> reset ) {
  { if (T8.values[0]) Single__regfile.put(Single__rti.values[0], 0, Single__rc.values[0]);}
  { if (Single__io_isWr.values[0]) Single__imem.put(Single__io_wrAddr.values[0], 0, Single__io_wrData.values[0]);}
  dat_t<10> Single__pc__shadow = T17;
  Single__pc = T17;
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
  sim_data.signals.push_back(new dat_api<32>(&mod->Single__rc));
  sim_data.signal_map["Single.rc"] = 0;
  sim_data.signals.push_back(new dat_api<5>(&mod->Single__rti));
  sim_data.signal_map["Single.rti"] = 1;
  std::string Single__regfile_path = "Single.regfile";
  for (size_t i = 0 ; i < 32 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__regfile.contents[i]));
    sim_data.signal_map[Single__regfile_path+"["+itos(i,false)+"]"] = 2+i;
  }
  std::string Single__imem_path = "Single.imem";
  for (size_t i = 0 ; i < 1024 ; i++) {
    sim_data.signals.push_back(new dat_api<32>(&mod->Single__imem.contents[i]));
    sim_data.signal_map[Single__imem_path+"["+itos(i,false)+"]"] = 34+i;
  }
  sim_data.signals.push_back(new dat_api<10>(&mod->Single__pc));
  sim_data.signal_map["Single.pc"] = 1058;
  sim_data.clk_map["clk"] = new clk_api(&mod->clk);
}
