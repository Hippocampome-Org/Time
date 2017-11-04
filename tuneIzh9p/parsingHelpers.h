#ifndef PARSINGHELPERS_H
#define PARSINGHELPERS_H


static const char* getStringArgument(const char* const option, const int argc, const char* const argv[], const char * const def) {
  assert(option != NULL);
  assert(argc >= 0);
  assert(argv != NULL);
  for (int i = 0; i < argc - 1; i++) {
    if (0 == strcmp(option, argv[i]))
      return argv[i+1];
  }
  return def;
}

static int getIntegerArgument(const char* const option, const int argc, const char* const argv[], const int def) {
  assert(option != NULL);
  assert(argc >= 0);
  assert(argv != NULL);
  for (int i = 0; i < argc - 1; i++) {
    if (0 == strcmp(option, argv[i]))
      return atoi(argv[i+1]);
  }
  return def;
}

static bool getFlag(const char * const flag, const int argc, const char * const argv[]) {
  assert(flag != NULL);
  assert(argc >= 0);
  assert(argv != NULL);
  for (int i = 0; i < argc; i++) {
    if (0 == strcmp(flag, argv[i]))
      return true;
  }
  return false;
}
#endif /* PARSINGHELPERS_H */
