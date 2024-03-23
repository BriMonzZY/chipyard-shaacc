#!/bin/bash

cur_date=$(date +%Y-%m-%d-%H-%M)
echo $cur_date
cd ..
tar -cvf - chipyard-shaacc | pigz --best -k > chipyard-shaacc-$cur_date.tar.gz

