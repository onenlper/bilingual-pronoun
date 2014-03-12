#!/bin/sh
#./sh folder lang
./compile.sh
SRCLANG=$2

ENGTEST=MT.chiCoNLL.test.$1
CHITEST=chiCoNLL.test.$1

if [ $SRCLANG == "eng" ];then
ENGTEST=engCoNLL.test.$1
CHITEST=MT.engCoNLL.test.$1
echo "eng is"
fi

GENDER=/users/yzcchen/Downloads/gender.data.gz

JAR=.:../lib/liblinear-1.8.jar:../lib/mallet.jar:../lib/mallet-deps.jar:../lib/jaws-bin.jar:../lib/edu.mit.jwi_2.2.3.jar

MODEL=jointCoNLL.model.$1
ENGOUTPUT=engCoNLL.result.$1
CHIOUTPUT=chiCoNLL.result.$1


DECODER="ClosestFirst"
#CHIDECODER="PronounsCF"

DECODETH="0.5"
#CHIDECODETH="0.5"

echo java -Xmx30g -cp $JAR ims.coref.CorefCCMTDev -dev false -engIn $ENGTEST -gender $GENDER -chiIn $CHITEST  -model $MODEL -decode $DECODER -decodeTH $DECODETH -engOut $ENGOUTPUT -chiOut $CHIOUTPUT -srclang $SRCLANG

java -Xmx30g -cp $JAR ims.coref.CorefCCMTDev -dev false -engIn $ENGTEST -gender $GENDER -chiIn $CHITEST  -model $MODEL -decode $DECODER -decodeTH $DECODETH -engOut $ENGOUTPUT -chiOut $CHIOUTPUT -srclang $SRCLANG

