#include "Single.h"

int main (int argc, char* argv[]) {
  Single_t module;
  Single_api_t api(&module);
  module.init();
  api.init_sim_data();
  FILE *f = fopen("./Single.vcd", "w");
  module.set_dumpfile(f);
  while(!api.exit()) api.tick();
  if (f) fclose(f);
}
