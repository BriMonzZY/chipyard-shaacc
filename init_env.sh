#!/bin/bash

conda install -n base conda-libmamba-solver
conda config --set solver libmamba
conda install -n base conda-lock==1.4.0
conda activate base

echo "开始安装chipyard依赖环境："
echo "跳过阶段："
echo "	5 pre-compiling Chipyard Scala sources"
echo "	6 initializing FireSim"
echo "	7 pre-compiling FireSim sources"
echo "	8 initializing FireMarshal"
echo "	9 pre-compiling FireMarshal default buildroot Linux sources"

./build-setup.sh riscv-tools -s 5 -s 6 -s 7 -s 8 -s 9

echo "环境安装完成"

