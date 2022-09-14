echo "Zipping all java sources inside src/ ......"

srcs=`find ./src -name "*"`

echo $srcs | xargs zip -q pansy.zip
