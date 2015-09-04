#ifndef __Single__
#define __Single__

#include "emulator.h"

class Single_t : public mod_t {
 private:
  val_t __rand_seed;
  void __srand(val_t seed) { __rand_seed = seed; }
  val_t __rand_val() { return ::__rand_val(&__rand_seed); }
 public:
  dat_t<1> Single__io_valid;
  dat_t<1> Single__io_boot;
  dat_t<1> Single__io_isWr;
  dat_t<1> T8;
  dat_t<1> reset;
  dat_t<5> Single__rti;
  dat_t<10> Single__io_wrAddr;
  dat_t<10> T17;
  dat_t<10> Single__pc;
  dat_t<10> Single__io_pc;
  dat_t<32> Single__rc;
  dat_t<32> Single__io_out;
  dat_t<32> Single__io_wrData;
  dat_t<32> Single__io_inst;
  mem_t<32,32> Single__regfile;
  mem_t<32,1024> Single__imem;
  dat_t<1> Single__io_valid__prev;
  dat_t<1> Single__io_boot__prev;
  dat_t<1> Single__io_isWr__prev;
  dat_t<10> Single__io_wrAddr__prev;
  dat_t<10> Single__pc__prev;
  dat_t<10> Single__io_pc__prev;
  dat_t<32> Single__io_out__prev;
  dat_t<32> Single__io_wrData__prev;
  dat_t<32> Single__io_inst__prev;
  clk_t clk;

  void init ( val_t rand_init = 0 );
  void clock_lo ( dat_t<1> reset );
  void clock_hi ( dat_t<1> reset );
  int clock ( dat_t<1> reset );
  void print ( FILE* f );
  void print ( std::ostream& s );
  void dump ( FILE* f, int t );
  void dump_init ( FILE* f );

};

#include "emul_api.h"
class Single_api_t : public emul_api_t {
 public:
  Single_api_t(mod_t* m) : emul_api_t(m) { }
  void init_sim_data();
};

#endif
