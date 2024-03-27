package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

// --------------
// Rocket+SHA2 Configs
// These live in a separate file to simplify patching out for the tutorials.
// --------------

// DOC include start: Sha2Rocket
// class Sha2RocketConfig extends Config(
//   new sha2.WithSha2Accel ++                                // add SHA2 rocc accelerator
//   new freechips.rocketchip.subsystem.WithNBigCores(1) ++
//   new chipyard.config.AbstractConfig)
// DOC include end: Sha2Rocket

// class Sha2RocketPrintfConfig extends Config(
//   new sha2.WithSha2Printf ++
//   new sha2.WithSha2Accel ++                                // add SHA2 rocc accelerator
//   new freechips.rocketchip.subsystem.WithNBigCores(1) ++
//   new chipyard.config.AbstractConfig)

// class Sha2BBRocketConfig extends Config(
//   new sha2.WithSha2BlackBox ++
//   new sha2.WithSha2Accel ++                                // add SHA2 rocc accelerator
//   new freechips.rocketchip.subsystem.WithNBigCores(1) ++
//   new chipyard.config.AbstractConfig)
