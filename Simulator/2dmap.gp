set terminal png
file='data.txt'
set output "data.png"
set palette rgbformulae 30,31,32
set size 1, 0.8

unset key

set xlabel "x (m)"
set ylabel "y (m)"

# RSS
set cblabel "RSS (dBm)"
set cbrange[-35:-75]

set pm3d map

splot "data.txt" u ($1*1.43256):($2*0.82296):3 matrix w image