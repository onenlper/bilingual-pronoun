#!/bin/sh
#./train-nostack.sh folder oneM|twoM
./compile.sh
CHITRAIN=chiCoNLL.train.$1
ENGTRAIN=engCoNLL.train.$1

#CHITRAIN=chiCoNLL.key
#ENGTRAIN=engCoNLL.key

JAR=.:../lib/liblinear-1.8.jar:../lib/mallet.jar:../lib/mallet-deps.jar:../lib/jaws-bin.jar:../lib/edu.mit.jwi_2.2.3.jar
JOINT=$2
GENDER=/users/yzcchen/Downloads/gender.data.gz
CHIFEATURES=chifeatures.txt
ENGFEATURES=engfeatures.txt
ENGMES="NT-NP,T-PRP,T-PRP$,NER-ALL,NonReferential"
#ENGMES="NT-NP,T-PRP,T-PRP$,NER-ALL"
CHIMES="NT-NP,T-PN,T-NR"
#MES="NT-NP,T-PRP,T-PRP$,NER-ALL"
#STACK_FEATURES=/fs/scratch/scratch/anders/c12/submission/eng-closed/stackfs.txt

java -Xmx30g -cp $JAR ims.coref.TrainCCMTAllDev -chiIn $CHITRAIN -engIn $ENGTRAIN -gender $GENDER -chiFeature $CHIFEATURES -engFeature $ENGFEATURES -cores 12 -anaphoricityTh 0.95 -model jointCoNLL.model.$1 -chiMES $CHIMES -engES $ENGMES -joint $2

#java -cp $JAR ims.coref.TrainStacked1 -in $TRAIN -gender $GENDER -features $FEATURES -cores 12 -anaphoricityTh 0.95 -lang $LANGUAGE 

#java -cp $JAR ims.coref.TrainStacked2 -in $TRAIN -features $STACK_FEATURES -cores 12 -model coref-stacked.mdl -decode AvgMaxProb


# java -Xmx30g -cp /users/yzcchen/Downloads/ims-coref-conll2012/lib/liblinear-1.8.jar:/users/yzcchen/Downloads/ims-coref-conll2012/lib/mallet-deps.jar:/users/yzcchen/Downloads/ims-coref-conll2012/lib/mallet.jar:/users/yzcchen/Downloads/ims-coref-conll2012/lib/jaws-bin.jar:. ims.coref.Train -in engCoNLL.train.0 -gender /users/yzcchen/Downloads/gender.data.gz -features engfeatures.txt -cores 12 -anaphoricityTh 0.95 -lang eng -markableExtractors NT-NP,T-PRP,T-PRP$,NER-ALL,NonReferential
