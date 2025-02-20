package chipyard.fpga.vcu108

// import chipyard.harness.ApplyHarnessBinders
// import chipyard.iobinders.HasIOBinders
// import chipyard._
// import chisel3._
// import freechips.rocketchip.config._
// import freechips.rocketchip.diplomacy._
// import freechips.rocketchip.tilelink._
// import sifive.blocks.devices.spi._
// import sifive.blocks.devices.uart._
// import sifive.fpgashells.clocks._
// import sifive.fpgashells.ip.xilinx._
// import sifive.fpgashells.shell._
// import sifive.fpgashells.shell.xilinx._

import chisel3._
import chisel3.experimental.{IO}

import freechips.rocketchip.diplomacy.{LazyModule, LazyRawModuleImp, BundleBridgeSource}
import org.chipsalliance.cde.config.{Parameters}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy.{IdRange, TransferSizes}
import freechips.rocketchip.subsystem.{SystemBusKey}
import freechips.rocketchip.prci._
import sifive.fpgashells.shell.xilinx._
import sifive.fpgashells.ip.xilinx.{IBUF, PowerOnResetFPGAOnly}
import sifive.fpgashells.shell._
import sifive.fpgashells.clocks._

import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTPortIO}
import sifive.blocks.devices.spi.{PeripherySPIKey, SPIPortIO}

import chipyard._
import chipyard.harness._


class VCU108FPGATestHarness(override implicit val p: Parameters) extends VCU108ShellBasicOverlays {

  def dp = designParameters

  val pmod_is_sdio  = p(VCU108ShellPMOD) == "SDIO"
  // val jtag_location = Some(if (pmod_is_sdio) "FMC_J2" else "PMOD_J52")
  val jtag_location = Some("PMOD_J53")

  // Order matters; ddr depends on sys_clock
  val uart      = Overlay(UARTOverlayKey, new UARTVCU108ShellPlacer(this, UARTShellInput()))
  val sdio      = if (pmod_is_sdio) Some(Overlay(SPIOverlayKey, new SDIOVCU108ShellPlacer(this, SPIShellInput()))) else None
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugVCU108ShellPlacer(this, JTAGDebugShellInput(location = jtag_location)))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugVCU108ShellPlacer(this, cJTAGDebugShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanVCU108ShellPlacer(this, JTAGDebugBScanShellInput()))
  //val fmc       = Overlay(PCIeOverlayKey, new PCIeVCU108FMCShellPlacer(this, PCIeShellInput()))
  //val edge      = Overlay(PCIeOverlayKey, new PCIeVCU108EdgeShellPlacer(this, PCIeShellInput()))
  val sys_clock2 = Overlay(ClockInputOverlayKey, new SysClock2VCU108ShellPlacer(this, ClockInputShellInput()))
  val ddr2       = Overlay(DDROverlayKey, new DDR2VCU108ShellPlacer(this, DDRShellInput()))

  // val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")

// DOC include start: ClockOverlay
  // place all clocks in the shell
  require(dp(ClockInputOverlayKey).size >= 1)
  val sysClkNode = dp(ClockInputOverlayKey)(0).place(ClockInputDesignInput()).overlayOutput.node

  /*** Connect/Generate clocks ***/

  // connect to the PLL that will generate multiple clocks
  val harnessSysPLL = dp(PLLFactoryKey)()
  harnessSysPLL := sysClkNode

  // create and connect to the dutClock
  // println(s"VCU108 FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  // val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  // val dutWrangler = LazyModule(new ResetWrangler)
  // val dutGroup = ClockGroup()
  // dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  val dutFreqMHz = (dp(SystemBusKey).dtsFrequency.get / (1000 * 1000)).toInt
  val dutClock = ClockSinkNode(freqMHz = dutFreqMHz)
  println(s"VCU108 FPGA Base Clock Freq: ${dutFreqMHz} MHz")
  val dutWrangler = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL
// DOC include end: ClockOverlay

  /*** JTAG ***/
  val jtagPlacedOverlay = dp(JTAGDebugOverlayKey).head.place(JTAGDebugDesignInput())

  /*** UART ***/

// DOC include start: UartOverlay
  // 1st UART goes to the VCU108 dedicated UART

  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
// DOC include end: UartOverlay

  /*** SPI ***/

  // 1st SPI goes to the VCU108 SDIO port

  val io_spi_bb = BundleBridgeSource(() => (new SPIPortIO(dp(PeripherySPIKey).head)))
  dp(SPIOverlayKey).head.place(SPIDesignInput(dp(PeripherySPIKey).head, io_spi_bb))

  /*** DDR ***/

  val ddrNode = dp(DDROverlayKey).head.place(DDRDesignInput(dp(ExtTLMem).get.master.base, dutWrangler.node, harnessSysPLL)).overlayOutput.ddr

  // connect 1 mem. channel to the FPGA DDR
  // val inParams = topDesign match { case td: ChipTop =>
  //   td.lazySystem match { case lsys: CanHaveMasterTLMemPort =>
  //     lsys.memTLNode.edges.in(0)
  //   }
  // }
  // val ddrClient = TLClientNode(Seq(inParams.master))
  // ddrNode := ddrClient
  val ddrClient = TLClientNode(Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(
    name = "chip_ddr",
    sourceId = IdRange(0, 1 << dp(ExtTLMem).get.master.idBits)
  )))))
  ddrNode := TLWidthWidget(dp(ExtTLMem).get.master.beatBytes) := ddrClient

  // module implementation
  override lazy val module = new VCU108FPGATestHarnessImp(this)
}

class VCU108FPGATestHarnessImp(_outer: VCU108FPGATestHarness) extends LazyRawModuleImp(_outer) with HasHarnessInstantiators {
  override def provideImplicitClockToLazyChildren = true
  val vcu108Outer = _outer

  val reset = IO(Input(Bool())).suggestName("reset")
  _outer.xdc.addPackagePin(reset, "E36")
  _outer.xdc.addIOStandard(reset, "LVCMOS12")

  val resetIBUF = Module(new IBUF)
  resetIBUF.io.I := reset

  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock

  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  _outer.sdc.addAsyncPath(Seq(powerOnReset))

  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkVCU108PlacedOverlay) => !x.ereset_n
    case _ => false.B
  }

  _outer.pllReset := (resetIBUF.io.O || powerOnReset || ereset)

  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset

  // val buildtopClock = _outer.dutClock.in.head._1.clock
  // val buildtopReset = WireInit(hReset)
  // val dutReset = hReset.asAsyncReset
  // val success = false.B
  def referenceClockFreqMHz = _outer.dutFreqMHz
  def referenceClock = _outer.dutClock.in.head._1.clock
  def referenceReset = hReset
  def success = { require(false, "Unused"); false.B }

  // childClock := buildtopClock
  // childReset := buildtopReset
  childClock := referenceClock
  childReset := referenceReset

  // harness binders are non-lazy
  // _outer.topDesign match { case d: HasTestHarnessFunctions =>
  //   d.harnessFunctions.foreach(_(this))
  // }
  // _outer.topDesign match { case d: HasIOBinders =>
  //   ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  // }

  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  // _outer.topDesign match {
  //   case d: HasReferenceClockFreq => require(d.refClockFreqMHz == p(DefaultClockFrequencyKey))
  //   case _ =>
  // }

  instantiateChipTops()
}
