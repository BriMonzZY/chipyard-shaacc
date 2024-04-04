package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

// --------------
// Rocket+SHA2 Configs
// --------------

class Sha2RocketConfig extends Config(
  new sha2.WithSha2Accel ++                                // add SHA2 rocc accelerator
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)

class Sha2RocketPrintfConfig extends Config(
  new sha2.WithSha2Printf ++
  new sha2.WithSha2Accel ++                                // add SHA2 rocc accelerator
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)

