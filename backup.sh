#!/bin/bash

read -r -p "是否要通过压缩备份chipyard-shaacc目录?压缩文件将生成在上级目录下，请确认！ [Y/n] " input

case $input in
    [yY][eE][sS]|[yY])
		echo "Yes"
		;;

    [nN][oO]|[nN])
		echo "No"
      exit 0 	;;

    *)
		echo "Invalid input..."
		exit 1
		;;
esac

cur_date=$(date +%Y-%m-%d-%H-%M)
echo $cur_date
cd ..
tar -cvf - chipyard-shaacc | pigz --best -k > chipyard-shaacc-$cur_date.tar.gz
