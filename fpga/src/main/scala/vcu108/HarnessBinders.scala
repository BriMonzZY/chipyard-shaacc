package chipyard.fpga.vcu108

// import chipyard.harness.OverrideHarnessBinder
// import chipyard.{CanHaveMasterTLMemPort, HasHarnessSignalReferences}
// import chisel3._
// import chisel3.experimental.BaseModule
// import freechips.rocketchip.tilelink.TLBundle
// import freechips.rocketchip.util.HeterogeneousBag
// import sifive.blocks.devices.spi.{HasPeripherySPI, SPIPortIO}
// import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}

import chisel3._
import chisel3.experimental.{BaseModule}

import freechips.rocketchip.util.{HeterogeneousBag}
import freechips.rocketchip.tilelink.{TLBundle}

import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}
import sifive.blocks.devices.spi.{HasPeripherySPI, SPIPortIO}

import chipyard._
import chipyard.harness._
import chipyard.iobinders._

/*** UART ***/
// class WithUART extends OverrideHarnessBinder({
//   (system: HasPeripheryUARTModuleImp, th: BaseModule with HasHarnessSignalReferences, ports: Seq[UARTPortIO]) => {
//     th match { case vcu108th: VCU108FPGATestHarnessImp => {
//       vcu108th.vcu108Outer.io_uart_bb.bundle <> ports.head
//     } }
//   }
// })
class WithUART extends HarnessBinder({
  case (th: VCU108FPGATestHarnessImp, port: UARTPort, chipId: Int) => {
    th.vcu108Outer.io_uart_bb.bundle <> port.io
  }
})


/*** SPI ***/
// class WithSPISDCard extends OverrideHarnessBinder({
//   (system: HasPeripherySPI, th: BaseModule with HasHarnessSignalReferences, ports: Seq[SPIPortIO]) => {
//     th match { case vcu108th: VCU108FPGATestHarnessImp => {
//       vcu108th.vcu108Outer.io_spi_bb.bundle <> ports.head
//     } }
//   }
// })
class WithSPISDCard extends HarnessBinder({
  case (th: VCU108FPGATestHarnessImp, port: SPIPort, chipId: Int) => {
    th.vcu108Outer.io_spi_bb.bundle <> port.io
  }
})

/*** Experimental DDR ***/
// class WithDDRMem extends OverrideHarnessBinder({
//   (system: CanHaveMasterTLMemPort, th: BaseModule with HasHarnessSignalReferences, ports: Seq[HeterogeneousBag[TLBundle]]) => {
//     th match { case vcu108th: VCU108FPGATestHarnessImp => {
//       require(ports.size == 1)

//       val bundles = vcu108th.vcu108Outer.ddrClient.out.map(_._1)
//       val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
//       bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
//       ddrClientBundle <> ports.head
//     } }
//   }
// })
class WithDDRMem extends HarnessBinder({
  case (th: VCU108FPGATestHarnessImp, port: TLMemPort, chipId: Int) => {
    val bundles = th.vcu108Outer.ddrClient.out.map(_._1)
    val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
    bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
    ddrClientBundle <> port.io
  }
})